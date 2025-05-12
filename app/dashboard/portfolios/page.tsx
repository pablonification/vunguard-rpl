import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus, Trash2 } from "lucide-react"
import Link from "next/link"
import { getPortfolios, formatCurrency, formatPercentage, Portfolio } from "@/lib/db/models/portfolio"
import { requireAuth } from "@/lib/auth"
import { executeQuery } from "@/lib/db"
import InvestorSelect from "./investor-select"
import { redirect } from "next/navigation"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { AlertCircle, CheckCircle2 } from "lucide-react"
import { DeletePortfolioButton } from "./delete-portfolio-button"

interface SearchParams {
  investorId?: string;
  success?: string;
  error?: string;
}

export default async function PortfoliosPage({ searchParams }: { searchParams: SearchParams }) {
  // Get current user's session and account
  const session = await requireAuth(['investor', 'manager', 'analyst'])
  
  // Determine if user is authorized to select investors (manager or analyst)
  const canSelectInvestor = ['manager', 'analyst'].includes(session.role as string);
  const canCreatePortfolio = session.role === 'investor' || session.role === 'manager';
  const canDeletePortfolio = session.role === 'investor' || session.role === 'manager';
  
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

  // Fetch actual portfolio data from the database
  const portfolios = await getPortfolios(accountId)

  // Generate the create portfolio URL
  let createPortfolioUrl = "/dashboard/portfolios/new";
  if (canSelectInvestor && targetAccountId !== session.id) {
    createPortfolioUrl += `?investorId=${targetAccountId}`;
  }
  
  // Success and error messages
  let successMessage: string | null = null;
  let errorMessage: string | null = null;
  
  if (searchParams.success === 'portfolio-deleted') {
    successMessage = 'Portfolio has been successfully deleted.';
  }
  
  if (searchParams.error === 'delete-failed') {
    errorMessage = 'Failed to delete portfolio. Please try again.';
  } else if (searchParams.error === 'unexpected') {
    errorMessage = 'An unexpected error occurred. Please try again.';
  }

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Portfolios</h1>
          {canCreatePortfolio && (
            <Link href={createPortfolioUrl}>
              <Button>
                <Plus className="mr-2 h-4 w-4" /> Create Portfolio
              </Button>
            </Link>
          )}
        </div>
        <p className="text-muted-foreground">Manage your investment portfolios and track their performance.</p>

        {/* Success message */}
        {successMessage && (
          <Alert variant="default" className="bg-green-50 border-green-300 text-green-800">
            <CheckCircle2 className="h-4 w-4 text-green-600" />
            <AlertTitle>Success</AlertTitle>
            <AlertDescription>{successMessage}</AlertDescription>
          </Alert>
        )}
        
        {/* Error message */}
        {errorMessage && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{errorMessage}</AlertDescription>
          </Alert>
        )}

        {/* Show investor selection for managers and analysts */}
        {canSelectInvestor && <InvestorSelect currentInvestorId={targetAccountId} />}
        
        {canSelectInvestor && targetAccountId !== session.id && (
          <p className="text-muted-foreground">
            Viewing portfolios for {accountFullName || 'investor'}
          </p>
        )}

        <Card>
          <CardHeader>
            <CardTitle>
              {canSelectInvestor && targetAccountId !== session.id ? `${accountFullName}'s Portfolios` : 'Your Portfolios'}
            </CardTitle>
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
                  <TableHead>Actions</TableHead>
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
                    <TableCell>
                      <div className="flex space-x-2">
                        <Link href={`/dashboard/portfolios/${portfolio.id}`}>
                          <Button variant="ghost" size="sm">
                            View
                          </Button>
                        </Link>
                        {canDeletePortfolio && (
                          <DeletePortfolioButton 
                            portfolioId={portfolio.id} 
                            portfolioName={portfolio.name}
                            investorId={canSelectInvestor && targetAccountId !== session.id ? targetAccountId.toString() : undefined}
                          />
                        )}
                      </div>
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
