"use client";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';

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
const RISK_COLORS = {
  'High': 'hsl(0 84% 60%)',     // Red
  'Medium': 'hsl(48 96% 53%)',  // Yellow
  'Low': 'hsl(142 70% 45%)',    // Green
};

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

// Custom formatter for Y-axis values
const formatYAxis = (value: number) => {
  if (value >= 1000) {
    return `$${(value / 1000).toFixed(0)}k`;
  }
  return `$${value}`;
};

interface PerformanceData {
  date: string;
  portfolioValue: number;
  benchmarkValue: number;
}

interface AssetAllocation {
  name: string;
  value: number;
  riskLevel: string;
}

interface DashboardChartsProps {
  performanceData: PerformanceData[];
  assetAllocation: AssetAllocation[];
}

export default function DashboardCharts({ performanceData, assetAllocation }: DashboardChartsProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
      <Card className="col-span-4">
        <CardHeader>
          <CardTitle>Portfolio Performance</CardTitle>
          <CardDescription>
            Track how your portfolios have performed over time
          </CardDescription>
        </CardHeader>
        <CardContent className="pl-2">
          {performanceData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={performanceData}>
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
          ) : (
            <div className="h-[300px] flex items-center justify-center">
              <span className="text-muted-foreground">No performance data available</span>
            </div>
          )}
        </CardContent>
      </Card>
      
      <Card className="col-span-3">
        <CardHeader>
          <CardTitle>Asset Allocation</CardTitle>
          <CardDescription>
            Distribution of your investments by asset type
          </CardDescription>
        </CardHeader>
        <CardContent className="pl-2">
          {assetAllocation.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={assetAllocation}
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
                  {assetAllocation.map((entry, index) => (
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
                  formatter={(value: string, entry: any, index: number) => (
                    <span className="text-sm text-muted-foreground">{value}</span>
                  )}
                  iconSize={10}
                  iconType="circle"
                  wrapperStyle={{ paddingLeft: '10px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center">
              <span className="text-muted-foreground">No asset allocation data available</span>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
} 