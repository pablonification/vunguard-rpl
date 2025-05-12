import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getTransactionById } from "@/lib/db/models/transaction"
import { formatDate } from "@/lib/utils"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import { notFound } from "next/navigation"

export default async function TransactionPage({ params }: { params: { id: string } }) {
  const transaction = await getTransactionById(parseInt(params.id))

  if (!transaction) {
    notFound()
  }

  return (
    <DashboardLayout requiredRoles={["investor", "manager", "admin"]}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center gap-4">
          <Link href="/dashboard/transactions">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Transaction Details</h1>
            <p className="text-muted-foreground">
              View details of transaction #{transaction.id}
            </p>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>Transaction details and amounts</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                <div className="text-sm text-muted-foreground">Transaction ID</div>
                <div className="text-sm font-medium">#{transaction.id}</div>

                <div className="text-sm text-muted-foreground">Type</div>
                <div className="text-sm font-medium">
                  <span
                    className={
                      transaction.transactionType === "buy"
                        ? "text-green-600 dark:text-green-400"
                        : "text-red-600 dark:text-red-400"
                    }
                  >
                    {transaction.transactionType.charAt(0).toUpperCase() + transaction.transactionType.slice(1)}
                  </span>
                </div>

                <div className="text-sm text-muted-foreground">Date</div>
                <div className="text-sm font-medium">
                  {formatDate(new Date(transaction.transactionDate))}
                </div>

                <div className="text-sm text-muted-foreground">Quantity</div>
                <div className="text-sm font-medium">
                  {transaction.quantity.toLocaleString(undefined, {
                    minimumFractionDigits: 0,
                    maximumFractionDigits: 6
                  })}
                </div>

                <div className="text-sm text-muted-foreground">Price per Unit</div>
                <div className="text-sm font-medium">
                  {new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD',
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 6
                  }).format(transaction.price)}
                </div>

                <div className="text-sm text-muted-foreground">Total Amount</div>
                <div className="text-sm font-medium">
                  {new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD',
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                  }).format(transaction.total)}
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Portfolio Information</CardTitle>
              <CardDescription>Related portfolio and product details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                <div className="text-sm text-muted-foreground">Portfolio</div>
                <div className="text-sm font-medium">{transaction.portfolioName}</div>

                <div className="text-sm text-muted-foreground">Product</div>
                <div className="text-sm font-medium">{transaction.productName}</div>

                {transaction.notes && (
                  <>
                    <div className="text-sm text-muted-foreground">Notes</div>
                    <div className="text-sm font-medium col-span-2">{transaction.notes}</div>
                  </>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  )
} 