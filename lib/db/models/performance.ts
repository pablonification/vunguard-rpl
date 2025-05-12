import { sql } from "@vercel/postgres";
import { unstable_noStore as noStore } from "next/cache";

export interface PortfolioPerformance {
  id: number;
  name: string;
  currentValue: number;
  initialValue: number;
  returnPercentage: number;
  benchmarkComparison: number;
}

export interface AssetAllocation {
  name: string;
  value: number;
  riskLevel: string;
}

export interface PerformanceOverTime {
  date: string;
  portfolioValue: number;
  benchmarkValue: number;
}

export async function getPortfolioPerformances(accountId: number) {
  noStore();
  try {
    // console.log('Fetching portfolio performances for account:', accountId);
    const result = await sql`
      WITH PortfolioValues AS (
        SELECT 
          p.id,
          p.name,
          COALESCE(SUM(a.quantity * a.current_price), 0) as current_value,
          COALESCE(SUM(a.quantity * a.purchase_price), 0) as initial_value,
          COALESCE(perf.return_percentage, 0) as return_percentage,
          COALESCE(perf.benchmark_comparison, 0) as benchmark_comparison
        FROM portfolios p
        LEFT JOIN assets a ON p.id = a.portfolio_id
        LEFT JOIN performances perf ON p.id = perf.portfolio_id 
          AND perf.asset_id IS NULL -- Ensure we only join portfolio-level latest performance
          AND perf.date = (
            SELECT MAX(sub_perf.date) 
            FROM performances sub_perf
            WHERE sub_perf.portfolio_id = p.id AND sub_perf.asset_id IS NULL
          )
        WHERE p.account_id = ${accountId}
        GROUP BY p.id, p.name, perf.return_percentage, perf.benchmark_comparison
      )
      SELECT 
        id,
        name,
        ROUND(current_value::numeric, 2) as "currentValue",
        ROUND(initial_value::numeric, 2) as "initialValue",
        ROUND(return_percentage::numeric, 2) as "returnPercentage",
        ROUND(benchmark_comparison::numeric, 2) as "benchmarkComparison"
      FROM PortfolioValues
      WHERE current_value > 0 OR initial_value > 0 OR return_percentage <> 0 OR benchmark_comparison <> 0 -- Ensure portfolio has some activity
      ORDER BY current_value DESC;
    `;
    // console.log('Portfolio performances results:', result.rows);
    return result.rows;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch portfolio performances');
  }
}

export async function getAssetAllocations(accountId: number) {
  noStore();
  try {
    const result = await sql`
      WITH TotalValue AS (
        SELECT SUM(a.quantity * a.current_price) as total
        FROM portfolios p
        JOIN assets a ON p.id = a.portfolio_id
        WHERE p.account_id = ${accountId}
      )
      SELECT 
        pr.name,
        SUM(a.quantity * a.current_price) as value,
        pr.risk_level as "riskLevel"
      FROM portfolios p
      JOIN assets a ON p.id = a.portfolio_id
      JOIN products pr ON a.product_id = pr.id
      WHERE p.account_id = ${accountId}
      GROUP BY pr.name, pr.risk_level;
    `;
    return result.rows;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch asset allocations');
  }
}

export async function getPerformanceOverTime(accountId: number) {
  noStore();
  try {
    // console.log('Fetching performance over time for account:', accountId);
    const result = await sql`
      WITH PortfolioPerformances AS (
        SELECT DISTINCT date
        FROM performances
        WHERE portfolio_id IN (
          SELECT id FROM portfolios WHERE account_id = ${accountId}
        )
        AND asset_id IS NULL
        AND date >= '2023-01-01'  -- Ensure we only get data from 2023 onwards
        AND date <= CURRENT_DATE
      ),
      AccountPerformance AS (
        SELECT 
          perf_dates.date as series_date,
          COALESCE(
            SUM(perf.value), 
            0
          ) as total_portfolio_value,
          COALESCE(
            SUM(perf.value * (1 + COALESCE(perf.benchmark_comparison, 0)/100)), 
            0
          ) as benchmark_value
        FROM PortfolioPerformances perf_dates
        LEFT JOIN portfolios p ON p.account_id = ${accountId}
        LEFT JOIN performances perf ON perf.portfolio_id = p.id 
          AND perf.asset_id IS NULL
          AND perf.date = perf_dates.date
        GROUP BY perf_dates.date
      )
      SELECT 
        TO_CHAR(series_date, 'Mon DD, YYYY') as date,
        ROUND(total_portfolio_value::numeric, 2) as "portfolioValue",
        ROUND(benchmark_value::numeric, 2) as "benchmarkValue"
      FROM AccountPerformance
      WHERE total_portfolio_value > 0 OR benchmark_value > 0
      ORDER BY series_date;
    `;
    // console.log('Performance over time results:', result.rows);
    return result.rows;
  } catch (error) {
    console.error('Database Error in getPerformanceOverTime:', error);
    throw new Error('Failed to fetch performance over time');
  }
}

export async function getRiskAllocation(accountId: number) {
  noStore();
  try {
    const result = await sql`
      WITH TotalValue AS (
        SELECT SUM(a.quantity * a.current_price) as total
        FROM portfolios p
        JOIN assets a ON p.id = a.portfolio_id
        WHERE p.account_id = ${accountId}
      )
      SELECT 
        pr.risk_level as name,
        SUM(a.quantity * a.current_price) as value
      FROM portfolios p
      JOIN assets a ON p.id = a.portfolio_id
      JOIN products pr ON a.product_id = pr.id
      WHERE p.account_id = ${accountId}
      GROUP BY pr.risk_level;
    `;
    return result.rows;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch risk allocation');
  }
} 