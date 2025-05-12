import { sql } from '@vercel/postgres';
import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';

export async function GET(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    // Get userId from query params
    const { searchParams } = new URL(request.url);
    const userId = searchParams.get('userId');

    // If no userId provided and user is an investor, use their own id
    // If no userId provided and user is manager/admin, return error
    let accountId = session.id;
    if (session.role === 'investor') {
      if (userId && userId !== session.id.toString()) {
        return new NextResponse('Forbidden', { status: 403 });
      }
    } else if (['manager', 'admin'].includes(session.role)) {
      if (!userId) {
        return new NextResponse('UserId is required for managers and admins', { status: 400 });
      }
      accountId = parseInt(userId);
    }

    // Get portfolios for the specified user with their assets and product information
    const result = await sql`
      SELECT 
        p.id as portfolio_id,
        p.name as portfolio_name,
        p.cash_balance,
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

    // Transform the flat results into a nested structure
    const portfoliosMap = new Map();

    result.rows.forEach(row => {
      if (!portfoliosMap.has(row.portfolio_id)) {
        portfoliosMap.set(row.portfolio_id, {
          id: row.portfolio_id,
          name: row.portfolio_name,
          cashBalance: parseFloat(row.cash_balance || 0),
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

    return NextResponse.json(portfolios);
  } catch (error) {
    console.error('Database Error:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 