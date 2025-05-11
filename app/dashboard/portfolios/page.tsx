import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"

export default function PortfoliosPage() {
  // Mock data - in a real app, this would come from the database
  const portfolios = [
    {
      id: 1,
      name: "Retirement Fund",
      totalValue: "$125,000.00",
      return: "+12.5%",
      assetCount: 8,
      lastUpdated: "2023-05-10",
    },
    {
      id: 2,
      name: "Education Fund",
      totalValue: "$45,000.00",
      return: "+8.2%",
      assetCount: 5,
      lastUpdated: "2023-05-09",
    },
    {
      id: 3,
      name: "Emergency Fund",
      totalValue: "$15,000.00",
      return: "+3.1%",
      assetCount: 3,
      lastUpdated: "2023-05-08",
    },
    {
      id: 4,
      name: "Growth Portfolio",
      totalValue: "$78,000.00",
      return: "+18.7%",
      assetCount: 12,
      lastUpdated: "2023-05-07",
    },
    {
      id: 5,
      name: "Income Portfolio",
      totalValue: "$92,000.00",
      return: "+6.4%",
      assetCount: 7,
      lastUpdated: "2023-05-06",
    },
  ]

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Portfolios</h1>
          <Link href="/dashboard/portfolios/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" /> Create Portfolio
            </Button>
          </Link>
        </div>
        <p className="text-muted-foreground">Manage your investment portfolios and track their performance.</p>

        <Card>
          <CardHeader>
            <CardTitle>All Portfolios</CardTitle>
            <CardDescription>A list of all your investment portfolios.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Total Value</TableHead>
                  <TableHead>Return</TableHead>
                  <TableHead className="hidden md:table-cell">Assets</TableHead>
                  <TableHead className="hidden md:table-cell">Last Updated</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {portfolios.map((portfolio) => (
                  <TableRow key={portfolio.id}>
                    <TableCell className="font-medium">{portfolio.name}</TableCell>
                    <TableCell>{portfolio.totalValue}</TableCell>
                    <TableCell className="text-green-600">{portfolio.return}</TableCell>
                    <TableCell className="hidden md:table-cell">{portfolio.assetCount}</TableCell>
                    <TableCell className="hidden md:table-cell">{portfolio.lastUpdated}</TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/portfolios/${portfolio.id}`}>
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
