import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"
import { getPortfolios, formatCurrency, formatPercentage, Portfolio } from "@/lib/db/models/portfolio"
import { requireAuth } from "@/lib/auth"
import { executeQuery } from "@/lib/db"

export default async function PortfoliosPage() {
  // Get current user's session and account
  const session = await requireAuth()
  const accountQuery = "SELECT id FROM accounts WHERE id = $1"
  const accountResult = await executeQuery(accountQuery, [session.id])
  const accountId = accountResult[0]?.id

  if (!accountId) {
    throw new Error('Account not found')
  }

  // Fetch actual portfolio data from the database
  const portfolios = await getPortfolios(accountId)

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
            <CardTitle>Your Portfolios</CardTitle>
            <CardDescription>Manage and monitor your investment portfolios.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Asset Value</TableHead>
                  <TableHead>Cash Balance</TableHead>
                  <TableHead>Total Value</TableHead>
                  <TableHead>Return</TableHead>
                  <TableHead className="hidden md:table-cell">Assets</TableHead>
                  <TableHead className="hidden md:table-cell">Last Updated</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {portfolios.map((portfolio: Portfolio) => (
                  <TableRow key={portfolio.id}>
                    <TableCell className="font-medium">
                      {portfolio.name}
                      {portfolio.description && (
                        <p className="text-sm text-muted-foreground">{portfolio.description}</p>
                      )}
                    </TableCell>
                    <TableCell>{formatCurrency(portfolio.total_value - portfolio.cash_balance)}</TableCell>
                    <TableCell>{formatCurrency(portfolio.cash_balance)}</TableCell>
                    <TableCell className="font-medium">{formatCurrency(portfolio.total_value)}</TableCell>
                    <TableCell className={portfolio.return_percentage >= 0 ? "text-green-600" : "text-red-600"}>
                      {formatPercentage(portfolio.return_percentage)}
                    </TableCell>
                    <TableCell className="hidden md:table-cell">{portfolio.asset_count}</TableCell>
                    <TableCell className="hidden md:table-cell">
                      {new Date(portfolio.last_updated).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/portfolios/${portfolio.id}`}>
                        <Button variant="ghost" size="sm">
                          View
                        </Button>
                      </Link>
                    </TableCell>
                  </TableRow>
                ))}
                {portfolios.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center text-muted-foreground">
                      No portfolios found. Create your first portfolio to get started.
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
