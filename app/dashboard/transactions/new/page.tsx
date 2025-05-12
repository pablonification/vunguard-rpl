import { DashboardLayout } from "@/components/dashboard-layout"
import { RecordTransactionForm } from "./record-transaction-form"
import { requireAuth } from "@/lib/auth"

export default async function NewTransactionPage() {
  // Only allow managers and admins to access this page
  await requireAuth(["manager", "admin"])

  return (
    <DashboardLayout requiredRoles={["manager", "admin"]}>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Record Transaction</h1>
        <p className="text-muted-foreground">Record a new investment transaction.</p>
        <RecordTransactionForm />
      </div>
    </DashboardLayout>
  )
} 