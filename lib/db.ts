import { neon } from "@neondatabase/serverless"
import { drizzle } from "drizzle-orm/neon-http"

// Get connection string from either DATABASE_URL or POSTGRES_URL
const connectionString = process.env.DATABASE_URL || process.env.POSTGRES_URL
// console.log('Attempting database connection with:', 
//   connectionString ? 'Connection string found' : 'No connection string found',
//   '\nDATABASE_URL exists:', !!process.env.DATABASE_URL,
//   '\nPOSTGRES_URL exists:', !!process.env.POSTGRES_URL
// )

if (!connectionString) {
  throw new Error('Database connection string not found. Please set either DATABASE_URL or POSTGRES_URL in your environment variables.')
}

// Create a SQL client with the connection string
const sql = neon(connectionString)
export const db = drizzle(sql)

// Helper function to execute raw SQL queries
export async function executeQuery(query: string, params: any[] = []) {
  try {
    return await sql.query(query, params)
  } catch (error) {
    console.error("Database query error:", error)
    throw error
  }
}
