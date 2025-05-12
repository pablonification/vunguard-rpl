import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';
import { createRecommendation, getRecommendations } from '@/lib/db/models/recommendation';
import { z } from 'zod';

// GET /api/recommendations
export async function GET(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    // Validate user role - only analysts and managers can access recommendations
    if (!['analyst', 'manager'].includes(session.role)) {
      return new NextResponse('Forbidden', { status: 403 });
    }

    // Parse query parameters
    const { searchParams } = new URL(request.url);
    const filters: any = {};

    // Add filters based on role
    if (session.role === 'analyst') {
      // Analysts can only see their own recommendations
      filters.analystId = session.id;
    }
    // Managers can see all recommendations

    if (searchParams.has('productId')) {
      filters.productId = parseInt(searchParams.get('productId')!);
    }
    if (searchParams.has('status')) {
      filters.status = searchParams.get('status');
    }
    if (searchParams.has('timeframe')) {
      filters.timeframe = searchParams.get('timeframe');
    }

    const recommendations = await getRecommendations(filters);
    return NextResponse.json(recommendations);
  } catch (error) {
    console.error('Error in GET /api/recommendations:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
}

// POST /api/recommendations
export async function POST(request: Request) {
  try {
    console.log('POST /api/recommendations received');
    const session = await getSession();
    console.log('Session:', session);
    
    // Validate user role - only analysts can create recommendations
    if (!session || session.role !== 'analyst') {
      console.log('Unauthorized attempt to create recommendation:', session?.role);
      return new NextResponse(JSON.stringify({ message: 'Unauthorized: Only analysts can create recommendations' }), { 
        status: 401,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const body = await request.json();
    console.log('Request body:', body);
    
    // Validate request body has required fields
    if (!body.productId) {
      console.log('Missing required field: productId');
      return new NextResponse(JSON.stringify({ message: 'Missing required field: productId' }), { 
        status: 400,
        headers: { 'Content-Type': 'application/json' }
      });
    }
    
    // Ensure analystId matches the session id
    if (body.analystId && body.analystId !== session.id) {
      console.log('Analyst ID mismatch:', body.analystId, session.id);
      return new NextResponse(JSON.stringify({ message: 'Forbidden: Cannot create recommendations for other analysts' }), { 
        status: 403,
        headers: { 'Content-Type': 'application/json' }
      });
    }
    
    try {
      console.log('Calling createRecommendation with:', { ...body, analystId: session.id });
      const recommendation = await createRecommendation({
        ...body,
        analystId: session.id,
      });
      
      console.log('Recommendation created:', recommendation);
      return NextResponse.json(recommendation);
    } catch (createError) {
      console.error('Database error creating recommendation:', createError);
      return new NextResponse(JSON.stringify({ 
        message: 'Failed to create recommendation in database',
        error: createError instanceof Error ? createError.message : String(createError)
      }), { 
        status: 500,
        headers: { 'Content-Type': 'application/json' }
      });
    }
  } catch (error) {
    console.error('Error in POST /api/recommendations:', error);
    return new NextResponse(JSON.stringify({ 
      message: 'Internal Server Error',
      error: error instanceof Error ? error.message : String(error)
    }), { 
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    });
  }
} 