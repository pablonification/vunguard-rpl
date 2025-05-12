import { sql } from '@vercel/postgres';
import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';

export async function GET() {
  try {
    const session = await getSession();
    if (!session) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    const accountId = session.id;
    // console.log('Fetching portfolios for account:', accountId);

    // Get portfolios for the logged-in user with their assets and product information
    const result = await sql`
      SELECT 
        p.id as portfolio_id,
        p.name as portfolio_name,
        a.id as asset_id,
        a.quantity as asset_quantity,
        pr.id as product_id,
        pr.name as product_name
      FROM portfolios p
      LEFT JOIN assets a ON p.id = a.portfolio_id
      LEFT JOIN products pr ON a.product_id = pr.id
      WHERE p.account_id = ${accountId}
      ORDER BY p.name, pr.name
    `;

    // console.log('Query result rows:', result.rows.length);

    // Transform the flat results into a nested structure
    const portfoliosMap = new Map();

    result.rows.forEach(row => {
      if (!portfoliosMap.has(row.portfolio_id)) {
        portfoliosMap.set(row.portfolio_id, {
          id: row.portfolio_id,
          name: row.portfolio_name,
          assets: [],
        });
      }

      const portfolio = portfoliosMap.get(row.portfolio_id);

      if (row.asset_id) {
        portfolio.assets.push({
          id: row.asset_id,
          productId: row.product_id,
          productName: row.product_name,
          quantity: parseFloat(row.asset_quantity),
        });
      }
    });

    const portfolios = Array.from(portfoliosMap.values());
    // console.log('Transformed portfolios:', portfolios);

    return NextResponse.json(portfolios);
  } catch (error) {
    console.error('Database Error:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 