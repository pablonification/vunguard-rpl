import { neon, neonConfig } from '@neondatabase/serverless';
import { unstable_noStore as noStore } from "next/cache";
import { z } from 'zod';
import bcrypt from 'bcryptjs';

// Configure Neon client
neonConfig.fetchConnectionCache = true;

// For Next.js Edge Runtime and Serverless environments
if (typeof globalThis.WebSocket === 'undefined') {
  // In Node.js environments, we need to provide a WebSocket implementation
  // This will be ignored in browser environments where WebSocket is available
  try {
    // Try to dynamically import ws (it should be available in Node.js)
    const ws = require('ws');
    neonConfig.webSocketConstructor = ws.WebSocket;
  } catch (e) {
    console.warn('Could not load WebSocket implementation for Neon');
  }
}

// Connection string
const connectionString = process.env.POSTGRES_URL || 
  "postgres://neondb_owner:npg_0DtOI4YdeNJl@ep-late-queen-a1p6lfmz-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require";

// Create SQL executor
const sql = neon(connectionString);

// Account schema for validation
export const accountSchema = z.object({
  username: z.string().min(3).max(50),
  fullName: z.string().min(3).max(100),
  email: z.string().email(),
  password: z.string().min(6).optional(),
  role: z.enum(['investor', 'manager', 'analyst', 'admin']),
});

export type Account = z.infer<typeof accountSchema> & {
  id: number;
  createdAt: Date;
};

// Database functions
export async function getAccounts() {
  noStore();
  try {
    const result = await sql`
      SELECT id, username, full_name, email, role, created_at
      FROM accounts
      ORDER BY created_at DESC
    `;
    return result.map((row: any) => ({
      id: row.id,
      username: row.username,
      fullName: row.full_name,
      email: row.email,
      role: row.role,
      createdAt: row.created_at,
    }));
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch accounts.');
  }
}

export async function getAccountById(id: number) {
  noStore();
  try {
    const result = await sql`
      SELECT id, username, full_name, email, role, created_at
      FROM accounts
      WHERE id = ${id}
    `;
    const account = result[0];
    if (!account) return null;
    
    return {
      id: account.id,
      username: account.username,
      fullName: account.full_name,
      email: account.email,
      role: account.role,
      createdAt: account.created_at,
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch account.');
  }
}

export async function updateAccount(id: number, data: Partial<Account>) {
  noStore();
  try {
    const { username, fullName, email, role, password } = data;
    let hashedPassword = undefined;
    
    if (password) {
      hashedPassword = await bcrypt.hash(password, 10);
    }

    const result = await sql`
      UPDATE accounts
      SET 
        username = COALESCE(${username}, username),
        full_name = COALESCE(${fullName}, full_name),
        email = COALESCE(${email}, email),
        role = COALESCE(${role}, role),
        password = COALESCE(${hashedPassword}, password)
      WHERE id = ${id}
      RETURNING id, username, full_name, email, role, created_at
    `;
    
    const account = result[0];
    return {
      id: account.id,
      username: account.username,
      fullName: account.full_name,
      email: account.email,
      role: account.role,
      createdAt: account.created_at,
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to update account.');
  }
}

export async function createAccount(data: z.infer<typeof accountSchema>) {
  noStore();
  try {
    const { username, fullName, email, role, password } = data;
    const hashedPassword = await bcrypt.hash(password!, 10);

    const result = await sql`
      INSERT INTO accounts (username, full_name, email, role, password)
      VALUES (${username}, ${fullName}, ${email}, ${role}, ${hashedPassword})
      RETURNING id, username, full_name, email, role, created_at
    `;
    
    const account = result[0];
    return {
      id: account.id,
      username: account.username,
      fullName: account.full_name,
      email: account.email,
      role: account.role,
      createdAt: account.created_at,
    };
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to create account.');
  }
}

// Delete an account
export async function deleteAccount(id: number) {
  noStore(); // Ensure we get fresh data state if needed, although deletion doesn't return data
  try {
    // Ensure related data (e.g., portfolios, transactions) is handled appropriately.
    // The current schema uses ON DELETE CASCADE for portfolios, 
    // which means related portfolios (and their assets, transactions, etc.) will be deleted.
    // Consider if this is the desired behavior or if accounts should be soft-deleted.
    await sql`
      DELETE FROM accounts
      WHERE id = ${id}
    `;
  } catch (error) {
    console.error('Database Error:', error);
    // Add more specific error handling if needed (e.g., check for foreign key constraints if CASCADE wasn't used)
    throw new Error('Failed to delete account.');
  }
} 