import { executeQuery } from "@/lib/db"

export interface Portfolio {
  id: number;
  name: string;
  description: string | null;
  total_value: number;
  return_percentage: number;
  asset_count: number;
  last_updated: string;
  cash_balance: number;
}

export async function getPortfolios(accountId: number): Promise<Portfolio[]> {
  try {
    // Query to get portfolios with their total value (including cash), return, and asset count
    const query = `
      WITH latest_performance AS (
        SELECT 
          portfolio_id,
          value as asset_value,
          return_percentage,
          date as last_updated
        FROM performances
        WHERE asset_id IS NULL
        AND date = (
          SELECT MAX(date)
          FROM performances p2
          WHERE p2.portfolio_id = performances.portfolio_id
          AND p2.asset_id IS NULL
        )
      ),
      asset_counts AS (
        SELECT 
          portfolio_id,
          COUNT(*) as asset_count
        FROM assets
        GROUP BY portfolio_id
      )
      SELECT 
        p.id,
        p.name,
        p.description,
        p.cash_balance,
        COALESCE(lp.asset_value, 0) + p.cash_balance as total_value,
        COALESCE(lp.return_percentage, 0) as return_percentage,
        COALESCE(ac.asset_count, 0) as asset_count,
        COALESCE(lp.last_updated, p.created_at) as last_updated
      FROM portfolios p
      LEFT JOIN latest_performance lp ON p.id = lp.portfolio_id
      LEFT JOIN asset_counts ac ON p.id = ac.portfolio_id
      WHERE p.account_id = $1
      ORDER BY p.name ASC
    `

    const result = await executeQuery(query, [accountId])
    return (result || []).map(row => ({
      ...row,
      total_value: parseFloat(row.total_value),
      return_percentage: parseFloat(row.return_percentage),
      asset_count: parseInt(row.asset_count),
      cash_balance: parseFloat(row.cash_balance)
    })) as Portfolio[]

  } catch (error) {
    console.error('Error fetching portfolios:', error)
    throw error
  }
}

// Function to delete a portfolio
export async function deletePortfolio(portfolioId: number, accountId: number): Promise<{ success: boolean, message: string }> {
  try {
    // First verify the portfolio belongs to the account
    const verifyQuery = `
      SELECT id FROM portfolios 
      WHERE id = $1 AND account_id = $2
    `;
    const verifyResult = await executeQuery(verifyQuery, [portfolioId, accountId]);
    
    if (!verifyResult || verifyResult.length === 0) {
      return { 
        success: false, 
        message: "Portfolio not found or you don't have permission to delete it" 
      };
    }
    
    // Use a transaction to ensure data integrity
    await executeQuery('BEGIN', []);
    
    try {
      // Delete related transactions
      await executeQuery(`
        DELETE FROM transactions 
        WHERE portfolio_id = $1
      `, [portfolioId]);
      
      // Delete related performances
      await executeQuery(`
        DELETE FROM performances 
        WHERE portfolio_id = $1
      `, [portfolioId]);
      
      // Delete related assets
      await executeQuery(`
        DELETE FROM assets 
        WHERE portfolio_id = $1
      `, [portfolioId]);
      
      // Finally delete the portfolio itself
      await executeQuery(`
        DELETE FROM portfolios 
        WHERE id = $1
      `, [portfolioId]);
      
      await executeQuery('COMMIT', []);
      
      return { 
        success: true, 
        message: "Portfolio and all related data deleted successfully" 
      };
    } catch (error) {
      await executeQuery('ROLLBACK', []);
      throw error;
    }
  } catch (error) {
    console.error('Error deleting portfolio:', error);
    return { 
      success: false, 
      message: `Error deleting portfolio: ${error instanceof Error ? error.message : 'Unknown error'}` 
    };
  }
}

// Helper function to format currency
export function formatCurrency(value: number | string): string {
  const numValue = typeof value === 'string' ? parseFloat(value) : value
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(numValue)
}

// Helper function to format percentage
export function formatPercentage(value: number | string): string {
  const numValue = typeof value === 'string' ? parseFloat(value) : value
  const sign = numValue >= 0 ? '+' : ''
  return `${sign}${numValue.toFixed(2)}%`
} 