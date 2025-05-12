import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getProductById } from "@/lib/db/models/product"
import { requireAuth } from "@/lib/auth"
import { notFound } from "next/navigation"
import { EditProductForm } from "./edit-product-form"

interface EditProductPageProps {
  params: {
    id: string
  }
}

export default async function EditProductPage({ params }: EditProductPageProps) {
  // Get current user and verify authorization
  const session = await requireAuth()
  const role = session.role as string
  
  if (!["manager", "admin"].includes(role)) {
    throw new Error("Unauthorized")
  }

  // Ensure params.id is properly awaited and parsed
  const productId = parseInt(params.id, 10)
  
  // Fetch product details
  const product = await getProductById(productId)
  
  if (!product) {
    notFound()
  }

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Edit Product</h1>
        <p className="text-muted-foreground">Update the details of this investment product.</p>

        <Card>
          <CardHeader>
            <CardTitle>Edit {product.name}</CardTitle>
            <CardDescription>Make changes to the product information below.</CardDescription>
          </CardHeader>
          <CardContent>
            <EditProductForm product={product} />
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
} 