import { neon, neonConfig } from '@neondatabase/serverless'; // Use neon for transactions
import { z } from 'zod';
import { unstable_noStore as noStore } from "next/cache";
import { createTransactionNotification } from './notification';

// Ensure ws is configured for Neon if needed (copy from account.ts or similar)
if (typeof globalThis.WebSocket === 'undefined') {
  try {
    const ws = require('ws');
    neonConfig.webSocketConstructor = ws.WebSocket;
  } catch (e) {
    console.warn('Could not load WebSocket implementation for Neon');
  }
}

// Transaction schema for validation
export const transactionSchema = z.object({
  portfolioId: z.number().int().positive(),
  productId: z.number().int().positive(), // Use productId consistently
  transactionType: z.enum(['buy', 'sell']),
  quantity: z.number().positive(),
  price: z.number().positive(),
  notes: z.string().max(500).optional(),
});

// Add userId to the input type for createTransaction
export type CreateTransactionInput = z.infer<typeof transactionSchema> & { userId: number };

export type Transaction = z.infer<typeof transactionSchema> & {
  id: number;
  assetId: number; // Keep assetId in the output type if needed
  createdAt: Date;
  portfolioName: string;
  productName: string;
};

// Database functions
export async function getTransactions() {
  noStore(); // Add noStore
  try {
    // Re-check this query later, ensure it joins correctly and calculates total
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    const result = await sql`
      SELECT 
        t.id,
        t.transaction_type,
        t.quantity,
        t.price,
        t.transaction_date,
        t.notes,
        t.created_at,
        p.name as portfolio_name,
        pr.name as product_name,
        t.asset_id -- Include asset_id if needed elsewhere
      FROM transactions t
      JOIN portfolios p ON t.portfolio_id = p.id
      JOIN assets a ON t.asset_id = a.id
      JOIN products pr ON a.product_id = pr.id
      ORDER BY t.transaction_date DESC, t.created_at DESC
    `;
    
    return result.map((row: any) => ({
      id: row.id,
      transactionType: row.transaction_type,
      quantity: parseFloat(row.quantity),
      price: parseFloat(row.price),
      transactionDate: row.transaction_date,
      notes: row.notes,
      createdAt: row.created_at,
      portfolioName: row.portfolio_name,
      productName: row.product_name,
      assetId: row.asset_id,
      total: parseFloat(row.quantity) * parseFloat(row.price)
    }));
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch transactions.');
  }
}

export async function getTransactionById(id: number) {
  noStore(); // Add noStore
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    const result = await sql`
      SELECT 
        t.id,
        t.portfolio_id,
        t.asset_id,
        t.transaction_type,
        t.quantity,
        t.price,
        t.transaction_date,
        t.notes,
        t.created_at,
        p.name as portfolio_name,
        pr.name as product_name
      FROM transactions t
      JOIN portfolios p ON t.portfolio_id = p.id
      JOIN assets a ON t.asset_id = a.id
      JOIN products pr ON a.product_id = pr.id
      WHERE t.id = ${id}
    `;
    
    const transaction = result[0];
    if (!transaction) return null;
    
    return {
      id: transaction.id,
      portfolioId: transaction.portfolio_id,
      assetId: transaction.asset_id,
      transactionType: transaction.transaction_type,
      quantity: parseFloat(transaction.quantity),
      price: parseFloat(transaction.price),
      transactionDate: transaction.transaction_date,
      notes: transaction.notes,
      createdAt: transaction.created_at,
      portfolioName: transaction.portfolio_name,
      productName: transaction.product_name,
      total: parseFloat(transaction.quantity) * parseFloat(transaction.price)
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch transaction.');
  }
}

