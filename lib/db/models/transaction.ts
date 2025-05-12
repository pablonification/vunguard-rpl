import { sql } from '@vercel/postgres';
import { z } from 'zod';

// Transaction schema for validation
export const transactionSchema = z.object({
  portfolioId: z.number(),
  assetId: z.number(),
  transactionType: z.enum(['buy', 'sell']),
  quantity: z.number().positive(),
  price: z.number().positive(),
  transactionDate: z.string(), // Will be converted to timestamp in createTransaction
  notes: z.string().optional(),
});

export type Transaction = z.infer<typeof transactionSchema> & {
  id: number;
  createdAt: Date;
  portfolioName: string;
  productName: string; // Changed from assetName to productName
};

// Database functions
export async function getTransactions() {
  try {
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
        pr.name as product_name
      FROM transactions t
      JOIN portfolios p ON t.portfolio_id = p.id
      JOIN assets a ON t.asset_id = a.id
      JOIN products pr ON a.product_id = pr.id
      ORDER BY t.transaction_date DESC, t.created_at DESC
    `;
    
    return result.rows.map(row => ({
      id: row.id,
      transactionType: row.transaction_type,
      quantity: parseFloat(row.quantity),
      price: parseFloat(row.price),
      transactionDate: row.transaction_date,
      notes: row.notes,
      createdAt: row.created_at,
      portfolioName: row.portfolio_name,
      productName: row.product_name, // Changed from assetName to productName
      // Calculate total for display
      total: parseFloat(row.quantity) * parseFloat(row.price)
    }));
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch transactions.');
  }
}

export async function getTransactionById(id: number) {
  try {
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
    
    const transaction = result.rows[0];
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
      productName: transaction.product_name, // Changed from assetName to productName
      // Calculate total for display
      total: parseFloat(transaction.quantity) * parseFloat(transaction.price)
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch transaction.');
  }
}

export async function createTransaction(data: z.infer<typeof transactionSchema>) {
  try {
    const { portfolioId, assetId, transactionType, quantity, price, transactionDate, notes } = data;
    
    const result = await sql`
      INSERT INTO transactions (
        portfolio_id,
        asset_id,
        transaction_type,
        quantity,
        price,
        transaction_date,
        notes
      )
      VALUES (
        ${portfolioId},
        ${assetId},
        ${transactionType},
        ${quantity},
        ${price},
        ${new Date(transactionDate).toISOString()},
        ${notes || null}
      )
      RETURNING id, created_at
    `;
    
    const transaction = result.rows[0];
    return {
      id: transaction.id,
      createdAt: transaction.created_at,
      ...data,
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to create transaction.');
  }
} 