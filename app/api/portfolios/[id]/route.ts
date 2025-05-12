import { NextRequest, NextResponse } from 'next/server';
import { requireAuth } from '@/lib/auth';
import { deletePortfolio } from '@/lib/db/models/portfolio';
import { executeQuery } from '@/lib/db';

export async function DELETE(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    // Verify authentication and get user info
    const session = await requireAuth(['investor', 'manager']);
    
    // No need to check roles again since requireAuth already does this
    // and would redirect to /unauthorized if role was invalid
    
    const portfolioId = parseInt(params.id, 10);
    if (isNaN(portfolioId)) {
      return NextResponse.json(
        { error: 'Invalid portfolio ID' },
        { status: 400 }
      );
    }

    // For managers, get the actual account_id from the portfolio
    if (session.role === 'manager') {
      // Managers can delete any portfolio (the deletePortfolio function will still verify access)
      const portfolioInfo = await executeQuery(
        'SELECT account_id FROM portfolios WHERE id = $1',
        [portfolioId]
      );
      
      if (!portfolioInfo || portfolioInfo.length === 0) {
        return NextResponse.json(
          { error: 'Portfolio not found' },
          { status: 404 }
        );
      }
      
      // Delete using the portfolio's account_id
      const result = await deletePortfolio(portfolioId, portfolioInfo[0].account_id);
      
      if (result.success) {
        return NextResponse.json(
          { message: result.message },
          { status: 200 }
        );
      } else {
        return NextResponse.json(
          { error: result.message },
          { status: 400 }
        );
      }
    } else {
      // For investors, use their own account ID
      const accountId = typeof session.id === 'string' 
        ? parseInt(session.id, 10) 
        : Number(session.id || 0);
      
      // Delete the portfolio
      const result = await deletePortfolio(portfolioId, accountId);
      
      if (result.success) {
        return NextResponse.json(
          { message: result.message },
          { status: 200 }
        );
      } else {
        return NextResponse.json(
          { error: result.message },
          { status: 400 }
        );
      }
    }
  } catch (error) {
    console.error('Error deleting portfolio:', error);
    return NextResponse.json(
      { error: 'Failed to delete portfolio' },
      { status: 500 }
    );
  }
} 