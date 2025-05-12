import { executeQuery } from "@/lib/db"
import { formatCurrency, formatPercentage } from "@/lib/db/models/portfolio"

export interface DashboardSummary {
  totalValue: number;
  totalReturn: number;
  activeProducts: number;
  pendingTransactions: number;
  monthlyChange: number;
}

export interface PerformanceData {
  date: string;
  portfolioValue: number;
  benchmarkValue: number;
}

export interface AssetAllocation {
  name: string;
  value: number;
  riskLevel: string;
}

export interface RecentTransaction {
  id: number;
  transactionType: string;
  productName: string;
  date: string;
  amount: number;
}

export interface TopPerformingProduct {
  name: string;
  riskLevel: string;
  returnPercentage: number;
}

export async function getDashboardSummary(accountId: number): Promise<DashboardSummary> {
  try {
    // Get total portfolio value and return
    const valueSummaryQuery = `
      WITH latest_performance AS (
        SELECT 
          p.portfolio_id,
          p.value,
          p.return_percentage
        FROM performances p
        INNER JOIN portfolios pf ON p.portfolio_id = pf.id
        WHERE p.asset_id IS NULL 
        AND pf.account_id = $1
        AND p.date = (
          SELECT MAX(date)
          FROM performances p2
          WHERE p2.portfolio_id = p.portfolio_id
          AND p2.asset_id IS NULL
        )
      ),
      month_ago_performance AS (
        SELECT 
          p.portfolio_id,
          p.value
        FROM performances p
        INNER JOIN portfolios pf ON p.portfolio_id = pf.id
        WHERE p.asset_id IS NULL 
        AND pf.account_id = $1
        AND p.date = (
          SELECT MAX(date)
          FROM performances p2
          WHERE p2.portfolio_id = p.portfolio_id
          AND p2.asset_id IS NULL
          AND p2.date <= NOW() - INTERVAL '1 month'
        )
      )
      SELECT 
        COALESCE(SUM(lp.value), 0) as total_value,
        COALESCE(AVG(lp.return_percentage), 0) as avg_return,
        COALESCE(SUM(lp.value) - SUM(mp.value), 0) as value_change,
        CASE 
          WHEN SUM(mp.value) = 0 THEN 0
          ELSE ((SUM(lp.value) - SUM(mp.value)) / SUM(mp.value) * 100)
        END as month_change_percent
      FROM latest_performance lp
      LEFT JOIN month_ago_performance mp ON lp.portfolio_id = mp.portfolio_id
    `;

    // Get count of active products
    const productsQuery = `
      SELECT COUNT(DISTINCT p.id) as active_products
      FROM assets a
      JOIN portfolios p ON a.portfolio_id = p.id
      WHERE p.account_id = $1
    `;

    // Get count of pending transactions
    const transactionsQuery = `
      SELECT COUNT(*) as pending_transactions
      FROM transactions t
      JOIN portfolios p ON t.portfolio_id = p.id
      WHERE p.account_id = $1
      AND t.transaction_date >= NOW() - INTERVAL '1 month'
    `;

    const [valueSummary, productsResult, transactionsResult] = await Promise.all([
      executeQuery(valueSummaryQuery, [accountId]),
      executeQuery(productsQuery, [accountId]),
      executeQuery(transactionsQuery, [accountId])
    ]);

    return {
      totalValue: parseFloat(valueSummary[0]?.total_value || '0'),
      totalReturn: parseFloat(valueSummary[0]?.avg_return || '0'),
      activeProducts: parseInt(productsResult[0]?.active_products || '0'),
      pendingTransactions: parseInt(transactionsResult[0]?.pending_transactions || '0'),
      monthlyChange: parseFloat(valueSummary[0]?.month_change_percent || '0')
    };
  } catch (error) {
    console.error('Error fetching dashboard summary:', error);
    throw error;
  }
}

