import { executeQuery } from "@/lib/db"

export interface Portfolio {
  id: number;
  name: string;
  description: string | null;
  total_value: number;
  return_percentage: number;
  asset_count: number;
  last_updated: string;
}

export async function getPortfolios(accountId: number): Promise<Portfolio[]> {
  try {
    // Query to get portfolios with their total value, return, and asset count
    const query = `
      WITH latest_performance AS (
        SELECT 
          portfolio_id,
          value as total_value,
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
        COALESCE(lp.total_value, 0) as total_value,
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
    return (result || []) as Portfolio[]

  } catch (error) {
    console.error('Error fetching portfolios:', error)
    throw error
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