import { neon } from '@neondatabase/serverless';
import { z } from 'zod';
import { unstable_noStore as noStore } from 'next/cache';

// Notification types
export const NotificationType = {
  PORTFOLIO_CHANGE: 'portfolio_change',
  TRANSACTION: 'transaction',
  ANALYST_RECOMMENDATION: 'analyst_recommendation',
  SYSTEM: 'system',
} as const;

// Notification priority levels
export const NotificationPriority = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
} as const;

// Schema for creating notifications
export const notificationSchema = z.object({
  userId: z.number().int().positive(),
  type: z.enum([
    NotificationType.PORTFOLIO_CHANGE,
    NotificationType.TRANSACTION,
    NotificationType.ANALYST_RECOMMENDATION,
    NotificationType.SYSTEM
  ]),
  title: z.string().max(200),
  message: z.string().max(1000),
  priority: z.enum([
    NotificationPriority.LOW,
    NotificationPriority.MEDIUM,
    NotificationPriority.HIGH
  ]),
  metadata: z.record(z.any()).optional(), // Additional JSON data
});

export type CreateNotificationInput = z.infer<typeof notificationSchema>;

export type Notification = CreateNotificationInput & {
  id: number;
  createdAt: Date;
  readAt: Date | null;
};

// Database functions
export async function createNotification(data: CreateNotificationInput) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    const result = await sql`
      INSERT INTO notifications (
        user_id,
        type,
        title,
        message,
        priority,
        metadata
      ) VALUES (
        ${data.userId},
        ${data.type},
        ${data.title},
        ${data.message},
        ${data.priority},
        ${data.metadata ? JSON.stringify(data.metadata) : null}
      )
      RETURNING id, created_at
    `;

    return {
      ...data,
      id: result[0].id,
      createdAt: result[0].created_at,
      readAt: null,
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to create notification.');
  }
}

export async function getNotifications(userId: number, limit = 50) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    const result = await sql`
      SELECT 
        id,
        user_id,
        type,
        title,
        message,
        priority,
        metadata,
        created_at,
        read_at
      FROM notifications
      WHERE user_id = ${userId}
      ORDER BY created_at DESC
      LIMIT ${limit}
    `;

    return result.map((row: any) => ({
      id: row.id,
      userId: row.user_id,
      type: row.type,
      title: row.title,
      message: row.message,
      priority: row.priority,
      metadata: row.metadata,
      createdAt: row.created_at,
      readAt: row.read_at,
    }));
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch notifications.');
  }
}

export async function markNotificationAsRead(notificationId: number, userId: number) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    await sql`
      UPDATE notifications
      SET read_at = CURRENT_TIMESTAMP
      WHERE id = ${notificationId}
      AND user_id = ${userId}
      AND read_at IS NULL
    `;

    return { success: true };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to mark notification as read.');
  }
}

export async function getUnreadNotificationCount(userId: number) {
  noStore();
  try {
    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    const result = await sql`
      SELECT COUNT(*) as count
      FROM notifications
      WHERE user_id = ${userId}
      AND read_at IS NULL
    `;

    return parseInt(result[0].count);
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to get unread notification count.');
  }
}

// Helper function to create transaction notifications
export async function createTransactionNotification(
  userId: number,
  transactionType: 'buy' | 'sell',
  quantity: number,
  productName: string,
  portfolioName: string
) {
  const title = `${transactionType.toUpperCase()} Transaction Completed`;
  const message = `${transactionType === 'buy' ? 'Bought' : 'Sold'} ${quantity} units of ${productName} in portfolio "${portfolioName}"`;
  
  return createNotification({
    userId,
    type: NotificationType.TRANSACTION,
    title,
    message,
    priority: NotificationPriority.MEDIUM,
    metadata: {
      transactionType,
      quantity,
      productName,
      portfolioName,
    },
  });
}

// Helper function to create analyst recommendation notifications
export async function createAnalystRecommendationNotification(
  userId: number,
  productName: string,
  recommendation: string,
  analyst: string
) {
  return createNotification({
    userId,
    type: NotificationType.ANALYST_RECOMMENDATION,
    title: `New Analyst Recommendation for ${productName}`,
    message: `Analyst ${analyst} has recommended to ${recommendation} ${productName}`,
    priority: NotificationPriority.HIGH,
    metadata: {
      productName,
      recommendation,
      analyst,
    },
  });
} 