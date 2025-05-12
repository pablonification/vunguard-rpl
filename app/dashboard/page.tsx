import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { requireAuth } from "@/lib/auth"
import { executeQuery } from "@/lib/db"
import { formatCurrency, formatPercentage } from "@/lib/db/models/portfolio"
import { 
  getDashboardSummary, 
  getPerformanceOverTime, 
  getAssetAllocation, 
  getRecentTransactions, 
  getTopPerformingProducts 
} from "@/lib/db/models/dashboard"
import DashboardCharts from "./dashboard-charts"
import { TopUpDialog } from "./topup-dialog"
import { Button } from "@/components/ui/button"
import { Coins } from "lucide-react"
import InvestorSelect from "./investor-select"
import { redirect } from "next/navigation"

interface SearchParams {
  investorId?: string;
}

export default async function DashboardPage({ searchParams }: { searchParams: SearchParams }) {
  // Get current user with explicit authorization
  const session = await requireAuth(["investor", "manager", "analyst"])
  
  // Redirect admin to accounts page if they somehow access the dashboard
  if (session.role === "admin") {
    redirect("/dashboard/accounts")
  }
  
  // Determine if user is authorized to select investors (manager, admin, or analyst)
  const canSelectInvestor = ['manager', 'analyst'].includes(session.role as string);
  
  // Determine which account to show data for
  let targetAccountId: number;
  
  if (canSelectInvestor && searchParams.investorId) {
    // If authorized role and investorId is provided in URL, use that
    targetAccountId = parseInt(searchParams.investorId, 10);
    if (isNaN(targetAccountId)) {
      console.error("Invalid investorId in URL:", searchParams.investorId);
      // Fall back to the session's own ID
      targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : Number(session.id || 0);
    }
  } else {
    // Otherwise use session ID (for regular users or if no selection)
    targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : Number(session.id || 0);
  }
  
  if (isNaN(targetAccountId)) {
    console.error("Invalid targetAccountId after parsing:", targetAccountId);
    throw new Error('Invalid account ID');
  }

  // Get account details
  const accountQuery = "SELECT id, full_name FROM accounts WHERE id = $1"
  const accountResult = await executeQuery(accountQuery, [targetAccountId])
  const accountId = accountResult[0]?.id
  const accountFullName = accountResult[0]?.full_name

  if (!accountId) {
    throw new Error('Account not found')
  }

  // Fetch data for dashboard
  const [summary, performanceData, assetAllocation, recentTransactions, topProducts] = await Promise.all([
    getDashboardSummary(accountId),
    getPerformanceOverTime(accountId),
    getAssetAllocation(accountId),
    getRecentTransactions(accountId),
    getTopPerformingProducts(accountId)
  ])

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          {session.role === 'investor' && (
            <TopUpDialog accountId={accountId} />
          )}
        </div>
        
        {/* Show investor selection for managers and analysts */}
        {canSelectInvestor && <InvestorSelect currentInvestorId={targetAccountId} />}
        
        <p className="text-muted-foreground">
          {canSelectInvestor && targetAccountId !== session.id ? 
            `Viewing dashboard for ${accountFullName || 'investor'}` : 
            `Welcome back, ${accountFullName || session.username}! Here's an overview of your investment assets.`
          }
        </p>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Portfolio Value</CardTitle>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                className="h-4 w-4 text-muted-foreground"
              >
                <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
              </svg>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(summary.totalValue)}</div>
              <p className="text-xs text-muted-foreground">
                {summary.monthlyChange >= 0 ? "+" : ""}{formatPercentage(summary.monthlyChange)} from last month
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Average Return</CardTitle>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                className="h-4 w-4 text-muted-foreground"
              >
                <path d="M16 3h5v5M8 3H3v5M3 16v5h5M16 21h5v-5" />
              </svg>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatPercentage(summary.totalReturn)}</div>
              <p className="text-xs text-muted-foreground">Across all portfolios</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Active Products</CardTitle>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                className="h-4 w-4 text-muted-foreground"
              >
                <rect width="20" height="14" x="2" y="5" rx="2" />
                <path d="M2 10h20" />
              </svg>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.activeProducts}</div>
              <p className="text-xs text-muted-foreground">Different investment products</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Recent Transactions</CardTitle>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                className="h-4 w-4 text-muted-foreground"
              >
                <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
              </svg>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{summary.pendingTransactions}</div>
              <p className="text-xs text-muted-foreground">In the last 30 days</p>
            </CardContent>
          </Card>
        </div>

        {/* Charts section - using client component for the charts */}
        <DashboardCharts 
          performanceData={performanceData}
          assetAllocation={assetAllocation}
        />

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
          <Card className="col-span-4">
            <CardHeader>
              <CardTitle>Recent Transactions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-8">
                {recentTransactions.length > 0 ? (
                  recentTransactions.map(transaction => (
                    <div key={transaction.id} className="flex items-center">
                  <div className="ml-4 space-y-1">
                        <p className="text-sm font-medium leading-none">
                          {transaction.transactionType.charAt(0).toUpperCase() + transaction.transactionType.slice(1)}: {transaction.productName}
                        </p>
                        <p className="text-sm text-muted-foreground">{transaction.date}</p>
                  </div>
                      <div className={`ml-auto font-medium ${transaction.amount >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {formatCurrency(Math.abs(transaction.amount))}
                </div>
                  </div>
                  ))
                ) : (
                  <p className="text-muted-foreground text-center py-4">No recent transactions found</p>
                )}
              </div>
            </CardContent>
          </Card>
          <Card className="col-span-3">
            <CardHeader>
              <CardTitle>Top Performing Products</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-8">
                {topProducts.length > 0 ? (
                  topProducts.map((product, index) => (
                    <div key={index} className="flex items-center">
                  <div className="ml-4 space-y-1">
                        <p className="text-sm font-medium leading-none">{product.name}</p>
                        <p className="text-sm text-muted-foreground">{product.riskLevel} Risk</p>
                  </div>
                      <div className="ml-auto font-medium text-green-600">
                        {formatPercentage(product.returnPercentage)}
                </div>
                  </div>
                  ))
                ) : (
                  <p className="text-muted-foreground text-center py-4">No products data available</p>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  )
}
