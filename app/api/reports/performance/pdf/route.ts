import { NextResponse } from 'next/server';
import PDFDocument from 'pdfkit';

// Helper function to robustly format currency
function formatCurrency(value: string | number | null | undefined): string {
  try {
    if (value === null || value === undefined || String(value).trim() === '') return '';
    
    // Attempt to clean and parse the value
    const cleanedString = String(value).replace(/[^0-9.-]+/g, "");
    const numValue = parseFloat(cleanedString);

    if (isNaN(numValue)) return String(value); // Return original string if parsing fails but it's not empty

    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(numValue);
  } catch (e) {
    // console.error("Error formatting currency for value:", value, e);
    return String(value); // Return original value on error
  }
}

// Helper function to create tables with page breaks and header redrawing
function createTable(doc: PDFKit.PDFDocument, columns: { header: string; width: number }[], dataRows: any[][], sectionTitleForDebug: string) {
    const cellPadding = 5;
    const lineHeight = 18; // Suitable for font size 10
    const startX = doc.page.margins.left;
    let currentY = doc.y;

    // console.log(`DEBUG: Creating table for ${sectionTitleForDebug}, startY: ${currentY}, data rows: ${dataRows.length}`);

    const drawRow = (rowData: any[], isHeader: boolean) => {
        if (isHeader) {
            doc.font('Times-Bold').fontSize(10);
        } else {
            doc.font('Times-Roman').fontSize(10);
        }

        let currentX = startX;
        rowData.forEach((cellContent, colIndex) => {
            if (colIndex < columns.length) { // Ensure we don't try to access non-existent column
                const columnWidth = columns[colIndex].width;
                doc.text(String(cellContent === null || cellContent === undefined ? '' : cellContent), currentX, currentY, {
                    width: columnWidth,
                    align: 'left',
                    lineBreak: false // Try to keep content on one line per cell
                });
                currentX += columnWidth + cellPadding;
            }
        });
        currentY += lineHeight;
    };

    // Check for page break before drawing headers
    if (currentY + lineHeight > doc.page.height - doc.page.margins.bottom) {
        doc.addPage();
        currentY = doc.page.margins.top;
        // console.log(`DEBUG: ${sectionTitleForDebug} - Page break before headers, new Y: ${currentY}`);
    }
    // Draw headers
    drawRow(columns.map(c => c.header), true);

    // Draw data rows
    if (dataRows && dataRows.length > 0) {
        dataRows.forEach((row) => {
            // Check for page break before drawing a data row
            if (currentY + lineHeight > doc.page.height - doc.page.margins.bottom) {
                doc.addPage();
                currentY = doc.page.margins.top;
                // console.log(`DEBUG: ${sectionTitleForDebug} - Page break before data row, new Y: ${currentY}`);
                // Redraw headers on new page
                drawRow(columns.map(c => c.header), true);
            }
            drawRow(row, false);
        });
    } else {
        // Optional: Add a line indicating no data if the table is empty
        // doc.font('Times-Roman').fontSize(10).text('No data available for this section.', startX, currentY);
        // currentY += lineHeight;
        // console.log(`DEBUG: ${sectionTitleForDebug} - No data rows.`);
    }
    doc.y = currentY; // Update doc.y to position after the table
    // console.log(`DEBUG: Finished table for ${sectionTitleForDebug}, endY: ${doc.y}`);
}

