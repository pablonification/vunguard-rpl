import { sql } from '@vercel/postgres';
import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';

export async function GET() {
  try {
    const session = await getSession();
    if (!session) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    // Only allow managers and admins to access this endpoint
    if (!['manager', 'admin'].includes(session.role)) {
      return new NextResponse('Forbidden', { status: 403 });
    }

    // Get all investors
    const result = await sql`
      SELECT 
        id,
        full_name,
        email
      FROM accounts
      WHERE role = 'investor'
      ORDER BY full_name
    `;

    return NextResponse.json(result.rows);
  } catch (error) {
    console.error('Database Error:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 