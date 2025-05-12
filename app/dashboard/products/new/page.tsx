import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { requireAuth } from "@/lib/auth"
import { NewProductForm } from "./new-product-form"

export default async function NewProductPage() {
  // Get current user and verify authorization
  const session = await requireAuth()
  const role = session.role as string
  
  if (!["manager", "admin"].includes(role)) {
    throw new Error("Unauthorized")
  }

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Add New Product</h1>
        <p className="text-muted-foreground">Create a new investment product in the system.</p>

        <Card>
          <CardHeader>
            <CardTitle>Product Details</CardTitle>
            <CardDescription>Fill in the information for the new product.</CardDescription>
          </CardHeader>
          <CardContent>
            <NewProductForm />
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
} 