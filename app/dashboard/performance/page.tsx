import { DashboardLayout } from "@/components/dashboard-layout";
import PerformanceClientContent from "./performance-client-content";
import { getPortfolioPerformances, getAssetAllocations, getPerformanceOverTime, getRiskAllocation } from "@/lib/db/models/performance";
import { getSession } from "@/lib/auth";
import { redirect } from "next/navigation";
import type { PortfolioPerformance, AssetAllocation, PerformanceOverTime } from "@/lib/db/models/performance";

interface Session {
  id: string | number;
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


export default async function PerformancePage() {
  const session = await getSession() as Session | null;
  // console.log('Session:', session); // Keep for debugging if needed
  
  if (!session || !session.id) { // Ensure session.id exists
    redirect("/login");
    return; // Add return to stop execution after redirect
  }

  // Ensure session.id is a number for database queries
  const accountId = typeof session.id === 'string' ? parseInt(session.id, 10) : session.id;
  if (isNaN(accountId)) {
    console.error("Invalid accountId after parsing session.id:", session.id);
    // Handle error appropriately, maybe redirect or show an error message
    redirect("/login"); // Or an error page
    return;
  }
  // console.log('Account ID:', accountId); // Keep for debugging if needed

  // Fetch all required data from the database
  /* 
  // Commented out mock data assignment
  const mockPortfolioPerformancesRaw = generateMockPortfolioPerformance();
  const mockAssetAllocationsRaw = generateMockAssetAllocation();
  const mockPerformanceOverTimeRaw = generateMockPerformanceOverTime();
  const mockRiskAllocationRaw = generateMockRiskAllocation(mockAssetAllocationsRaw);

  const [
    portfolioPerformances,
    assetAllocations,
    performanceOverTime,
    riskAllocation
  ] = [
    mockPortfolioPerformancesRaw,
    mockAssetAllocationsRaw,
    mockPerformanceOverTimeRaw,
    mockRiskAllocationRaw
  ];
  */

  const [
    dbPortfolioPerformances,
    dbAssetAllocations,
    dbPerformanceOverTime,
    dbRiskAllocation
  ] = await Promise.all([
    getPortfolioPerformances(accountId),
    getAssetAllocations(accountId),
    getPerformanceOverTime(accountId),
    getRiskAllocation(accountId) // This function should derive risk from asset allocations for accountId
  ]) as [
    PortfolioPerformance[],
    AssetAllocation[],
    PerformanceOverTime[], // Assuming this returns date as Date object or ISO string
    { name: string; value: number; }[]
  ];

  // console.log('DB Portfolio Performances:', dbPortfolioPerformances);
  // console.log('DB Asset Allocations:', dbAssetAllocations);
  // console.log('DB Performance Over Time:', dbPerformanceOverTime);
  // console.log('DB Risk Allocation:', dbRiskAllocation);

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

  // console.log('Formatted Table Data (from DB):', portfolioPerformanceTableData);
  // console.log('Formatted Chart Data (Performance Over Time from DB):', formattedPerformanceOverTimeData);

  return (
    <DashboardLayout>
      <PerformanceClientContent 
        portfolioPerformanceTableData={portfolioPerformanceTableData}
        performanceOverTimeData={formattedPerformanceOverTimeData} // Use formatted data
        assetAllocationData={dbAssetAllocations} // Pass dbAssetAllocations directly
        riskAllocationData={dbRiskAllocation} // Pass dbRiskAllocation directly
      />
    </DashboardLayout>
  );
}
