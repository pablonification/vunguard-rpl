import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus } from "lucide-react"
import Link from "next/link"

export default function AccountsPage() {
  // Mock data - in a real app, this would come from the database
  const accounts = [
    {
      id: 1,
      username: "john.doe",
      fullName: "John Doe",
      email: "john.doe@example.com",
      role: "investor",
      createdAt: "2023-01-15",
    },
    {
      id: 2,
      username: "jane.smith",
      fullName: "Jane Smith",
      email: "jane.smith@example.com",
      role: "manager",
      createdAt: "2023-02-20",
    },
    {
      id: 3,
      username: "robert.johnson",
      fullName: "Robert Johnson",
      email: "robert.johnson@example.com",
      role: "analyst",
      createdAt: "2023-03-10",
    },
    {
      id: 4,
      username: "sarah.williams",
      fullName: "Sarah Williams",
      email: "sarah.williams@example.com",
      role: "investor",
      createdAt: "2023-04-05",
    },
    {
      id: 5,
      username: "admin.user",
      fullName: "Admin User",
      email: "admin@example.com",
      role: "admin",
      createdAt: "2023-01-01",
    },
  ]

  return (
    <DashboardLayout requiredRoles={["admin"]}>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Accounts</h1>
          <Link href="/dashboard/accounts/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" /> Add Account
            </Button>
          </Link>
        </div>
        <p className="text-muted-foreground">Manage user accounts and their access permissions.</p>

        <Card>
          <CardHeader>
            <CardTitle>All Accounts</CardTitle>
            <CardDescription>A list of all user accounts in the system.</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Username</TableHead>
                  <TableHead>Full Name</TableHead>
                  <TableHead className="hidden md:table-cell">Email</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead className="hidden md:table-cell">Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {accounts.map((account) => (
                  <TableRow key={account.id}>
                    <TableCell className="font-medium">{account.username}</TableCell>
                    <TableCell>{account.fullName}</TableCell>
                    <TableCell className="hidden md:table-cell">{account.email}</TableCell>
                    <TableCell>
                      <span className="capitalize">{account.role}</span>
                    </TableCell>
                    <TableCell className="hidden md:table-cell">{account.createdAt}</TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/accounts/${account.id}`}>
                        <Button variant="ghost" size="sm">
                          Edit
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
