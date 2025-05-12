import { NextResponse } from 'next/server';
import { getSession } from '@/lib/auth';
import { getProducts, getProductById } from '@/lib/db/models/product';

// GET /api/products - Get all products
export async function GET(request: Request) {
  try {
    const session = await getSession();
    if (!session) {
      return new NextResponse('Unauthorized', { status: 401 });
    }

    // Check for product ID query parameter
    const { searchParams } = new URL(request.url);
    const id = searchParams.get('id');

    if (id) {
      // Get a specific product
      const productId = parseInt(id);
      const product = await getProductById(productId);
      
      if (!product) {
        return new NextResponse('Product not found', { status: 404 });
      }
      
      return NextResponse.json(product);
    } else {
      // Get all products
      const products = await getProducts();
      return NextResponse.json(products);
    }
  } catch (error) {
    console.error('Error in GET /api/products:', error);
    return new NextResponse('Internal Server Error', { status: 500 });
  }
} 