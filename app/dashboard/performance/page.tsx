import { DashboardLayout } from "@/components/dashboard-layout"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { BarChart, LineChart, PieChart } from "lucide-react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

export default function PerformancePage() {
  // Mock data - in a real app, this would come from the database
  const portfolioPerformance = [
    {
      id: 1,
      name: "Retirement Fund",
      currentValue: "$125,000.00",
      initialValue: "$100,000.00",
      return: "+25.0%",
      benchmark: "+18.5%",
      difference: "+6.5%",
    },
    {
      id: 2,
      name: "Education Fund",
      currentValue: "$45,000.00",
      initialValue: "$40,000.00",
      return: "+12.5%",
      benchmark: "+10.2%",
      difference: "+2.3%",
    },
    {
      id: 3,
      name: "Emergency Fund",
      currentValue: "$15,000.00",
      initialValue: "$15,000.00",
      return: "+0.0%",
      benchmark: "+2.1%",
      difference: "-2.1%",
    },
    {
      id: 4,
      name: "Growth Portfolio",
      currentValue: "$78,000.00",
      initialValue: "$60,000.00",
      return: "+30.0%",
      benchmark: "+22.7%",
      difference: "+7.3%",
    },
    {
      id: 5,
      name: "Income Portfolio",
      currentValue: "$92,000.00",
      initialValue: "$85,000.00",
      return: "+8.2%",
      benchmark: "+7.4%",
      difference: "+0.8%",
    },
  ]

  return (
    <DashboardLayout>
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold tracking-tight">Performance</h1>
          <div className="flex items-center gap-2">
            <Select defaultValue="1m">
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
            <Button>Generate Report</Button>
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
                  <div className="h-[300px] flex items-center justify-center">
                    <LineChart className="h-16 w-16 text-muted-foreground" />
                    <span className="ml-4 text-muted-foreground">Performance chart will be displayed here</span>
                  </div>
                </CardContent>
              </Card>
              <Card className="col-span-3">
                <CardHeader>
                  <CardTitle>Portfolio Returns</CardTitle>
                  <CardDescription>Compare returns across different portfolios.</CardDescription>
                </CardHeader>
                <CardContent className="pl-2">
                  <div className="h-[300px] flex items-center justify-center">
                    <BarChart className="h-16 w-16 text-muted-foreground" />
                    <span className="ml-4 text-muted-foreground">Returns chart will be displayed here</span>
                  </div>
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
                    {portfolioPerformance.map((portfolio) => (
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
                  <div className="h-[300px] flex items-center justify-center">
                    <PieChart className="h-16 w-16 text-muted-foreground" />
                    <span className="ml-4 text-muted-foreground">Asset allocation chart will be displayed here</span>
                  </div>
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
                  <div className="h-[300px] flex items-center justify-center">
                    <PieChart className="h-16 w-16 text-muted-foreground" />
                    <span className="ml-4 text-muted-foreground">Risk allocation chart will be displayed here</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  )
}
