'use server'

import { revalidatePath } from 'next/cache'
import { requireAuth } from '@/lib/auth'
import { executeQuery } from '@/lib/db'
import { neon } from '@neondatabase/serverless'

// Get portfolios for a specific investor (used by TopUpDialog)
export async function getInvestorPortfolios(accountId: number) {
  const session = await requireAuth(['investor']) // Ensure caller is an investor
  if (session.id !== accountId) {
    throw new Error("Unauthorized: You can only view your own portfolios.")
  }

  try {
    const portfolios = await executeQuery(
      `SELECT id, name FROM portfolios WHERE account_id = $1 ORDER BY name`,
      [accountId]
    )
    return portfolios as { id: number; name: string }[]
  } catch (error) {
    console.error("Database Error: Failed to fetch investor portfolios", error)
    throw new Error("Failed to load portfolios.")
  }
}

// Perform the top-up (deposit transaction + update cash balance)
export async function performTopUp(portfolioId: number, amount: number) {
  const session = await requireAuth(['investor']) // Ensure caller is an investor
  const userId = session.id

  if (amount <= 0) {
    throw new Error("Top-up amount must be positive.")
  }

  // Use Neon client directly for transaction
  const connectionString = process.env.POSTGRES_URL
  if (!connectionString) {
    throw new Error("Database connection string is not configured.")
  }
  const sql = neon(connectionString)

  try {
    // Start transaction manually
    await sql`BEGIN`

    try {
      // 1. Verify the portfolio belongs to the investor (using the base sql object)
      const portfolioCheck = await sql`
        SELECT account_id FROM portfolios WHERE id = ${portfolioId}
      `
      if (portfolioCheck.length === 0 || portfolioCheck[0].account_id !== userId) {
        throw new Error("Unauthorized: Portfolio does not belong to the user.")
      }

      // 2. Insert the deposit into the NEW cash_transactions table
      await sql`
        INSERT INTO cash_transactions (portfolio_id, type, amount, created_by)
        VALUES (${portfolioId}, 'deposit', ${amount}, ${userId})
      `

      // 3. Update the portfolio cash balance
      await sql`
        UPDATE portfolios
        SET cash_balance = cash_balance + ${amount}
        WHERE id = ${portfolioId}
      `

      // Commit transaction if all steps succeeded
      await sql`COMMIT`

    } catch (innerError) {
      // Rollback transaction if any step failed
      await sql`ROLLBACK`
      // Rethrow the specific error from the failed step
      console.error("Transaction Error during top-up:", innerError)
      if (innerError instanceof Error) {
        throw new Error(`Top-up failed during transaction: ${innerError.message}`)
      } else {
        throw new Error("An unknown error occurred within the top-up transaction.")
      }
    }

    // Revalidate relevant paths *after* successful commit
    revalidatePath('/dashboard')
    revalidatePath(`/dashboard/portfolios/${portfolioId}`)
    revalidatePath('/dashboard/transactions')

    return { success: true }

  } catch (outerError) {
    // Handle errors starting/committing/rolling back the transaction itself, or rethrown errors
    console.error("Database Error: Failed to perform top-up transaction", outerError)
    if (outerError instanceof Error) {
      throw new Error(`Top-up failed: ${outerError.message}`)
    } else {
      throw new Error("An unknown error occurred during the top-up process.")
    }
  }
} 