// Rewritten createTransaction with validation and balance update
export async function createTransaction(data: CreateTransactionInput) {
  noStore(); // Prevent caching stale balance/asset data
  const { portfolioId, productId, transactionType, quantity, price, notes, userId } = data;
  const transactionValue = quantity * price;

  const connectionString = process.env.POSTGRES_URL;
  if (!connectionString) {
    throw new Error("Database connection string is not configured.");
  }
  const sql = neon(connectionString);

  await sql`BEGIN`;

  try {
    // 1. Fetch current portfolio cash balance and name
    const portfolioResult = await sql`
      SELECT cash_balance, name FROM portfolios WHERE id = ${portfolioId}
    `;
    if (portfolioResult.length === 0) {
      throw new Error("Portfolio not found.");
    }
    const currentCashBalance = parseFloat(portfolioResult[0].cash_balance);
    const portfolioName = portfolioResult[0].name;

    // 2. Fetch product name
    const productResult = await sql`
      SELECT name FROM products WHERE id = ${productId}
    `;
    if (productResult.length === 0) {
      throw new Error("Product not found.");
    }
    const productName = productResult[0].name;

    // 2. Fetch current asset quantity (if exists)
    const assetResult = await sql`
      SELECT id, quantity FROM assets WHERE portfolio_id = ${portfolioId} AND product_id = ${productId}
    `;
    const currentAsset = assetResult.length > 0 ? assetResult[0] : null;
    const currentAssetQuantity = currentAsset ? parseFloat(currentAsset.quantity) : 0;
    let assetId = currentAsset ? currentAsset.id : null;

    let newAssetQuantity: number;
    let newCashBalance: number;

    // 3. Validate transaction
    if (transactionType === 'buy') {
      if (currentCashBalance < transactionValue) {
        throw new Error(`Insufficient funds. Required: ${transactionValue.toFixed(2)}, Available: ${currentCashBalance.toFixed(2)}`);
      }
      newAssetQuantity = currentAssetQuantity + quantity;
      newCashBalance = currentCashBalance - transactionValue;
    } else { // Sell transaction
      if (!currentAsset || currentAssetQuantity < quantity) {
        throw new Error(`Insufficient asset quantity. Trying to sell: ${quantity}, Available: ${currentAssetQuantity}`);
      }
      newAssetQuantity = currentAssetQuantity - quantity;
      newCashBalance = currentCashBalance + transactionValue;
    }

    // 4. Update or Insert Asset
    if (currentAsset) {
      // Update existing asset quantity
      // Consider deleting if newAssetQuantity is very close to 0?
      await sql`
        UPDATE assets SET quantity = ${newAssetQuantity} WHERE id = ${assetId}
      `;
    } else if (transactionType === 'buy') {
      // Insert new asset if buying and it doesn't exist
      // NOTE: Assumes purchase_price and current_price should be set to the transaction price here
      const insertAssetResult = await sql`
        INSERT INTO assets (portfolio_id, product_id, quantity, purchase_price, current_price)
        VALUES (${portfolioId}, ${productId}, ${quantity}, ${price}, ${price})
        RETURNING id
      `;
      // We need the new assetId for the transaction record
      if (!insertAssetResult || insertAssetResult.length === 0) {
          throw new Error("Failed to insert new asset record.");
      }
      const newAssetId = insertAssetResult[0].id;
      // Set assetId for transaction insertion below
      // This assignment feels a bit clumsy, maybe structure differently?
      // For now, we need an assetId for the INSERT INTO transactions
      assetId = newAssetId; 
    } else {
        // This case should not happen due to validation above (trying to sell non-existent asset)
        throw new Error("Cannot sell an asset that does not exist in the portfolio.");
    }
    
    // Ensure assetId is available before inserting transaction
    if (!assetId) {
        throw new Error("Asset ID missing for transaction record.");
    }

    // 5. Update Portfolio Cash Balance
    await sql`
      UPDATE portfolios SET cash_balance = ${newCashBalance} WHERE id = ${portfolioId}
    `;

    // 6. Insert Transaction Record
    await sql`
      INSERT INTO transactions (
        portfolio_id,
        asset_id,
        transaction_type,
        quantity,
        price,
        notes,
        transaction_date
      )
      VALUES (
        ${portfolioId},
        ${assetId},
        ${transactionType},
        ${quantity},
        ${price},
        ${notes || null},
        TIMEZONE('Asia/Jakarta', NOW())
      )
    `;

    // After successful transaction, create notification
    await createTransactionNotification(
      userId,
      transactionType,
      quantity,
      productName,
      portfolioName
    );

    await sql`COMMIT`;

    // Note: Cache invalidation will be handled by router.refresh() on the client side
    return { success: true, message: "Transaction recorded successfully." };

  } catch (error) {
    await sql`ROLLBACK`;
    console.error('Database Error: Failed to create transaction:', error);
    if (error instanceof Error) {
      throw new Error(`Failed to create transaction: ${error.message}`);
    }
    throw error;
  }
} 