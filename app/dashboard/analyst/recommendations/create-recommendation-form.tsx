'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/components/ui/use-toast';
import { Loader2 } from 'lucide-react';
import {
  recommendationSchema,
  RecommendationType,
  type CreateRecommendationInput
} from '@/lib/db/models/recommendation';
import { getSession } from '@/lib/auth';
import type { Product } from '@/lib/db/models/product';

interface CreateRecommendationFormProps {
  onSuccess: () => void;
}

export default function CreateRecommendationForm({ onSuccess }: CreateRecommendationFormProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [sessionLoaded, setSessionLoaded] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(true);
  const [formError, setFormError] = useState<string | null>(null);
  const [analystId, setAnalystId] = useState<number | null>(null);
  const router = useRouter();
  const { toast } = useToast();

  // Setup form without analystId first - we'll update it when we get the session
  const form = useForm<CreateRecommendationInput>({
    resolver: zodResolver(recommendationSchema),
    defaultValues: {
      type: RecommendationType.BUY,
      confidence: 3,
      timeframe: 'medium_term',
      currentPrice: 0,
      targetPrice: 0,
      rationale: '',
    },
  });

  // Load session data first to get the analyst ID
  useEffect(() => {
    const loadSession = async () => {
      try {
        const session = await getSession();
        if (session && session.id) {
          // Convert id to number if it's a string
          const numericId = typeof session.id === 'string' ? parseInt(session.id) : Number(session.id);
          setAnalystId(numericId);
          
          // Set the analystId in the form
          form.setValue('analystId', numericId);
          console.log('Set analyst ID in form:', numericId);
        } else {
          setFormError('Session data is missing or invalid');
        }
      } catch (error) {
        console.error('Error loading session:', error);
        setFormError('Failed to load user session');
      } finally {
        setSessionLoaded(true);
      }
    };

    loadSession();
  }, [form]);

  // Load products after session is loaded
  useEffect(() => {
    if (!sessionLoaded) return;

    const fetchProducts = async () => {
      try {
        setIsLoadingProducts(true);
        const response = await fetch('/api/products');
        if (!response.ok) {
          throw new Error('Failed to fetch products');
        }
        const data = await response.json();
        console.log('Products fetched:', data);
        setProducts(data);
      } catch (error) {
        console.error('Error loading products:', error);
        toast({
          title: 'Error',
          description: 'Failed to load products. Please try again.',
          variant: 'destructive',
        });
      } finally {
        setIsLoadingProducts(false);
      }
    };

    fetchProducts();
  }, [sessionLoaded, toast]);

  const onSubmit = async (data: CreateRecommendationInput) => {
    console.log('Form submitted with data:', data);
    
    // Verify analystId is set
    if (!data.analystId) {
      console.error('Analyst ID is missing in form data');
      setFormError('Analyst ID is missing. Please refresh the page and try again.');
      return;
    }
    
    setFormError(null);
    
    try {
      setIsLoading(true);
      
      // Clone the data to avoid modifying the original form data
      const requestData = { 
        ...data,
        // Make sure numeric fields are properly passed as numbers
        analystId: Number(data.analystId),
        productId: Number(data.productId),
        currentPrice: Number(data.currentPrice),
        targetPrice: Number(data.targetPrice),
        confidence: Number(data.confidence)
      };
      
      console.log('Request data after formatting:', requestData);
      
      const response = await fetch('/api/recommendations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      });

      console.log('Response received:', response.status, response.statusText);
      
      if (!response.ok) {
        let errorMessage = 'Failed to create recommendation';
        
        try {
          // Clone the response and get text and JSON separately
          const clonedResponse = response.clone();
          const responseText = await response.text();
          console.log('Error response text:', responseText);
          
          try {
            const errorData = JSON.parse(responseText);
            if (errorData && errorData.message) {
              errorMessage = errorData.message;
            }
          } catch (jsonError) {
            // If JSON parsing fails, use the raw text
            if (responseText) {
              errorMessage = responseText;
            }
          }
        } catch (textError) {
          console.error('Error reading response:', textError);
        }
        
        setFormError(errorMessage);
        toast({
          title: 'Error',
          description: errorMessage,
          variant: 'destructive',
        });
        return;
      }

      try {
        const responseData = await response.json();
        console.log('Success response data:', responseData);
      } catch (jsonError) {
        console.log('Response was not JSON, but request was successful');
      }

      toast({
        title: 'Success',
        description: 'Recommendation created successfully',
      });

      console.log('Refreshing page and closing dialog');
      router.refresh();
      onSuccess();
    } catch (error) {
      console.error('Error creating recommendation:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to create recommendation. Please try again.';
      setFormError(errorMessage);
      toast({
        title: 'Error',
        description: errorMessage,
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        {formError && (
          <div className="bg-destructive/15 text-destructive text-sm p-3 rounded-md">
            {formError}
          </div>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="productId"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Product</FormLabel>
                <Select
                  value={field.value?.toString()}
                  onValueChange={(value) => field.onChange(parseInt(value))}
                  disabled={isLoadingProducts || !sessionLoaded}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder={isLoadingProducts ? "Loading products..." : "Select a product"} />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {isLoadingProducts ? (
                      <SelectItem value="loading" disabled>
                        <div className="flex items-center">
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Loading products...
                        </div>
                      </SelectItem>
                    ) : products.length > 0 ? (
                      products.map((product) => (
                        <SelectItem key={product.id} value={product.id.toString()}>
                          {product.name} ({product.code})
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="none" disabled>
                        No products available
                      </SelectItem>
                    )}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="type"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Recommendation Type</FormLabel>
                <Select
                  value={field.value}
                  onValueChange={field.onChange}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {Object.entries(RecommendationType).map(([key, value]) => (
                      <SelectItem key={value} value={value}>
                        {key}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="currentPrice"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Current Price</FormLabel>
                <FormControl>
                  <Input
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    {...field}
                    value={field.value || ''}
                    onChange={(e) => {
                      const value = e.target.value === '' ? 0 : parseFloat(e.target.value);
                      field.onChange(value);
                    }}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="targetPrice"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Target Price</FormLabel>
                <FormControl>
                  <Input
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    {...field}
                    value={field.value || ''}
                    onChange={(e) => {
                      const value = e.target.value === '' ? 0 : parseFloat(e.target.value);
                      field.onChange(value);
                    }}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="confidence"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Confidence Level (1-5)</FormLabel>
                <Select
                  value={field.value?.toString()}
                  onValueChange={(value) => field.onChange(parseInt(value))}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select confidence" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {[1, 2, 3, 4, 5].map((level) => (
                      <SelectItem key={level} value={level.toString()}>
                        {'⭐'.repeat(level)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="timeframe"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Timeframe</FormLabel>
                <Select
                  value={field.value}
                  onValueChange={field.onChange}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select timeframe" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="short_term">Short Term</SelectItem>
                    <SelectItem value="medium_term">Medium Term</SelectItem>
                    <SelectItem value="long_term">Long Term</SelectItem>
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="rationale"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Rationale</FormLabel>
              <FormControl>
                <Textarea
                  placeholder="Explain your recommendation..."
                  className="h-16 resize-none"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <FormField
            control={form.control}
            name="technicalAnalysis"
            render={({ field }) => (
              <FormItem className="col-span-1 md:col-span-1">
                <FormLabel>Technical Analysis</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Technical analysis..."
                    className="h-16 resize-none"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="fundamentalAnalysis"
            render={({ field }) => (
              <FormItem className="col-span-1 md:col-span-1">
                <FormLabel>Fundamental Analysis</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Fundamental analysis..."
                    className="h-16 resize-none"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="risks"
            render={({ field }) => (
              <FormItem className="col-span-1 md:col-span-1">
                <FormLabel>Risks</FormLabel>
                <FormControl>
                  <Textarea
                    placeholder="Potential risks..."
                    className="h-16 resize-none"
                    {...field}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <div className="flex justify-end gap-4 pt-2">
          <Button type="button" variant="outline" onClick={onSuccess}>
            Cancel
          </Button>
          <Button type="submit" disabled={isLoading || isLoadingProducts || !sessionLoaded}>
            {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Create Recommendation
          </Button>
        </div>
      </form>
    </Form>
  );
} 