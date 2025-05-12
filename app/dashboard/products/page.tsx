import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"
import { getProducts } from "@/lib/db/models/product"
import { requireAuth } from "@/lib/auth"

export default async function ProductsPage() {
  // Get current user and verify authorization with explicit roles
  const session = await requireAuth(["manager", "analyst"])
  
  // Fetch products from database
  const products = await getProducts()

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Products</h1>
          {/* Only show Add Product button for managers */}
          {session.role === "manager" && (
            <Link href="/dashboard/products/new">
              <Button>
                <Plus className="mr-2 h-4 w-4" /> Add Product
              </Button>
            </Link>
          )}
        </div>
        <p className="text-muted-foreground">Manage your investment products and their details.</p>

        <Card>
          <CardHeader>
            <CardTitle>All Products</CardTitle>
            <CardDescription>A list of all investment products in the system.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Code</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead className="hidden md:table-cell">Description</TableHead>
                  <TableHead>Strategy</TableHead>
                  <TableHead>Risk Level</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell className="font-medium">{product.code}</TableCell>
                    <TableCell>{product.name}</TableCell>
                    <TableCell className="hidden md:table-cell">{product.description}</TableCell>
                    <TableCell>{product.investment_strategy}</TableCell>
                    <TableCell>{product.risk_level}</TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/products/${product.id}`}>
                        <Button variant="ghost" size="sm">
                          View
                        </Button>
                      </Link>
                    </TableCell>
                  </TableRow>
                ))}
                {products.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center text-muted-foreground">
                      No products found.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
