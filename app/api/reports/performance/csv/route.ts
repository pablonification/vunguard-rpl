import { NextResponse } from 'next/server';
import { parse } from 'json2csv';

export async function POST(req: Request) {
  try {
    const data = await req.json();
    
    // Prepare data for CSV
    const csvData = {
      performanceOverTime: data.performanceOverTimeData.map((item: any) => ({
        Date: item.date,
        'Portfolio Value': item.portfolioValue,
        'Benchmark Value': item.benchmarkValue
      })),
      portfolioPerformance: data.portfolioPerformanceTableData.map((item: any) => ({
        Portfolio: item.name,
        'Current Value': item.currentValue,
        'Initial Value': item.initialValue,
        'Return (%)': item.return,
        'Benchmark (%)': item.benchmark,
        'Difference (%)': item.difference
      })),
      assetAllocation: data.assetAllocationData.map((item: any) => ({
        Asset: item.name,
        Value: item.value,
        'Risk Level': item.riskLevel
      })),
      riskAllocation: data.riskAllocationData.map((item: any) => ({
        'Risk Level': item.name,
        Value: item.value
      }))
    };

    // Convert each section to CSV
    const performanceOverTimeCsv = parse(csvData.performanceOverTime);
    const portfolioPerformanceCsv = parse(csvData.portfolioPerformance);
    const assetAllocationCsv = parse(csvData.assetAllocation);
    const riskAllocationCsv = parse(csvData.riskAllocation);

    // Combine all sections with headers
    const fullCsv = [
      'PERFORMANCE OVER TIME',
      performanceOverTimeCsv,
      '\n\nPORTFOLIO PERFORMANCE',
      portfolioPerformanceCsv,
      '\n\nASSET ALLOCATION',
      assetAllocationCsv,
      '\n\nRISK ALLOCATION',
      riskAllocationCsv
    ].join('\n');

    // Return CSV file
    return new NextResponse(fullCsv, {
      headers: {
        'Content-Type': 'text/csv',
        'Content-Disposition': `attachment; filename=performance_report_${data.timePeriod}_${new Date().toISOString().split('T')[0]}.csv`
      }
    });
  } catch (error) {
    console.error('Error generating CSV:', error);
    return new NextResponse('Error generating CSV report', { status: 500 });
  }
} 