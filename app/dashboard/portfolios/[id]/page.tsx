import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { formatCurrency, formatPercentage } from "@/lib/db/models/portfolio"
import { executeQuery } from "@/lib/db"
import { requireAuth } from "@/lib/auth"
import { notFound } from "next/navigation"

// Add a function to check if a portfolio exists and get its owner's account ID
async function getPortfolioOwner(portfolioId: number) {
  const query = `
    SELECT account_id FROM portfolios WHERE id = $1
  `
  const result = await executeQuery(query, [portfolioId])
  return result[0]?.account_id
}

async function getPortfolioDetails(portfolioId: number, accountId: number) {
  const query = `
    WITH latest_performance AS (
      SELECT 
        portfolio_id,
        value as total_value,
        return_percentage,
        benchmark_comparison,
        date as last_updated
      FROM performances
      WHERE asset_id IS NULL
      AND date = (
        SELECT MAX(date)
        FROM performances p2
        WHERE p2.portfolio_id = performances.portfolio_id
        AND p2.asset_id IS NULL
      )
    )
    SELECT 
      p.id,
      p.name,
      p.description,
      COALESCE(lp.total_value, 0) as total_value,
      COALESCE(lp.return_percentage, 0) as return_percentage,
      COALESCE(lp.benchmark_comparison, 0) as benchmark_comparison,
      COALESCE(lp.last_updated, p.created_at) as last_updated,
      p.cash_balance
    FROM portfolios p
    LEFT JOIN latest_performance lp ON p.id = lp.portfolio_id
    WHERE p.id = $1 AND p.account_id = $2
  `
  const result = await executeQuery(query, [portfolioId, accountId])
  return result[0]
}

async function getPortfolioAssets(portfolioId: number) {
  const query = `
    SELECT 
      a.id,
      p.name as product_name,
      p.code as product_code,
      p.risk_level,
      a.quantity,
      a.purchase_price,
      a.current_price,
      a.allocation_percentage,
      (a.current_price - a.purchase_price) * a.quantity as total_gain_loss,
      ((a.current_price - a.purchase_price) / a.purchase_price * 100) as return_percentage
    FROM assets a
    JOIN products p ON a.product_id = p.id
    WHERE a.portfolio_id = $1
    ORDER BY a.allocation_percentage DESC
  `
  const result = await executeQuery(query, [portfolioId])
  return result
}

// Get account name for displaying title
async function getAccountName(accountId: number) {
  const query = `SELECT full_name FROM accounts WHERE id = $1`
  const result = await executeQuery(query, [accountId])
  return result[0]?.full_name
}

interface PortfolioPageProps {
  params: { id: string };
  searchParams: { investorId?: string };
}

