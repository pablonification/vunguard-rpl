"use server"

import { sql } from "@vercel/postgres"
import { getSession } from "@/lib/auth"
import { revalidatePath } from "next/cache"

export async function createPortfolio(name: string, description?: string, targetInvestorId?: number | null) {
  try {
    const session = await getSession()
    if (!session) {
      return { success: false, error: "Unauthorized" }
    }

    // Determine the account ID to create the portfolio for
    let accountId: number;
    
    // If the user is a manager and a target investor ID is provided
    if (session.role === 'manager' && targetInvestorId) {
      // Verify the target investor exists and has investor role
      const investorCheck = await sql`
        SELECT id FROM accounts 
        WHERE id = ${targetInvestorId} AND role = 'investor'
      `
      
      if (investorCheck.rows.length === 0) {
        return { success: false, error: "Invalid investor selected" }
      }
      
      // Use the target investor's ID
      accountId = targetInvestorId
    } else {
      // Otherwise, use the current user's ID
      accountId = Number(session.id)
    }

    // Validate input
    if (!name.trim()) {
      return { success: false, error: "Portfolio name is required" }
    }

    if (name.length > 100) {
      return { success: false, error: "Portfolio name must be less than 100 characters" }
    }

    if (description && description.length > 500) {
      return { success: false, error: "Description must be less than 500 characters" }
    }

    // Check if portfolio with same name exists for this account
    const existingPortfolio = await sql`
      SELECT id FROM portfolios 
      WHERE account_id = ${accountId} AND name = ${name}
    `

    if (existingPortfolio.rows.length > 0) {
      return { success: false, error: "A portfolio with this name already exists" }
    }

    // Create the portfolio
    const result = await sql`
      INSERT INTO portfolios (name, description, account_id, created_at, updated_at, cash_balance)
      VALUES (${name}, ${description || null}, ${accountId}, NOW(), NOW(), 0)
      RETURNING id
    `

    const portfolioId = result.rows[0].id

    // Revalidate the portfolios page
    revalidatePath("/dashboard/portfolios")

    return { success: true, portfolioId }
  } catch (error) {
    console.error("Failed to create portfolio:", error)
    return { success: false, error: "Failed to create portfolio" }
  }
} 