export async function POST(req: Request) {
  try {
    const bodyData = await req.json();
    
    const doc = new PDFDocument({ 
      margin: 50, 
      font: 'Times-Roman'
    });
    const chunks: Buffer[] = [];
    doc.on('data', chunk => chunks.push(chunk));
    const pdfPromise = new Promise<Buffer>((resolve, reject) => {
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', reject);
    });

    const printableWidth = doc.page.width - doc.page.margins.left - doc.page.margins.right;

    // Add title
    doc.font('Times-Bold').fontSize(20)
       .text('Investment Portfolio Performance Report', doc.page.margins.left, doc.y, { align: 'center', width: printableWidth })
       .font('Times-Roman').fontSize(12)
       .text(`Time Period: ${bodyData.timePeriod}`, { align: 'center', width: printableWidth })
       .moveDown(2);

    // Performance Over Time Section
    doc.font('Times-Bold').fontSize(16).text('Performance Over Time', doc.page.margins.left, doc.y, {width: printableWidth, align: 'left' })
       .font('Times-Roman').fontSize(12).moveDown(0.5);
    createTable(doc, [
      { header: 'Date', width: 120 }, // Adjusted width slightly
      { header: 'Portfolio Value', width: 150 },
      { header: 'Benchmark Value', width: 150 }
    ], (bodyData.performanceOverTimeData || []).map((item: any) => [
      item.date,
      formatCurrency(item.portfolioValue),
      formatCurrency(item.benchmarkValue)
    ]), 'Performance Over Time');
    doc.moveDown(2);

    // Portfolio Performance Section
    doc.font('Times-Bold').fontSize(16).text('Portfolio Performance', doc.page.margins.left, doc.y, {width: printableWidth, align: 'left' })
        .font('Times-Roman').fontSize(12).moveDown(0.5);
    createTable(doc, [
      // Adjusted widths to fit within ~495 points printable area
      // Target sum of widths + (num_cols-1)*padding <= printableWidth
      // 465 + (6-1)*5 = 465 + 25 = 490. This should fit.
      { header: 'Portfolio', width: 120 },
      { header: 'Current Value', width: 85 }, 
      { header: 'Initial Value', width: 85 }, 
      { header: 'Return (%)', width: 70 }, 
      { header: 'Benchmark (%)', width: 70 },
      { header: 'Difference (%)', width: 75 }
    ], (bodyData.portfolioPerformanceTableData || []).map((item: any) => [
      item.name,
      formatCurrency(item.currentValue),
      formatCurrency(item.initialValue),
      item.return,
      item.benchmark,
      item.difference
    ]), 'Portfolio Performance');
    doc.moveDown(2);

    // Asset Allocation Section
    // Ensure x is reset by drawing title with full width
    doc.font('Times-Bold').fontSize(16).text('Asset Allocation', doc.page.margins.left, doc.y, {width: printableWidth, align: 'left' })
        .font('Times-Roman').fontSize(12).moveDown(0.5);
    createTable(doc, [
      { header: 'Asset', width: 180 }, // Adjusted width slightly
      { header: 'Value', width: 130 },
      { header: 'Risk Level', width: 110 }
    ], (bodyData.assetAllocationData || []).map((item: any) => [
      item.name,
      formatCurrency(item.value),
      item.riskLevel
    ]), 'Asset Allocation');
    doc.moveDown(2);

    // Risk Allocation Section
    doc.font('Times-Bold').fontSize(16).text('Risk Allocation', doc.page.margins.left, doc.y, {width: printableWidth, align: 'left' })
        .font('Times-Roman').fontSize(12).moveDown(0.5);
    createTable(doc, [
      { header: 'Risk Level', width: 200 },
      { header: 'Value', width: 150 }
    ], (bodyData.riskAllocationData || []).map((item: any) => [
      item.name,
      formatCurrency(item.value)
    ]), 'Risk Allocation');
    doc.moveDown(2);

    // Page Numbering (remains the same)
    const pageRange = doc.bufferedPageRange();
    const totalPages = pageRange.count;
    if (totalPages > 0) {
      for (let i = 0; i < totalPages; i++) {
        const pageIndexToSwitchTo = pageRange.start + i;
        doc.switchToPage(pageIndexToSwitchTo); 
        doc.font('Times-Roman').fontSize(10)
           .text(
             `Page ${i + 1} of ${totalPages}`,
             doc.page.margins.left, 
             doc.page.height - doc.page.margins.bottom + 10, 
             { align: 'center', width: printableWidth }
           );
      }
    }

    doc.end();
    const pdfBuffer = await pdfPromise;

    return new NextResponse(pdfBuffer, {
      headers: {
        'Content-Type': 'application/pdf',
        'Content-Disposition': `attachment; filename=performance_report_${bodyData.timePeriod}_${new Date().toISOString().split('T')[0]}.pdf`
      }
    });
  } catch (error) {
    console.error('Error generating PDF:', error);
    const errorMessage = error instanceof Error ? error.message : 'Unknown error';
    return new NextResponse(`Error generating PDF report: ${errorMessage}`, { status: 500 });
  }
} 