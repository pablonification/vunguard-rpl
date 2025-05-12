import { neon } from '@neondatabase/serverless';
import { z } from 'zod';
import { unstable_noStore as noStore } from 'next/cache';
import { createAnalystRecommendationNotification } from './notification';

// Recommendation types
export const RecommendationType = {
  BUY: 'buy',
  SELL: 'sell',
  HOLD: 'hold',
} as const;

// Recommendation status
export const RecommendationStatus = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  IMPLEMENTED: 'implemented',
} as const;

// Schema for creating recommendations
export const recommendationSchema = z.object({
  analystId: z.number().int().positive(),
  productId: z.number().int().positive(),
  type: z.enum([
    RecommendationType.BUY,
    RecommendationType.SELL,
    RecommendationType.HOLD
  ]),
  targetPrice: z.number().positive(),
  currentPrice: z.number().positive(),
  confidence: z.number().min(1).max(5), // Confidence level 1-5
  timeframe: z.string(), // e.g., "short_term", "medium_term", "long_term"
  rationale: z.string().max(2000),
  technicalAnalysis: z.string().max(2000).optional(),
  fundamentalAnalysis: z.string().max(2000).optional(),
  risks: z.string().max(1000).optional(),
});

export type CreateRecommendationInput = z.infer<typeof recommendationSchema>;

export type Recommendation = CreateRecommendationInput & {
  id: number;
  status: keyof typeof RecommendationStatus;
  createdAt: Date;
  updatedAt: Date;
  implementedAt: Date | null;
  productName: string;
  analystName: string;
};

// Database functions
export async function createRecommendation(data: CreateRecommendationInput) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    // Start transaction
    await sql`BEGIN`;

    try {
      // Get product and analyst details for notification
      const productResult = await sql`
        SELECT name FROM products WHERE id = ${data.productId}
      `;
      const analystResult = await sql`
        SELECT username FROM accounts WHERE id = ${data.analystId}
      `;

      if (productResult.length === 0) throw new Error("Product not found");
      if (analystResult.length === 0) throw new Error("Analyst not found");

      const productName = productResult[0].name;
      const analystName = analystResult[0].username;

      // Insert recommendation
      const result = await sql`
        INSERT INTO recommendations (
          analyst_id,
          product_id,
          type,
          target_price,
          current_price,
          confidence,
          timeframe,
          rationale,
          technical_analysis,
          fundamental_analysis,
          risks,
          status
        ) VALUES (
          ${data.analystId},
          ${data.productId},
          ${data.type},
          ${data.targetPrice},
          ${data.currentPrice},
          ${data.confidence},
          ${data.timeframe},
          ${data.rationale},
          ${data.technicalAnalysis || null},
          ${data.fundamentalAnalysis || null},
          ${data.risks || null},
          ${RecommendationStatus.PENDING}
        )
        RETURNING id, created_at
      `;

      // Get all portfolio managers to notify them
      const managers = await sql`
        SELECT id FROM accounts WHERE role = 'manager'
      `;

      // Create notifications for all managers
      for (const manager of managers) {
        await createAnalystRecommendationNotification(
          manager.id,
          productName,
          data.type,
          analystName
        );
      }

      await sql`COMMIT`;

      return {
        ...data,
        id: result[0].id,
        status: RecommendationStatus.PENDING,
        createdAt: result[0].created_at,
        updatedAt: result[0].created_at,
        implementedAt: null,
        productName,
        analystName,
      };
    } catch (error) {
      await sql`ROLLBACK`;
      throw error;
    }
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to create recommendation.');
  }
}

