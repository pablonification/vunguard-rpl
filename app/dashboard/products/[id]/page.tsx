import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowLeft, Pencil } from "lucide-react"
import Link from "next/link"
import { getProductById } from "@/lib/db/models/product"
import { requireAuth } from "@/lib/auth"
import { notFound } from "next/navigation"
import { DeleteButton } from "./delete-button"

interface ProductPageProps {
  params: {
    id: string
  }
}

export default async function ProductPage({ params }: ProductPageProps) {
  // Get current user and verify authorization with explicit roles
  const session = await requireAuth(["manager", "analyst"])

  // Ensure params.id is properly awaited and parsed
  const productId = parseInt(params.id, 10)
  
  // Fetch product details
  const product = await getProductById(productId)
  
  if (!product) {
    notFound()
  }

  // Helper function to format risk level with color
  const getRiskLevelColor = (level: string) => {
    switch (level.toLowerCase()) {
      case 'high':
        return 'text-red-600 dark:text-red-400'
      case 'medium':
        return 'text-yellow-600 dark:text-yellow-400'
      case 'low':
        return 'text-green-600 dark:text-green-400'
      default:
        return ''
    }
  }

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link href="/dashboard/products">
              <Button variant="ghost" size="icon">
                <ArrowLeft className="h-4 w-4" />
              </Button>
            </Link>
            <h1 className="text-3xl font-bold tracking-tight">{product.name}</h1>
          </div>
          {/* Only show edit/delete buttons for managers */}
          {session.role === "manager" && (
            <div className="flex gap-2">
              <Link href={`/dashboard/products/${product.id}/edit`}>
                <Button variant="outline">
                  <Pencil className="mr-2 h-4 w-4" />
                  Edit Product
                </Button>
              </Link>
              <DeleteButton productId={product.id} productName={product.name} />
            </div>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Product Details</CardTitle>
            <CardDescription>Detailed information about this investment product.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Product Code</h3>
                <p className="mt-1 text-sm">{product.code}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Risk Level</h3>
                <p className={`mt-1 text-sm font-medium ${getRiskLevelColor(product.risk_level)}`}>
                  {product.risk_level}
                </p>
              </div>
            </div>

            <div>
              <h3 className="text-sm font-medium text-muted-foreground">Description</h3>
              <p className="mt-1 text-sm">{product.description}</p>
            </div>

            <div>
              <h3 className="text-sm font-medium text-muted-foreground">Investment Strategy</h3>
              <p className="mt-1 text-sm">{product.investment_strategy}</p>
            </div>
          </CardContent>
        </Card>

        {/* Additional sections can be added here, such as:
        - Performance metrics
        - Associated portfolios
        - Historical data
        - etc. */}
      </div>
    </DashboardLayout>
  )
} 