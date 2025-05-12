import { DashboardLayout } from "@/components/dashboard-layout";
import { requireAuth } from "@/lib/auth";
import { redirect } from "next/navigation";
import { getRecommendations } from "@/lib/db/models/recommendation";
import ManagerRecommendationList from "./manager-recommendation-list";

export default async function ManagerRecommendationsPage() {
  const session = await requireAuth();
  
  if (session.role !== 'manager') {
    redirect('/unauthorized');
  }

  const recommendations = await getRecommendations();

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Review Recommendations</h1>
        </div>
        <p className="text-muted-foreground">
          Review and manage analyst recommendations for investment products.
        </p>

        <ManagerRecommendationList initialRecommendations={recommendations} />
      </div>
    </DashboardLayout>
  );
} 