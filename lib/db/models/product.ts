import { sql } from "@vercel/postgres";
import { unstable_noStore as noStore } from "next/cache";

export interface Product {
  id: number;
  code: string;
  name: string;
  description: string;
  investment_strategy: string;
  risk_level: string;
}

export async function getProducts(): Promise<Product[]> {
  noStore();
  try {
    const result = await sql<Product>`
      SELECT 
        id,
        code,
        name,
        description,
        investment_strategy,
        risk_level
      FROM products
      ORDER BY code ASC;
    `;

    return result.rows;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch products');
  }
}

export async function getProductById(id: number): Promise<Product | null> {
  noStore();
  try {
    const result = await sql<Product>`
      SELECT 
        id,
        code,
        name,
        description,
        investment_strategy,
        risk_level
      FROM products
      WHERE id = ${id};
    `;

    return result.rows[0] || null;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to fetch product');
  }
}

export async function createProduct(data: Omit<Product, 'id'>): Promise<Product> {
  noStore();
  try {
    const result = await sql<Product>`
      INSERT INTO products (
        code,
        name,
        description,
        investment_strategy,
        risk_level
      ) VALUES (
        ${data.code},
        ${data.name},
        ${data.description},
        ${data.investment_strategy},
        ${data.risk_level}
      )
      RETURNING *;
    `;

    return result.rows[0];
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to create product');
  }
}

export async function updateProduct(id: number, data: Partial<Omit<Product, 'id'>>): Promise<Product> {
  noStore();
  try {
    // Handle each field separately to avoid complex dynamic SQL
    // This approach updates one field at a time to avoid WebSocket issues
    
    let updatedProduct = await getProductById(id);
    if (!updatedProduct) {
      throw new Error('Product not found');
    }

    // Update each field one by one
    if (data.code !== undefined) {
      const result = await sql<Product>`
        UPDATE products SET code = ${data.code} WHERE id = ${id} RETURNING *;
      `;
      updatedProduct = result.rows[0];
    }
    
    if (data.name !== undefined) {
      const result = await sql<Product>`
        UPDATE products SET name = ${data.name} WHERE id = ${id} RETURNING *;
      `;
      updatedProduct = result.rows[0];
    }
    
    if (data.description !== undefined) {
      const result = await sql<Product>`
        UPDATE products SET description = ${data.description} WHERE id = ${id} RETURNING *;
      `;
      updatedProduct = result.rows[0];
    }
    
    if (data.investment_strategy !== undefined) {
      const result = await sql<Product>`
        UPDATE products SET investment_strategy = ${data.investment_strategy} WHERE id = ${id} RETURNING *;
      `;
      updatedProduct = result.rows[0];
    }
    
    if (data.risk_level !== undefined) {
      const result = await sql<Product>`
        UPDATE products SET risk_level = ${data.risk_level} WHERE id = ${id} RETURNING *;
      `;
      updatedProduct = result.rows[0];
    }
    
    return updatedProduct;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to update product');
  }
}

export async function deleteProduct(id: number): Promise<void> {
  noStore();
  try {
    await sql`
      DELETE FROM products
      WHERE id = ${id};
    `;
  } catch (error) {
    console.error('Database Error:', error);
    throw new Error('Failed to delete product');
  }
} 