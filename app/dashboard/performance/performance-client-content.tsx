"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BarChart as LucideBarChart, LineChart as LucideLineChart, PieChart as LucidePieChart, Download } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  Area,
  AreaChart,
} from 'recharts';
import { useState, useEffect } from 'react';

// Modern color palette using HSL values that work well in both light and dark modes
const COLORS = [
  'hsl(var(--primary))',       // Primary theme color
  'hsl(329 80% 65%)',          // Rose
  'hsl(221 83% 65%)',          // Vivid blue
  'hsl(43 96% 58%)',           // Vibrant yellow
  'hsl(142 70% 45%)',          // Strong green
  'hsl(280 85% 65%)',          // Deep purple
  'hsl(0 90% 60%)',            // Bright red
  'hsl(189 95% 50%)',          // Cyan
];

// Specific colors for risk levels
const RISK_COLORS = [
  'hsl(142 70% 45%)',  // Low risk - Strong green
  'hsl(43 96% 58%)',   // Medium risk - Vibrant yellow
  'hsl(0 90% 60%)',    // High risk - Bright red
];

// Custom tooltip styles
const customTooltipStyle = {
  backgroundColor: 'hsl(var(--background))',
  border: 'none',
  borderRadius: '0.5rem',
  padding: '0.75rem',
  boxShadow: '0 4px 12px -1px rgb(0 0 0 / 0.15), 0 2px 8px -2px rgb(0 0 0 / 0.15)',
  color: 'hsl(var(--foreground))',
};

// Custom legend styles
const customLegendStyle = {
  marginTop: '1rem',
};

interface PortfolioPerformanceItem {
  id: number;
  name: string;
  currentValue: string;
  initialValue: string;
  return: string;
  benchmark: string;
  difference: string;
}

interface PerformanceClientContentProps {
  portfolioPerformanceTableData: PortfolioPerformanceItem[];
  performanceOverTimeData: {
    date: string;
    portfolioValue: number;
    benchmarkValue: number;
  }[];
  assetAllocationData: {
    name: string;
    value: number;
    riskLevel: string;
  }[];
  riskAllocationData: {
    name: string;
    value: number;
  }[];
}

// Custom formatter for Y-axis values
const formatYAxis = (value: number) => {
  if (value >= 1000) {
    return `$${(value / 1000).toFixed(0)}k`;
  }
  return `$${value}`;
};

// Custom formatter for percentage values
const formatPercent = (value: number) => `${value}%`;

