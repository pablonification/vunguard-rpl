import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"

export default function ProductsPage() {
  // Mock data - in a real app, this would come from the database
  const products = [
    {
      id: 1,
      code: "TGF001",
      name: "Tech Growth Fund",
      description: "A fund focused on high-growth technology companies",
      strategy: "Growth",
      riskLevel: "High",
    },
    {
      id: 2,
      code: "GBF002",
      name: "Global Bond Fund",
      description: "A diversified portfolio of government and corporate bonds",
      strategy: "Income",
      riskLevel: "Low",
    },
    {
      id: 3,
      code: "EME003",
      name: "Emerging Markets ETF",
      description: "Exposure to high-growth emerging market economies",
      strategy: "Growth",
      riskLevel: "High",
    },
    {
      id: 4,
      code: "INC004",
      name: "Income Fund",
      description: "Focus on dividend-paying stocks and fixed income",
      strategy: "Income",
      riskLevel: "Medium",
    },
    {
      id: 5,
      code: "HIF005",
      name: "Healthcare Innovation Fund",
      description: "Investing in breakthrough healthcare technologies",
      strategy: "Growth",
      riskLevel: "Medium",
    },
  ]

  return (
    <DashboardLayout requiredRoles={["manager", "analyst", "admin"]}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Products</h1>
          <Link href="/dashboard/products/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" /> Add Product
            </Button>
          </Link>
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
                    <TableCell>{product.strategy}</TableCell>
                    <TableCell>{product.riskLevel}</TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/products/${product.id}`}>
                        <Button variant="ghost" size="sm">
                          View
                        </Button>
                      </Link>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
}
