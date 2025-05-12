import { DashboardLayout } from "@/components/dashboard-layout";
import { requireAuth } from "@/lib/auth";
import { redirect } from "next/navigation";
import { getRecommendations } from "@/lib/db/models/recommendation";
import RecommendationList from "./recommendation-list";
import CreateRecommendationButton from "./create-recommendation-button";

export default async function AnalystRecommendationsPage() {
  const session = await requireAuth();
  
  if (session.role !== 'analyst') {
    redirect('/unauthorized');
  }

  const analystId = typeof session.id === 'string' ? parseInt(session.id) : session.id;
  
  console.log('Analyst page loading with session:', { 
    id: session.id, 
    parsedId: analystId,
    role: session.role 
  });
  
  const recommendations = await getRecommendations({ analystId });

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Investment Recommendations</h1>
          <CreateRecommendationButton />
        </div>
        <p className="text-muted-foreground">
          Create and manage investment recommendations for portfolio managers.
        </p>

        <RecommendationList initialRecommendations={recommendations} />
      </div>
    </DashboardLayout>
  );
} 