import { DashboardLayout } from "@/components/dashboard-layout"
import { RecordTransactionForm } from "./record-transaction-form"

export default function NewTransactionPage() {
  return (
    <DashboardLayout requiredRoles={["investor", "manager", "admin"]}>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Record Transaction</h1>
        <p className="text-muted-foreground">Record a new investment transaction.</p>
        <RecordTransactionForm />
      </div>
    </DashboardLayout>
  )
} 