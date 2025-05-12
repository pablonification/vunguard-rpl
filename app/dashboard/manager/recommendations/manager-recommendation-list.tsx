'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { useToast } from '@/components/ui/use-toast';
import { formatCurrency } from '@/lib/utils';
import { type Recommendation, RecommendationStatus, RecommendationType } from '@/lib/db/models/recommendation';
import { format, formatDistanceToNow } from 'date-fns';
import { Check, X, AlertTriangle, Eye } from 'lucide-react';

interface ManagerRecommendationListProps {
  initialRecommendations: Recommendation[];
}

export default function ManagerRecommendationList({ initialRecommendations }: ManagerRecommendationListProps) {
  const [recommendations, setRecommendations] = useState(initialRecommendations);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [isLoading, setIsLoading] = useState<number | null>(null);
  const [selectedRecommendation, setSelectedRecommendation] = useState<Recommendation | null>(null);
  const router = useRouter();
  const { toast } = useToast();

  const filteredRecommendations = statusFilter === 'all'
    ? recommendations
    : recommendations.filter(rec => rec.status === statusFilter);

  const handleStatusUpdate = async (id: number, newStatus: string) => {
    try {
      setIsLoading(id);
      const response = await fetch(`/api/recommendations/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: newStatus }),
      });

      if (!response.ok) {
        throw new Error('Failed to update recommendation status');
      }

      const updatedRecommendation = await response.json();
      setRecommendations(recommendations.map(rec =>
        rec.id === id ? updatedRecommendation : rec
      ));

      toast({
        title: 'Success',
        description: `Recommendation ${newStatus.toLowerCase()} successfully`,
      });

      router.refresh();
    } catch (error) {
      console.error('Error updating recommendation:', error);
      toast({
        title: 'Error',
        description: 'Failed to update recommendation status',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(null);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case RecommendationStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300';
      case RecommendationStatus.APPROVED:
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      case RecommendationStatus.REJECTED:
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
      case RecommendationStatus.IMPLEMENTED:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case RecommendationType.BUY:
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
      case RecommendationType.SELL:
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
      case RecommendationType.HOLD:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300';
    }
  };

  // Safe date formatting function
  const formatDate = (dateString: string | Date | null | undefined) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'Invalid date';
      return formatDistanceToNow(date, { addSuffix: true });
    } catch (error) {
      console.error('Date formatting error:', error, dateString);
      return 'Invalid date';
    }
  };

  return (
    <>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
          <div>
            <CardTitle>Analyst Recommendations</CardTitle>
            <CardDescription>Review and approve analyst investment recommendations</CardDescription>
          </div>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              {Object.entries(RecommendationStatus).map(([key, value]) => (
                <SelectItem key={value} value={value}>
                  {key.charAt(0) + key.slice(1).toLowerCase()}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead>Analyst</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Target Price</TableHead>
                  <TableHead>Current Price</TableHead>
                  <TableHead>Confidence</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredRecommendations.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">
                      No recommendations found
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredRecommendations.map((recommendation) => (
                    <TableRow key={recommendation.id}>
                      <TableCell className="font-medium">
                        {recommendation.productName}
                      </TableCell>
                      <TableCell>{recommendation.analystName}</TableCell>
                      <TableCell>
                        <Badge className={getTypeColor(recommendation.type)}>
                          {recommendation.type.toUpperCase()}
                        </Badge>
                      </TableCell>
                      <TableCell>{formatCurrency(recommendation.targetPrice)}</TableCell>
                      <TableCell>{formatCurrency(recommendation.currentPrice)}</TableCell>
                      <TableCell>
                        {'⭐'.repeat(recommendation.confidence)}
                      </TableCell>
                      <TableCell>
                        <Badge className={getStatusColor(recommendation.status)}>
                          {recommendation.status.toUpperCase()}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formatDate(recommendation.createdAt)}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            className="text-blue-600 hover:text-blue-700"
                            onClick={() => setSelectedRecommendation(recommendation)}
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          
                          {recommendation.status === RecommendationStatus.PENDING && (
                            <>
                              <Button
                                size="sm"
                                variant="outline"
                                className="text-green-600 hover:text-green-700"
                                onClick={() => handleStatusUpdate(recommendation.id, RecommendationStatus.APPROVED)}
                                disabled={isLoading === recommendation.id}
                              >
                                <Check className="h-4 w-4" />
                              </Button>
                              <Button
                                size="sm"
                                variant="outline"
                                className="text-red-600 hover:text-red-700"
                                onClick={() => handleStatusUpdate(recommendation.id, RecommendationStatus.REJECTED)}
                                disabled={isLoading === recommendation.id}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </>
                          )}
                          {recommendation.status === RecommendationStatus.APPROVED && (
                            <Button
                              size="sm"
                              variant="outline"
                              className="text-blue-600 hover:text-blue-700"
                              onClick={() => handleStatusUpdate(recommendation.id, RecommendationStatus.IMPLEMENTED)}
                              disabled={isLoading === recommendation.id}
                            >
                              <Check className="h-4 w-4 mr-1" />
                              Implement
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* Recommendation Detail Dialog */}
      <Dialog open={!!selectedRecommendation} onOpenChange={(open) => !open && setSelectedRecommendation(null)}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Recommendation Details</DialogTitle>
            <DialogDescription>
              Full details for recommendation #{selectedRecommendation?.id}
            </DialogDescription>
          </DialogHeader>

          {selectedRecommendation && (
            <div className="space-y-4 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Product</h3>
                  <p className="text-lg font-semibold">{selectedRecommendation.productName}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Analyst</h3>
                  <p className="text-lg">{selectedRecommendation.analystName}</p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Recommendation Type</h3>
                  <Badge className={`mt-1 ${getTypeColor(selectedRecommendation.type)}`}>
                    {selectedRecommendation.type.toUpperCase()}
                  </Badge>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Status</h3>
                  <Badge className={`mt-1 ${getStatusColor(selectedRecommendation.status)}`}>
                    {selectedRecommendation.status.toUpperCase()}
                  </Badge>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Confidence</h3>
                  <p className="text-lg">{'⭐'.repeat(selectedRecommendation.confidence)}</p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Current Price</h3>
                  <p className="text-lg">{formatCurrency(selectedRecommendation.currentPrice)}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Target Price</h3>
                  <p className="text-lg">{formatCurrency(selectedRecommendation.targetPrice)}</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Price Change</h3>
                  <p className={`text-lg ${selectedRecommendation.targetPrice > selectedRecommendation.currentPrice ? 'text-green-600' : 'text-red-600'}`}>
                    {((selectedRecommendation.targetPrice - selectedRecommendation.currentPrice) / selectedRecommendation.currentPrice * 100).toFixed(2)}%
                  </p>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Timeframe</h3>
                <p className="text-lg">{selectedRecommendation.timeframe.replace('_', ' ')}</p>
              </div>

              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Rationale</h3>
                <div className="mt-1 rounded-md border p-4 bg-muted/40">
                  <p>{selectedRecommendation.rationale}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {selectedRecommendation.technicalAnalysis && (
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Technical Analysis</h3>
                    <div className="mt-1 rounded-md border p-4 bg-muted/40 h-40 overflow-y-auto">
                      <p>{selectedRecommendation.technicalAnalysis}</p>
                    </div>
                  </div>
                )}
                
                {selectedRecommendation.fundamentalAnalysis && (
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Fundamental Analysis</h3>
                    <div className="mt-1 rounded-md border p-4 bg-muted/40 h-40 overflow-y-auto">
                      <p>{selectedRecommendation.fundamentalAnalysis}</p>
                    </div>
                  </div>
                )}
                
                {selectedRecommendation.risks && (
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Risks</h3>
                    <div className="mt-1 rounded-md border p-4 bg-muted/40 h-40 overflow-y-auto">
                      <p>{selectedRecommendation.risks}</p>
                    </div>
                  </div>
                )}
              </div>

              <div className="grid grid-cols-3 gap-4 text-sm text-muted-foreground">
                <div>
                  <span>Created: </span>
                  <span>{selectedRecommendation.createdAt ? format(new Date(selectedRecommendation.createdAt), 'PPP p') : 'N/A'}</span>
                </div>
                <div>
                  <span>Updated: </span>
                  <span>{selectedRecommendation.updatedAt ? format(new Date(selectedRecommendation.updatedAt), 'PPP p') : 'N/A'}</span>
                </div>
                <div>
                  <span>Implemented: </span>
                  <span>{selectedRecommendation.implementedAt ? format(new Date(selectedRecommendation.implementedAt), 'PPP p') : 'N/A'}</span>
                </div>
              </div>
            </div>
          )}

          <DialogFooter>
            {selectedRecommendation?.status === RecommendationStatus.PENDING && (
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  className="border-green-600 text-green-600 hover:bg-green-50 hover:text-green-700"
                  onClick={() => {
                    handleStatusUpdate(selectedRecommendation.id, RecommendationStatus.APPROVED);
                    setSelectedRecommendation(null);
                  }}
                  disabled={isLoading === selectedRecommendation.id}
                >
                  <Check className="mr-2 h-4 w-4" />
                  Approve
                </Button>
                <Button
                  variant="outline"
                  className="border-red-600 text-red-600 hover:bg-red-50 hover:text-red-700"
                  onClick={() => {
                    handleStatusUpdate(selectedRecommendation.id, RecommendationStatus.REJECTED);
                    setSelectedRecommendation(null);
                  }}
                  disabled={isLoading === selectedRecommendation.id}
                >
                  <X className="mr-2 h-4 w-4" />
                  Reject
                </Button>
              </div>
            )}
            {selectedRecommendation?.status === RecommendationStatus.APPROVED && (
              <Button
                variant="outline"
                className="border-blue-600 text-blue-600 hover:bg-blue-50 hover:text-blue-700"
                onClick={() => {
                  handleStatusUpdate(selectedRecommendation.id, RecommendationStatus.IMPLEMENTED);
                  setSelectedRecommendation(null);
                }}
                disabled={isLoading === selectedRecommendation.id}
              >
                <Check className="mr-2 h-4 w-4" />
                Mark as Implemented
              </Button>
            )}
            <Button
              variant="outline"
              onClick={() => setSelectedRecommendation(null)}
            >
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
} 