export default function PerformanceClientContent({ 
  portfolioPerformanceTableData,
  performanceOverTimeData: initialPerformanceOverTimeData,
  assetAllocationData,
  riskAllocationData
}: PerformanceClientContentProps) {
  
  const [timePeriod, setTimePeriod] = useState("all");
  const [performanceOverTimeData, setPerformanceOverTimeData] = useState(initialPerformanceOverTimeData);
  const [isGenerating, setIsGenerating] = useState(false);

  // Function to filter data based on time period
  const filterDataByTimePeriod = (data: typeof initialPerformanceOverTimeData, period: string) => {
    if (period === "all") return data;
    
    const now = new Date();
    let cutoffDate = new Date();
    
    switch (period) {
      case "1w":
        cutoffDate.setDate(now.getDate() - 7);
        break;
      case "1m":
        cutoffDate.setMonth(now.getMonth() - 1);
        break;
      case "3m":
        cutoffDate.setMonth(now.getMonth() - 3);
        break;
      case "6m":
        cutoffDate.setMonth(now.getMonth() - 6);
        break;
      case "1y":
        cutoffDate.setFullYear(now.getFullYear() - 1);
        break;
      default:
        return data;
    }

    return data.filter(item => {
      const itemDate = new Date(item.date);
      return itemDate >= cutoffDate;
    });
  };

  // Effect to update filtered data when time period changes
  useEffect(() => {
    const filteredData = filterDataByTimePeriod(initialPerformanceOverTimeData, timePeriod);
    setPerformanceOverTimeData(filteredData);
  }, [timePeriod, initialPerformanceOverTimeData]);

  // Parse string values to numbers for chart data
  const parsedAssetAllocationData = assetAllocationData.map(item => ({
    ...item,
    value: parseFloat(item.value as unknown as string)
  }));

  const parsedRiskAllocationData = riskAllocationData.map(item => ({
    ...item,
    value: parseFloat(item.value as unknown as string)
  }));

  const parsedPortfolioPerformanceTableData = portfolioPerformanceTableData.map(item => ({
    ...item,
    return: parseFloat(String(item.return).replace('%', '')),
    benchmark: parseFloat(String(item.benchmark).replace('%', '')),
  }));

  // console.log("CLIENT_SIDE Performance Over Time Data for Chart:", performanceOverTimeData);
  // console.log("CLIENT_SIDE Asset Allocation Data (Parsed):", parsedAssetAllocationData); 
  // console.log("CLIENT_SIDE Risk Allocation Data (Parsed):", parsedRiskAllocationData);
  // console.log("CLIENT_SIDE Portfolio Performance Table Data (Parsed for BarChart):", parsedPortfolioPerformanceTableData);

  const generateCSV = async () => {
    setIsGenerating(true);
    try {
      const response = await fetch('/api/reports/performance/csv', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          performanceOverTimeData,
          portfolioPerformanceTableData,
          assetAllocationData,
          riskAllocationData,
          timePeriod
        }),
      });

      if (!response.ok) throw new Error('Failed to generate CSV');

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `performance_report_${timePeriod}_${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error generating CSV:', error);
      // You might want to add toast notification here
    } finally {
      setIsGenerating(false);
    }
  };

  const generatePDF = async () => {
    setIsGenerating(true);
    try {
      const response = await fetch('/api/reports/performance/pdf', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          performanceOverTimeData,
          portfolioPerformanceTableData,
          assetAllocationData,
          riskAllocationData,
          timePeriod
        }),
      });

      if (!response.ok) throw new Error('Failed to generate PDF');

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `performance_report_${timePeriod}_${new Date().toISOString().split('T')[0]}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error generating PDF:', error);
      // You might want to add toast notification here
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold tracking-tight">Performance</h1>
        <div className="flex items-center gap-2">
          <Select defaultValue="all" onValueChange={setTimePeriod}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="Time Period" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="1w">1 Week</SelectItem>
              <SelectItem value="1m">1 Month</SelectItem>
              <SelectItem value="3m">3 Months</SelectItem>
              <SelectItem value="6m">6 Months</SelectItem>
              <SelectItem value="1y">1 Year</SelectItem>
              <SelectItem value="all">All Time</SelectItem>
            </SelectContent>
          </Select>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button disabled={isGenerating}>
                <Download className="mr-2 h-4 w-4" />
                {isGenerating ? 'Generating...' : 'Generate Report'}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem onClick={generateCSV}>
                Download as CSV
              </DropdownMenuItem>
              <DropdownMenuItem onClick={generatePDF}>
                Download as PDF
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
      <p className="text-muted-foreground">Track and analyze the performance of your investment portfolios.</p>

      <Tabs defaultValue="overview">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="comparison">Benchmark Comparison</TabsTrigger>
          <TabsTrigger value="allocation">Asset Allocation</TabsTrigger>
        </TabsList>
        <TabsContent value="overview">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-4">
              <CardHeader>
                <CardTitle>Performance Over Time</CardTitle>
                <CardDescription>
                  Track how your portfolios have performed over the selected time period.
                </CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={performanceOverTimeData}>
                    <defs>
                      <linearGradient id="portfolioGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.2}/>
                        <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
                      </linearGradient>
                      <linearGradient id="benchmarkGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="hsl(var(--secondary))" stopOpacity={0.2}/>
                        <stop offset="95%" stopColor="hsl(var(--secondary))" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid 
                      strokeDasharray="3 3" 
                      vertical={false}
                      stroke="hsl(var(--border))"
                    />
                    <XAxis 
                      dataKey="date" 
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: 'hsl(var(--muted-foreground))' }}
                    />
                    <YAxis 
                      tickFormatter={formatYAxis}
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: 'hsl(var(--muted-foreground))' }}
                    />
                    <Tooltip 
                      contentStyle={customTooltipStyle}
                      cursor={{ stroke: 'hsl(var(--muted))' }}
                    />
                    <Legend 
                      wrapperStyle={customLegendStyle}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="portfolioValue" 
                      stroke="hsl(var(--primary))" 
                      fill="url(#portfolioGradient)"
                      strokeWidth={2}
                      name="Portfolio Value"
                      activeDot={{ r: 6, fill: 'hsl(var(--primary))' }}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="benchmarkValue" 
                      stroke="hsl(var(--secondary))" 
                      fill="url(#benchmarkGradient)"
                      strokeWidth={2}
                      name="Benchmark Value"
                      activeDot={{ r: 6, fill: 'hsl(var(--secondary))' }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>Portfolio Returns</CardTitle>
                <CardDescription>Compare returns across different portfolios.</CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={parsedPortfolioPerformanceTableData} barGap={8}>
                    <CartesianGrid 
                      strokeDasharray="3 3" 
                      vertical={false}
                      stroke="hsl(var(--border))"
                    />
                    <XAxis 
                      dataKey="name" 
                      axisLine={false}
                      tickLine={false}
                      tick={{ 
                        fill: 'hsl(var(--muted-foreground))', 
                        fontSize: 12
                      }}
                      interval={0}
                      angle={-35}
                      textAnchor="end"
                      height={60}
                    />
                    <YAxis 
                      tickFormatter={formatPercent}
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: 'hsl(var(--muted-foreground))' }}
                    />
                    <Tooltip 
                      contentStyle={customTooltipStyle}
                      cursor={{ fill: 'hsl(var(--muted) / 0.1)' }}
                      formatter={(value: number) => [
                        <span key="value" className="font-medium text-foreground">${value.toFixed(1)}%</span>, 
                        null
                      ]}
                    />
                    <Legend 
                      wrapperStyle={customLegendStyle}
                      verticalAlign="top"
                      height={36}
                    />
                    <Bar 
                      dataKey="return" 
                      fill={COLORS[0]}
                      name="Return (%)"
                      radius={[4, 4, 0, 0]}
                    />
                    <Bar 
                      dataKey="benchmark" 
                      fill={COLORS[1]}
                      name="Benchmark (%)"
                      radius={[4, 4, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
        <TabsContent value="comparison">
          <Card>
            <CardHeader>
              <CardTitle>Benchmark Comparison</CardTitle>
              <CardDescription>Compare your portfolio performance against relevant benchmarks.</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Portfolio</TableHead>
                    <TableHead>Current Value</TableHead>
                    <TableHead>Initial Value</TableHead>
                    <TableHead>Return</TableHead>
                    <TableHead>Benchmark</TableHead>
                    <TableHead>Difference</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {portfolioPerformanceTableData.map((portfolio) => (
                    <TableRow key={portfolio.id}>
                      <TableCell className="font-medium">{portfolio.name}</TableCell>
                      <TableCell>{portfolio.currentValue}</TableCell>
                      <TableCell>{portfolio.initialValue}</TableCell>
                      <TableCell className="text-green-600">{portfolio.return}</TableCell>
                      <TableCell>{portfolio.benchmark}</TableCell>
                      <TableCell className={portfolio.difference.startsWith("+") ? "text-green-600" : "text-red-600"}>
                        {portfolio.difference}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="allocation">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-4">
              <CardHeader>
                <CardTitle>Current Asset Allocation</CardTitle>
                <CardDescription>
                  View how your assets are currently allocated across different categories.
                </CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={parsedAssetAllocationData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={100}
                      innerRadius={60}
                      fill="hsl(var(--primary))"
                      dataKey="value"
                      nameKey="name"
                      label={({ percent }) => `${(percent * 100).toFixed(0)}%`}
                      paddingAngle={3}
                    >
                      {parsedAssetAllocationData.map((entry, index) => (
                        <Cell 
                          key={`cell-${index}`} 
                          fill={COLORS[index % COLORS.length]}
                          className="transition-opacity duration-200 hover:opacity-80"
                          stroke="transparent"
                        />
                      ))}
                    </Pie>
                    <Tooltip 
                      contentStyle={customTooltipStyle}
                      formatter={(value: number, name: string) => [
                        <span key="value" className="font-medium text-foreground">${value.toLocaleString()}</span>,
                        <span key="name" className="text-sm text-muted-foreground">{name}</span>
                      ]}
                    />
                    <Legend 
                      layout="vertical"
                      align="left"
                      verticalAlign="middle"
                      formatter={(value: string) => (
                        <span className="text-sm text-muted-foreground">{value}</span>
                      )}
                      iconSize={10}
                      iconType="circle"
                      wrapperStyle={{ paddingLeft: '10px' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>Allocation by Risk Level</CardTitle>
                <CardDescription>
                  See how your investments are distributed across different risk levels.
                </CardDescription>
              </CardHeader>
              <CardContent className="pl-2">
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={parsedRiskAllocationData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={100}
                      innerRadius={60}
                      fill="hsl(var(--primary))"
                      dataKey="value"
                      nameKey="name"
                      label={({ percent }) => `${(percent * 100).toFixed(0)}%`}
                      paddingAngle={3}
                    >
                      {parsedRiskAllocationData.map((entry, index) => (
                        <Cell 
                          key={`cell-${index}`} 
                          fill={RISK_COLORS[index]}
                          className="transition-opacity duration-200 hover:opacity-80"
                          stroke="transparent"
                        />
                      ))}
                    </Pie>
                    <Tooltip 
                      contentStyle={customTooltipStyle}
                      formatter={(value: number, name: string) => [
                        <span key="value" className="font-medium text-foreground">${value.toLocaleString()}</span>,
                        <span key="name" className="text-sm text-muted-foreground">{name}</span>
                      ]}
                    />
                    <Legend 
                      layout="vertical"
                      align="left"
                      verticalAlign="middle"
                      formatter={(value: string) => (
                        <span className="text-sm text-muted-foreground">{value}</span>
                      )}
                      iconSize={10}
                      iconType="circle"
                      wrapperStyle={{ paddingLeft: '10px' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
} 