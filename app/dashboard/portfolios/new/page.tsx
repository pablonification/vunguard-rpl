import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { CreatePortfolioForm } from "./create-portfolio-form"
import { requireAuth } from "@/lib/auth"

export default async function CreatePortfolioPage() {
  await requireAuth()

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Create Portfolio</h1>
        <p className="text-muted-foreground">Create a new investment portfolio to manage your assets.</p>

        <Card>
          <CardHeader>
            <CardTitle>New Portfolio</CardTitle>
            <CardDescription>Enter the details for your new portfolio.</CardDescription>
          </CardHeader>
          <CardContent>
            <CreatePortfolioForm />
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
} 