import { DashboardLayout } from "@/components/dashboard-layout"
import { EditAccountForm } from "./edit-account-form"
import { getAccountById } from "@/lib/db/models/account"

export default async function EditAccountPage({ params }: { params: { id: string } }) {
  // Next.js 14 requires awaiting params before accessing its properties
  const { id } = await params;
  const account = await getAccountById(parseInt(id))

  return (
    <DashboardLayout requiredRoles={["admin"]}>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Edit Account</h1>
        <p className="text-muted-foreground">Update account information and permissions.</p>
        <EditAccountForm account={account} />
      </div>
    </DashboardLayout>
  )
} 