export async function getRecommendations(filters?: {
  analystId?: number;
  productId?: number;
  status?: keyof typeof RecommendationStatus;
  timeframe?: string;
}) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    // Handle different filter combinations by building specific queries
    let query;
    
    // No filters - return all recommendations
    if (!filters || Object.keys(filters).length === 0) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        ORDER BY r.created_at DESC
      `;
    } 
    // Just analystId filter
    else if (filters.analystId && !filters.productId && !filters.status && !filters.timeframe) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE r.analyst_id = ${filters.analystId}
        ORDER BY r.created_at DESC
      `;
    } 
    // Just productId filter
    else if (!filters.analystId && filters.productId && !filters.status && !filters.timeframe) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE r.product_id = ${filters.productId}
        ORDER BY r.created_at DESC
      `;
    }
    // Just status filter
    else if (!filters.analystId && !filters.productId && filters.status && !filters.timeframe) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE r.status = ${filters.status}
        ORDER BY r.created_at DESC
      `;
    }
    // Just timeframe filter
    else if (!filters.analystId && !filters.productId && !filters.status && filters.timeframe) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE r.timeframe = ${filters.timeframe}
        ORDER BY r.created_at DESC
      `;
    }
    // AnalystId + status (common case for analyst's view)
    else if (filters.analystId && !filters.productId && filters.status && !filters.timeframe) {
      query = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE r.analyst_id = ${filters.analystId} AND r.status = ${filters.status}
        ORDER BY r.created_at DESC
      `;
    }
    // Handle all other combinations with individual WHERE clauses
    else {
      // Build the base query
      let baseQuery = sql`
        SELECT 
          r.*,
          p.name as product_name,
          a.username as analyst_name
        FROM recommendations r
        JOIN products p ON r.product_id = p.id
        JOIN accounts a ON r.analyst_id = a.id
        WHERE 1=1
      `;
      
      // Add each condition individually
      if (filters.analystId) {
        baseQuery = sql`${baseQuery} AND r.analyst_id = ${filters.analystId}`;
      }
      if (filters.productId) {
        baseQuery = sql`${baseQuery} AND r.product_id = ${filters.productId}`;
      }
      if (filters.status) {
        baseQuery = sql`${baseQuery} AND r.status = ${filters.status}`;
      }
      if (filters.timeframe) {
        baseQuery = sql`${baseQuery} AND r.timeframe = ${filters.timeframe}`;
      }
      
      // Add ordering
      query = sql`${baseQuery} ORDER BY r.created_at DESC`;
    }
    
    const result = await query;

    return result.map((row: any) => ({
      id: row.id,
      analystId: row.analyst_id,
      productId: row.product_id,
      type: row.type,
      targetPrice: parseFloat(row.target_price),
      currentPrice: parseFloat(row.current_price),
      confidence: row.confidence,
      timeframe: row.timeframe,
      rationale: row.rationale,
      technicalAnalysis: row.technical_analysis,
      fundamentalAnalysis: row.fundamental_analysis,
      risks: row.risks,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
      implementedAt: row.implemented_at,
      productName: row.product_name,
      analystName: row.analyst_name,
    }));
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch recommendations.');
  }
}

export async function updateRecommendationStatus(
  id: number,
  status: keyof typeof RecommendationStatus,
  managerId: number
) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    const result = await sql`
      UPDATE recommendations
      SET 
        status = ${status},
        updated_at = CURRENT_TIMESTAMP,
        implemented_at = ${status === RecommendationStatus.IMPLEMENTED ? sql`CURRENT_TIMESTAMP` : null}
      WHERE id = ${id}
      RETURNING *
    `;

    if (result.length === 0) {
      throw new Error('Recommendation not found.');
    }

    // If implemented, notify the analyst
    if (status === RecommendationStatus.IMPLEMENTED) {
      // Get product and manager details
      const [product, manager] = await Promise.all([
        sql`SELECT name FROM products WHERE id = ${result[0].product_id}`,
        sql`SELECT username FROM accounts WHERE id = ${managerId}`
      ]);

      // Create notification for the analyst
      await createAnalystRecommendationNotification(
        result[0].analyst_id,
        product[0].name,
        'implemented',
        manager[0].username
      );
    }

    return result[0];
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to update recommendation status.');
  }
} 