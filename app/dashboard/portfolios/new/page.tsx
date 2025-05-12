import { DashboardLayout } from "@/components/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { CreatePortfolioForm } from "./create-portfolio-form"
import { requireAuth } from "@/lib/auth"
import { executeQuery } from "@/lib/db"

interface CreatePortfolioPageProps {
  searchParams: {
    investorId?: string;
  };
}

export default async function CreatePortfolioPage({ searchParams }: CreatePortfolioPageProps) {
  const session = await requireAuth(['investor', 'manager']);
  
  let investorId = null;
  let investorName = null;
  
  // Handle investorId for managers creating portfolios for other users
  if (session.role === 'manager' && searchParams.investorId) {
    const userId = parseInt(searchParams.investorId, 10);
    if (!isNaN(userId)) {
      // Verify investor exists and has investor role
      const result = await executeQuery(
        "SELECT id, full_name FROM accounts WHERE id = $1 AND role = 'investor'",
        [userId]
      );
      
      if (result.length > 0) {
        investorId = userId;
        investorName = result[0].full_name;
      }
    }
  }

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <h1 className="text-3xl font-bold tracking-tight">Create Portfolio</h1>
        {investorName ? (
          <p className="text-muted-foreground">Create a new investment portfolio for {investorName}.</p>
        ) : (
          <p className="text-muted-foreground">Create a new investment portfolio to manage your assets.</p>
        )}

        <Card>
          <CardHeader>
            <CardTitle>New Portfolio</CardTitle>
            <CardDescription>Enter the details for the new portfolio.</CardDescription>
          </CardHeader>
          <CardContent>
            <CreatePortfolioForm investorId={investorId} />
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  )
} 