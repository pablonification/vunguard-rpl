import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"

export default function TransactionsPage() {
  // Mock data - in a real app, this would come from the database
  const transactions = [
    {
      id: 1,
      portfolioName: "Retirement Fund",
      assetName: "Tech Growth Fund",
      type: "buy",
      quantity: "10",
      price: "$199.90",
      total: "$1,999.00",
      date: "2023-05-10",
    },
    {
      id: 2,
      portfolioName: "Retirement Fund",
      assetName: "Global Bond Fund",
      type: "sell",
      quantity: "50",
      price: "$70.00",
      total: "$3,500.00",
      date: "2023-05-08",
    },
    {
      id: 3,
      portfolioName: "Growth Portfolio",
      assetName: "Emerging Markets ETF",
      type: "buy",
      quantity: "25",
      price: "$110.00",
      total: "$2,750.00",
      date: "2023-05-05",
    },
    {
      id: 4,
      portfolioName: "Income Portfolio",
      assetName: "Income Fund",
      type: "dividend",
      quantity: "-",
      price: "-",
      total: "$350.00",
      date: "2023-05-01",
    },
    {
      id: 5,
      portfolioName: "Education Fund",
      assetName: "Healthcare Innovation Fund",
      type: "buy",
      quantity: "15",
      price: "$85.00",
      total: "$1,275.00",
      date: "2023-04-28",
    },
  ]

  return (
    <DashboardLayout requiredRoles={["investor", "manager", "admin"]}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Transactions</h1>
          <Link href="/dashboard/transactions/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" /> Record Transaction
            </Button>
          </Link>
        </div>
        <p className="text-muted-foreground">View and manage all investment transactions.</p>

        <Card>
          <CardHeader>
            <CardTitle>Recent Transactions</CardTitle>
            <CardDescription>A list of recent transactions across all portfolios.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Portfolio</TableHead>
                  <TableHead>Asset</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="hidden md:table-cell">Quantity</TableHead>
                  <TableHead className="hidden md:table-cell">Price</TableHead>
                  <TableHead>Total</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {transactions.map((transaction) => (
                  <TableRow key={transaction.id}>
                    <TableCell>{transaction.portfolioName}</TableCell>
                    <TableCell>{transaction.assetName}</TableCell>
                    <TableCell>
                      <span
                        className={
                          transaction.type === "buy"
                            ? "text-green-600"
                            : transaction.type === "sell"
                              ? "text-red-600"
                              : "text-blue-600"
                        }
                      >
                        {transaction.type.charAt(0).toUpperCase() + transaction.type.slice(1)}
                      </span>
                    </TableCell>
                    <TableCell className="hidden md:table-cell">{transaction.quantity}</TableCell>
                    <TableCell className="hidden md:table-cell">{transaction.price}</TableCell>
                    <TableCell>{transaction.total}</TableCell>
                    <TableCell>{transaction.date}</TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/transactions/${transaction.id}`}>
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