export default async function PortfolioPage({ params, searchParams }: PortfolioPageProps) {
  const session = await requireAuth(['investor', 'manager', 'analyst'])
  
  // Determine if user is authorized to select investors
  const canViewOtherPortfolios = ['manager', 'analyst'].includes(session.role as string)
  
  const portfolioId = parseInt(params.id)
  if (isNaN(portfolioId)) {
    return notFound()
  }

  // First check if the portfolio exists and who it belongs to
  const portfolioOwnerAccountId = await getPortfolioOwner(portfolioId)
  if (!portfolioOwnerAccountId) {
    return notFound()
  }

  // Determine which account's portfolio to show
  let targetAccountId: number
  
  // If user is manager/analyst and an investorId is in the URL
  if (canViewOtherPortfolios && searchParams.investorId) {
    const requestedInvestorId = parseInt(searchParams.investorId, 10)
    
    // Verify the portfolio actually belongs to the requested investor
    if (!isNaN(requestedInvestorId) && requestedInvestorId === portfolioOwnerAccountId) {
      targetAccountId = requestedInvestorId
    } else {
      // If investorId in URL doesn't match the portfolio owner, return not found
      return notFound()
    }
  } else {
    // For regular users or if no investorId in URL, check if the portfolio belongs to them
    const userAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : Number(session.id || 0)
    
    if (portfolioOwnerAccountId === userAccountId) {
      targetAccountId = userAccountId
    } else if (canViewOtherPortfolios) {
      // For managers/analysts, allow viewing any portfolio even without investorId in URL
      targetAccountId = portfolioOwnerAccountId
    } else {
      // For investors who don't own this portfolio
      return notFound()
    }
  }

  const [portfolio, assets] = await Promise.all([
    getPortfolioDetails(portfolioId, targetAccountId),
    getPortfolioAssets(portfolioId)
  ])

  if (!portfolio) {
    return notFound()
  }

  // Get account name if viewing another investor's portfolio
  let investorName = null
  if (canViewOtherPortfolios && targetAccountId !== Number(session.id)) {
    investorName = await getAccountName(targetAccountId)
  }

  // Calculate total gain/loss
  const totalGainLoss = assets.reduce((sum: number, asset: any) => sum + asset.total_gain_loss, 0)

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{portfolio.name}</h1>
          {investorName && (
            <p className="text-sm text-muted-foreground mt-1">Portfolio owned by {investorName}</p>
          )}
          {portfolio.description && (
            <p className="mt-2 text-muted-foreground">{portfolio.description}</p>
          )}
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Asset Value</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(portfolio.total_value - portfolio.cash_balance)}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Cash Balance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(portfolio.cash_balance)}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Value</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(portfolio.total_value)}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Return</CardTitle>
            </CardHeader>
            <CardContent>
              <div className={`text-2xl font-bold ${portfolio.return_percentage >= 0 ? "text-green-600" : "text-red-600"}`}>
                {formatPercentage(portfolio.return_percentage)}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">vs. Benchmark</CardTitle>
            </CardHeader>
            <CardContent>
              <div className={`text-2xl font-bold ${portfolio.benchmark_comparison >= 0 ? "text-green-600" : "text-red-600"}`}>
                {formatPercentage(portfolio.benchmark_comparison)}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Gain/Loss</CardTitle>
            </CardHeader>
            <CardContent>
              <div className={`text-2xl font-bold ${totalGainLoss >= 0 ? "text-green-600" : "text-red-600"}`}>
                {formatCurrency(totalGainLoss)}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Assets</CardTitle>
            <CardDescription>Current allocation and performance of assets in this portfolio.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Risk Level</TableHead>
                  <TableHead>Quantity</TableHead>
                  <TableHead>Current Price</TableHead>
                  <TableHead>Total Value</TableHead>
                  <TableHead>Allocation</TableHead>
                  <TableHead>Return</TableHead>
                  <TableHead>Gain/Loss</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {assets.map((asset: any) => (
                  <TableRow key={asset.id}>
                    <TableCell>
                      <div className="font-medium">{asset.product_name}</div>
                      <div className="text-sm text-muted-foreground">{asset.product_code}</div>
                    </TableCell>
                    <TableCell>
                      <div className={`inline-block px-2 py-1 text-xs font-medium rounded-full
                        ${asset.risk_level === 'High' ? 'bg-red-100 text-red-700' :
                          asset.risk_level === 'Medium' ? 'bg-yellow-100 text-yellow-700' :
                          'bg-green-100 text-green-700'}`}>
                        {asset.risk_level}
                      </div>
                    </TableCell>
                    <TableCell>{asset.quantity}</TableCell>
                    <TableCell>{formatCurrency(asset.current_price)}</TableCell>
                    <TableCell>{formatCurrency(asset.current_price * asset.quantity)}</TableCell>
                    <TableCell>{formatPercentage(asset.allocation_percentage)}</TableCell>
                    <TableCell className={asset.return_percentage >= 0 ? "text-green-600" : "text-red-600"}>
                      {formatPercentage(asset.return_percentage)}
                    </TableCell>
                    <TableCell className={asset.total_gain_loss >= 0 ? "text-green-600" : "text-red-600"}>
                      {formatCurrency(asset.total_gain_loss)}
                    </TableCell>
                  </TableRow>
                ))}
                {assets.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center text-muted-foreground">
                      No assets found in this portfolio.
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