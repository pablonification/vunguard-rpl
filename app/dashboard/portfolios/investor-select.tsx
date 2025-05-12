'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';

interface Investor {
  id: number;
  full_name: string;
  email: string;
}

interface InvestorSelectProps {
  currentInvestorId: number;
}

export default function InvestorSelect({ currentInvestorId }: InvestorSelectProps) {
  const [investors, setInvestors] = useState<Investor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedInvestorId, setSelectedInvestorId] = useState<string>(currentInvestorId.toString());
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    async function loadInvestors() {
      try {
        setIsLoading(true);
        const response = await fetch('/api/investors');
        if (!response.ok) {
          throw new Error('Failed to fetch investors');
        }
        const data = await response.json();
        setInvestors(data);
        
        // Check if there's an investor ID in the URL
        const investorIdFromUrl = searchParams.get('investorId');
        if (investorIdFromUrl) {
          setSelectedInvestorId(investorIdFromUrl);
        }
      } catch (error) {
        console.error('Failed to load investors:', error);
      } finally {
        setIsLoading(false);
      }
    }
    
    loadInvestors();
  }, [searchParams, currentInvestorId]);

  const handleInvestorChange = (investorId: string) => {
    setSelectedInvestorId(investorId);
    router.push(`/dashboard/portfolios?investorId=${investorId}`);
  };

  return (
    <Card className="mb-6">
      <CardHeader className="pb-3">
        <CardTitle>Investor Selection</CardTitle>
        <CardDescription>
          Select an investor to view their portfolio data
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Select
          value={selectedInvestorId}
          onValueChange={handleInvestorChange}
          disabled={isLoading}
        >
          <SelectTrigger className="w-full max-w-xs">
            <SelectValue placeholder={isLoading ? "Loading investors..." : "Select an investor"} />
          </SelectTrigger>
          <SelectContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-2">
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Loading investors...
              </div>
            ) : investors.length > 0 ? (
              investors.map((investor) => (
                <SelectItem key={investor.id} value={investor.id.toString()}>
                  {investor.full_name} ({investor.email})
                </SelectItem>
              ))
            ) : (
              <SelectItem value="none" disabled>
                No investors available
              </SelectItem>
            )}
          </SelectContent>
        </Select>
      </CardContent>
    </Card>
  );
} 