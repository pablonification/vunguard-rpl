import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';
import { neon } from '@neondatabase/serverless';
import { unstable_noStore as noStore } from 'next/cache';

// GET /api/investors
export async function GET() {
  try {
    noStore();
    const session = await getSession();
    
    // Check if user is authenticated and has appropriate role
    // Allow managers, admins, and analysts to access this endpoint
    if (!session || !['manager', 'admin', 'analyst'].includes(session.role as string)) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    const connectionString = process.env.POSTGRES_URL!;
    const sql = neon(connectionString);
    
    // Get all accounts with investor role
    const result = await sql`
      SELECT id, username as full_name, email
      FROM accounts
      WHERE role = 'investor'
      ORDER BY username
    `;

    return NextResponse.json(result);
  } catch (error) {
    console.error('Error in GET /api/investors:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 