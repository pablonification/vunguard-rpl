import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';
import { updateRecommendationStatus } from '@/lib/db/models/recommendation';

// PATCH /api/recommendations/:id
export async function PATCH(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    // Ensure params is resolved before accessing properties
    const id = params?.id;

    const session = await getSession();
    
    // Validate user role - only managers can update recommendation status
    if (!session || session.role !== 'manager') {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    const body = await request.json();
    const { status } = body;

    if (!status || !id) {
      return new NextResponse('Missing required fields', { status: 400 });
    }

    const recommendationId = parseInt(id);
    if (isNaN(recommendationId)) {
      return new NextResponse('Invalid recommendation ID', { status: 400 });
    }
    
    // Convert session.id to number if it's a string
    const managerId = typeof session.id === 'string' ? parseInt(session.id) : Number(session.id);

    const recommendation = await updateRecommendationStatus(
      recommendationId,
      status,
      managerId
    );

    return NextResponse.json(recommendation);
  } catch (error) {
    console.error('Error in PATCH /api/recommendations/:id:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 