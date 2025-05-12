import { DashboardLayout } from "@/components/dashboard-layout";
import PerformanceClientContent from "./performance-client-content";
import { getPortfolioPerformances, getAssetAllocations, getPerformanceOverTime, getRiskAllocation } from "@/lib/db/models/performance";
import { getSession } from "@/lib/auth";
import { redirect } from "next/navigation";
import type { PortfolioPerformance, AssetAllocation, PerformanceOverTime } from "@/lib/db/models/performance";
import InvestorSelect from "./investor-select";

interface Session {
  id: string | number;
  role?: string;
  // Add other session properties as needed
}

// --- MOCK DATA GENERATION (To be removed/commented) ---
/*
const generateMockPortfolioPerformance = () => [
  { id: 1, name: "Aggressive Growth Portfolio", currentValue: 125000, initialValue: 100000, returnPercentage: 25.0, benchmarkComparison: 20.0 },
  { id: 2, name: "Balanced Income Fund", currentValue: 85000, initialValue: 80000, returnPercentage: 6.25, benchmarkComparison: 5.0 },
  { id: 3, name: "Conservative Capital Preservation", currentValue: 52000, initialValue: 50000, returnPercentage: 4.0, benchmarkComparison: 3.5 },
  { id: 4, name: "Emerging Markets High-Yield", currentValue: 78000, initialValue: 65000, returnPercentage: 20.0, benchmarkComparison: 18.0 },
  { id: 5, name: "Tech Innovation Fund", currentValue: 150000, initialValue: 110000, returnPercentage: 36.36, benchmarkComparison: 30.0 },
];

const generateMockPerformanceOverTime = () => {
  const data = [];
  const basePortfolio = 100000;
  const baseBenchmark = 98000;
  const today = new Date();
  for (let i = 11; i >= 0; i--) {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1);
    const monthStr = date.toLocaleString('default', { month: 'short' });
    const yearStr = date.getFullYear().toString().slice(-2);
    data.push({
      date: `${monthStr} '${yearStr}`,
      portfolioValue: basePortfolio * (1 + (Math.random() * 0.03 + 0.01) * (12 - i)), // Simulating growth
      benchmarkValue: baseBenchmark * (1 + (Math.random() * 0.025 + 0.005) * (12 - i)), // Simulating growth
    });
  }
  return data;
};

const generateMockAssetAllocation = () => [
  { name: "US Large Cap Equity", value: 150000, riskLevel: "Medium" },
  { name: "International Developed Equity", value: 120000, riskLevel: "Medium" },
  { name: "Emerging Markets Equity", value: 80000, riskLevel: "High" },
  { name: "US Investment Grade Bonds", value: 90000, riskLevel: "Low" },
  { name: "High-Yield Corporate Bonds", value: 60000, riskLevel: "High" },
  { name: "Real Estate (REITs)", value: 75000, riskLevel: "Medium" },
  { name: "Cash & Equivalents", value: 25000, riskLevel: "Low" },
];

const generateMockRiskAllocation = (assetAllocations: Array<{name: string; value: number; riskLevel: string}>) => {
  const riskSummary = { Low: 0, Medium: 0, High: 0 };
  assetAllocations.forEach(asset => {
    if (asset.riskLevel === "Low") riskSummary.Low += asset.value;
    else if (asset.riskLevel === "Medium") riskSummary.Medium += asset.value;
    else if (asset.riskLevel === "High") riskSummary.High += asset.value;
  });
  return [
    { name: "Low", value: riskSummary.Low },
    { name: "Medium", value: riskSummary.Medium },
    { name: "High", value: riskSummary.High },
  ];
};
*/
// --- END MOCK DATA GENERATION ---

export default async function PerformancePage({ searchParams }: { searchParams: { investorId?: string } }) {
  const session = await getSession() as Session | null;
  
  if (!session || !session.id) { // Ensure session.id exists
    redirect("/login");
    return; // Add return to stop execution after redirect
  }

  // Determine if user is authorized to select investors (manager, admin, or analyst)
  const canSelectInvestor = ['manager', 'admin', 'analyst'].includes(session.role as string);
  
  // Determine which account to show data for
  let targetAccountId: number;
  
  if (canSelectInvestor && searchParams.investorId) {
    // If authorized role and investorId is provided in URL, use that
    targetAccountId = parseInt(searchParams.investorId, 10);
    if (isNaN(targetAccountId)) {
      console.error("Invalid investorId in URL:", searchParams.investorId);
      // Fall back to the session's own ID
      targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : session.id;
    }
  } else {
    // Otherwise use session ID (for regular users or if no selection)
    targetAccountId = typeof session.id === 'string' ? parseInt(session.id, 10) : session.id;
  }
  
  if (isNaN(targetAccountId)) {
    console.error("Invalid targetAccountId after parsing:", targetAccountId);
    redirect("/login");
    return;
  }
  
  // Fetch all required data from the database
  const [
    dbPortfolioPerformances,
    dbAssetAllocations,
    dbPerformanceOverTime,
    dbRiskAllocation
  ] = await Promise.all([
    getPortfolioPerformances(targetAccountId),
    getAssetAllocations(targetAccountId),
    getPerformanceOverTime(targetAccountId),
    getRiskAllocation(targetAccountId)
  ]) as [
    PortfolioPerformance[],
    AssetAllocation[],
    PerformanceOverTime[],
    { name: string; value: number; }[]
  ];

  // Format portfolio performance data for the table
  const portfolioPerformanceTableData = dbPortfolioPerformances.map(portfolio => {
    const returnPct = Number(portfolio.returnPercentage);
    const benchmarkPct = Number(portfolio.benchmarkComparison);
    const difference = returnPct - benchmarkPct;
    return {
      ...portfolio, // id, name etc. from DB object
      currentValue: new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
      }).format(portfolio.currentValue),
      initialValue: new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
      }).format(portfolio.initialValue),
      return: `${returnPct >= 0 ? '+' : ''}${returnPct.toFixed(1)}%`,
      benchmark: `${benchmarkPct >= 0 ? '+' : ''}${benchmarkPct.toFixed(1)}%`,
      difference: `${difference >= 0 ? '+' : ''}${difference.toFixed(1)}%`
    };
  });

  // Format performance over time data for the chart (ensure date is 'Mon YY' string)
  const formattedPerformanceOverTimeData = dbPerformanceOverTime.map(item => {
    const dateObj = new Date(item.date); // Assuming item.date is a Date object or valid date string
    const monthStr = dateObj.toLocaleString('default', { month: 'short' });
    const yearStr = dateObj.getFullYear().toString().slice(-2);
    return {
      ...item,
      date: `${monthStr} '${yearStr}`,
    };
  });

  return (
    <DashboardLayout>
      {canSelectInvestor && <InvestorSelect currentInvestorId={targetAccountId} />}
      <PerformanceClientContent 
        portfolioPerformanceTableData={portfolioPerformanceTableData}
        performanceOverTimeData={formattedPerformanceOverTimeData}
        assetAllocationData={dbAssetAllocations}
        riskAllocationData={dbRiskAllocation}
      />
    </DashboardLayout>
  );
}
