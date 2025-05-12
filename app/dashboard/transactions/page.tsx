import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"
import { getTransactions } from "@/lib/db/models/transaction"
import { formatDate } from "@/lib/utils"
import { requireAuth } from "@/lib/auth"
import { executeQuery } from "@/lib/db"
import InvestorSelect from "./investor-select"

interface SearchParams {
  investorId?: string;
}

export default async function TransactionsPage({ searchParams }: { searchParams: SearchParams }) {
  // Get current user and verify authorization with explicit roles
  const session = await requireAuth(["investor", "manager"])
  
  // Determine if user is authorized to select investors (manager role)
  const canSelectInvestor = session.role === 'manager';
  
  // Determine which account to show data for
  let targetAccountId: number;
  let accountFullName: string | null = null;
  
  if (canSelectInvestor && searchParams.investorId) {
    // If manager and investorId is provided in URL, use that
    targetAccountId = parseInt(searchParams.investorId, 10);
    if (isNaN(targetAccountId)) {
      console.error("Invalid investorId in URL:", searchParams.investorId);
      // Fall back to the session's own ID
      targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : Number(session.id || 0);
    } else {
      // Get investor name for display
      const accountResult = await executeQuery("SELECT full_name FROM accounts WHERE id = $1", [targetAccountId]);
      accountFullName = accountResult[0]?.full_name || null;
    }
  } else {
    // Otherwise, use session ID (for regular users or if no selection)
    targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : Number(session.id || 0);
  }
  
  if (isNaN(targetAccountId)) {
    console.error("Invalid targetAccountId after parsing:", targetAccountId);
    throw new Error('Invalid account ID');
  }
  
  // Generate the create transaction URL with investor ID if applicable
  let createTransactionUrl = "/dashboard/transactions/new";
  if (canSelectInvestor && searchParams.investorId) {
    createTransactionUrl += `?investorId=${searchParams.investorId}`;
  }
  
  // Fetch transactions for the specific account (or all if admin/manager without selection)
  const transactions = await getTransactions(
    // For investors, always filter by their ID
    // For managers, filter by selected investor if provided, otherwise show all
    session.role === 'investor' ? targetAccountId : 
    (canSelectInvestor && searchParams.investorId) ? targetAccountId : undefined
  );
  
  return (
    <DashboardLayout requiredRoles={["investor", "manager"]}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Transactions</h1>
          <Link href={createTransactionUrl}>
            <Button>
              <Plus className="mr-2 h-4 w-4" /> Record Transaction
            </Button>
          </Link>
        </div>
        <p className="text-muted-foreground">View and manage all investment transactions.</p>

        {/* Show investor selection for managers */}
        {canSelectInvestor && <InvestorSelect currentInvestorId={targetAccountId} />}
        
        {canSelectInvestor && accountFullName && (
          <p className="text-muted-foreground">
            Viewing transactions for {accountFullName}
          </p>
        )}

        <Card>
          <CardHeader>
            <CardTitle>
              {canSelectInvestor && accountFullName ? `${accountFullName}'s Transactions` : 'Recent Transactions'}
            </CardTitle>
            <CardDescription>A list of recent transactions across all portfolios.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Portfolio</TableHead>
                  <TableHead>Product</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="hidden md:table-cell">Quantity</TableHead>
                  <TableHead className="hidden md:table-cell">Price</TableHead>
                  <TableHead>Total</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {transactions.length > 0 ? (
                  transactions.map((transaction) => (
                    <TableRow key={transaction.id}>
                      <TableCell>{transaction.portfolioName}</TableCell>
                      <TableCell>{transaction.productName}</TableCell>
                      <TableCell>
                        <span
                          className={
                            transaction.transactionType === "buy"
                              ? "text-green-600 dark:text-green-400"
                              : "text-red-600 dark:text-red-400"
                          }
                        >
                          {transaction.transactionType.charAt(0).toUpperCase() + transaction.transactionType.slice(1)}
                        </span>
                      </TableCell>
                      <TableCell className="hidden md:table-cell">
                        {transaction.quantity.toLocaleString(undefined, {
                          minimumFractionDigits: 0,
                          maximumFractionDigits: 6
                        })}
                      </TableCell>
                      <TableCell className="hidden md:table-cell">
                        {new Intl.NumberFormat('en-US', {
                          style: 'currency',
                          currency: 'USD',
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 6
                        }).format(transaction.price)}
                      </TableCell>
                      <TableCell>
                        {new Intl.NumberFormat('en-US', {
                          style: 'currency',
                          currency: 'USD',
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2
                        }).format(transaction.total)}
                      </TableCell>
                      <TableCell>{formatDate(new Date(transaction.transactionDate))}</TableCell>
                      <TableCell className="text-right">
                        <Link href={`/dashboard/transactions/${transaction.id}`}>
                          <Button variant="ghost" size="sm">
                            View
                          </Button>
                        </Link>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={8} className="text-center text-muted-foreground py-6">
                      No transactions found.
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