export async function getPerformanceOverTime(accountId: number): Promise<PerformanceData[]> {
  try {
    const query = `
      WITH portfolio_dates AS (
        SELECT DISTINCT date
        FROM performances p
        JOIN portfolios pf ON p.portfolio_id = pf.id
        WHERE pf.account_id = $1
        AND p.asset_id IS NULL
        ORDER BY date
      ),
      daily_values AS (
        SELECT 
          pd.date,
          SUM(p.value) as portfolio_value,
          SUM(p.value * (1 - p.benchmark_comparison/100)) as benchmark_value
        FROM portfolio_dates pd
        JOIN performances p ON pd.date = p.date
        JOIN portfolios pf ON p.portfolio_id = pf.id
        WHERE pf.account_id = $1
        AND p.asset_id IS NULL
        GROUP BY pd.date
        ORDER BY pd.date
      )
      SELECT 
        TO_CHAR(date, 'YYYY-MM-DD') as date,
        portfolio_value as "portfolioValue",
        benchmark_value as "benchmarkValue"
      FROM daily_values
      ORDER BY date
    `;

    const result = await executeQuery(query, [accountId]);
    return result.map(row => ({
      date: row.date,
      portfolioValue: parseFloat(row.portfolioValue || '0'),
      benchmarkValue: parseFloat(row.benchmarkValue || '0')
    }));
  } catch (error) {
    console.error('Error fetching performance over time:', error);
    throw error;
  }
}

export async function getAssetAllocation(accountId: number): Promise<AssetAllocation[]> {
  try {
    const query = `
      SELECT 
        p.name,
        SUM(a.current_price * a.quantity) as value,
        p.risk_level as "riskLevel"
      FROM assets a
      JOIN portfolios pf ON a.portfolio_id = pf.id
      JOIN products p ON a.product_id = p.id
      WHERE pf.account_id = $1
      GROUP BY p.name, p.risk_level
      ORDER BY value DESC
    `;

    const result = await executeQuery(query, [accountId]);
    return result.map(row => ({
      name: row.name,
      value: parseFloat(row.value || '0'),
      riskLevel: row.riskLevel
    }));
  } catch (error) {
    console.error('Error fetching asset allocation:', error);
    throw error;
  }
}

export async function getRecentTransactions(accountId: number): Promise<RecentTransaction[]> {
  try {
    const query = `
      SELECT 
        t.id,
        t.transaction_type as "transactionType",
        p.name as "productName",
        TO_CHAR(t.transaction_date, 'YYYY-MM-DD') as date,
        CASE
          WHEN t.transaction_type = 'buy' THEN t.price * t.quantity
          WHEN t.transaction_type = 'sell' THEN -1 * t.price * t.quantity
          ELSE t.price * t.quantity
        END as amount
      FROM transactions t
      JOIN portfolios pf ON t.portfolio_id = pf.id
      JOIN assets a ON t.asset_id = a.id
      JOIN products p ON a.product_id = p.id
      WHERE pf.account_id = $1
      ORDER BY t.transaction_date DESC
      LIMIT 4
    `;

    const result = await executeQuery(query, [accountId]);
    return result.map(row => ({
      id: row.id,
      transactionType: row.transactionType,
      productName: row.productName,
      date: row.date,
      amount: parseFloat(row.amount || '0')
    }));
  } catch (error) {
    console.error('Error fetching recent transactions:', error);
    throw error;
  }
}

export async function getTopPerformingProducts(accountId: number): Promise<TopPerformingProduct[]> {
  try {
    const query = `
      WITH asset_performance AS (
        SELECT 
          p.id as product_id,
          p.name,
          p.risk_level as "riskLevel",
          ((a.current_price - a.purchase_price) / a.purchase_price * 100) as return_percentage
        FROM assets a
        JOIN portfolios pf ON a.portfolio_id = pf.id
        JOIN products p ON a.product_id = p.id
        WHERE pf.account_id = $1
      )
      SELECT 
        name,
        "riskLevel",
        AVG(return_percentage) as "returnPercentage"
      FROM asset_performance
      GROUP BY name, "riskLevel"
      ORDER BY "returnPercentage" DESC
      LIMIT 4
    `;

    const result = await executeQuery(query, [accountId]);
    return result.map(row => ({
      name: row.name,
      riskLevel: row.riskLevel,
      returnPercentage: parseFloat(row.returnPercentage || '0')
    }));
  } catch (error) {
    console.error('Error fetching top performing products:', error);
    throw error;
  }
} 