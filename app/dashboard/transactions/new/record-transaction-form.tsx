"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { createTransaction, transactionSchema } from "@/lib/db/models/transaction"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import type { z } from "zod"

type FormData = z.infer<typeof transactionSchema>

interface Portfolio {
  id: number;
  name: string;
  assets: Array<{
    id: number;
    productId: number;
    productName: string;
    quantity: number;
  }>;
}

export function RecordTransactionForm() {
  const router = useRouter()
  const { toast } = useToast()
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingPortfolios, setIsLoadingPortfolios] = useState(true)
  const [portfolios, setPortfolios] = useState<Portfolio[]>([])
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null)

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(transactionSchema),
    defaultValues: {
      transactionType: "buy",
      transactionDate: new Date().toISOString().split('T')[0],
    }
  })

  const transactionType = watch("transactionType")
  const price = watch("price")
  const quantity = watch("quantity")

  useEffect(() => {
    async function loadPortfolios() {
      try {
        setIsLoadingPortfolios(true)
        const response = await fetch('/api/portfolios')
        if (!response.ok) {
          throw new Error('Failed to fetch portfolios')
        }
        const data = await response.json()
        console.log('Loaded portfolios:', data)
        setPortfolios(data)
      } catch (error) {
        console.error('Failed to load portfolios:', error)
        toast({
          title: "Error",
          description: "Failed to load portfolios. Please try refreshing the page.",
          variant: "destructive",
        })
      } finally {
        setIsLoadingPortfolios(false)
      }
    }
    loadPortfolios()
  }, [toast])

  // Calculate total for display only
  const calculateTotal = () => {
    if (price && quantity) {
      return price * quantity
    }
    return 0
  }

  const handlePortfolioChange = (portfolioId: string) => {
    const portfolio = portfolios.find(p => p.id === parseInt(portfolioId))
    setSelectedPortfolio(portfolio || null)
    setValue("portfolioId", parseInt(portfolioId))
    // Reset asset selection when portfolio changes
    setValue("assetId", undefined)
  }

  async function onSubmit(data: FormData) {
    try {
      setIsLoading(true)
      await createTransaction(data)
      toast({
        title: "Success",
        description: "Transaction recorded successfully.",
      })
      router.push("/dashboard/transactions")
      router.refresh()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to record transaction.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Transaction Details</CardTitle>
        <CardDescription>Enter the details of the transaction.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid gap-2">
            <Label htmlFor="portfolioId">Portfolio</Label>
            <Select onValueChange={handlePortfolioChange} disabled={isLoadingPortfolios}>
              <SelectTrigger>
                <SelectValue placeholder={
                  isLoadingPortfolios 
                    ? "Loading portfolios..." 
                    : portfolios.length === 0 
                      ? "No portfolios available" 
                      : "Select a portfolio"
                } />
              </SelectTrigger>
              <SelectContent>
                {portfolios.map((portfolio) => (
                  <SelectItem key={portfolio.id} value={portfolio.id.toString()}>
                    {portfolio.name}
                  </SelectItem>
                ))}
                {portfolios.length === 0 && !isLoadingPortfolios && (
                  <SelectItem value="none" disabled>No portfolios available</SelectItem>
                )}
              </SelectContent>
            </Select>
            {errors.portfolioId && (
              <p className="text-sm text-destructive">{errors.portfolioId.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="assetId">Product</Label>
            <Select 
              onValueChange={(value) => setValue("assetId", parseInt(value))}
              disabled={!selectedPortfolio || isLoadingPortfolios}
            >
              <SelectTrigger>
                <SelectValue placeholder={
                  isLoadingPortfolios 
                    ? "Loading..." 
                    : !selectedPortfolio 
                      ? "Select a portfolio first"
                      : selectedPortfolio.assets.length === 0
                        ? "No products available"
                        : "Select a product"
                } />
              </SelectTrigger>
              <SelectContent>
                {selectedPortfolio?.assets.map((asset) => (
                  <SelectItem key={asset.id} value={asset.id.toString()}>
                    {asset.productName} (Current: {asset.quantity.toLocaleString(undefined, {
                      minimumFractionDigits: 0,
                      maximumFractionDigits: 6
                    })})
                  </SelectItem>
                ))}
                {selectedPortfolio && selectedPortfolio.assets.length === 0 && (
                  <SelectItem value="none" disabled>No products available in this portfolio</SelectItem>
                )}
              </SelectContent>
            </Select>
            {errors.assetId && (
              <p className="text-sm text-destructive">{errors.assetId.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="transactionType">Transaction Type</Label>
            <Select 
              defaultValue={transactionType} 
              onValueChange={(value) => setValue("transactionType", value as FormData["transactionType"])}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="buy">Buy</SelectItem>
                <SelectItem value="sell">Sell</SelectItem>
              </SelectContent>
            </Select>
            {errors.transactionType && (
              <p className="text-sm text-destructive">{errors.transactionType.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="quantity">Quantity</Label>
            <Input
              id="quantity"
              type="number"
              step="0.000001"
              min="0"
              {...register("quantity", { valueAsNumber: true })}
              className={errors.quantity ? "border-destructive" : ""}
            />
            {errors.quantity && (
              <p className="text-sm text-destructive">{errors.quantity.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="price">Price per Unit</Label>
            <Input
              id="price"
              type="number"
              step="0.000001"
              min="0"
              {...register("price", { valueAsNumber: true })}
              className={errors.price ? "border-destructive" : ""}
            />
            {errors.price && (
              <p className="text-sm text-destructive">{errors.price.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label>Total Amount (Calculated)</Label>
            <Input
              type="number"
              step="0.01"
              value={calculateTotal().toFixed(2)}
              readOnly
              disabled
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="transactionDate">Transaction Date</Label>
            <Input
              id="transactionDate"
              type="datetime-local"
              {...register("transactionDate")}
              className={errors.transactionDate ? "border-destructive" : ""}
            />
            {errors.transactionDate && (
              <p className="text-sm text-destructive">{errors.transactionDate.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Textarea
              id="notes"
              {...register("notes")}
              className={errors.notes ? "border-destructive" : ""}
              placeholder="Add any additional notes about this transaction..."
            />
            {errors.notes && (
              <p className="text-sm text-destructive">{errors.notes.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push("/dashboard/transactions")}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading || isLoadingPortfolios}>
              {isLoading ? "Recording..." : "Record Transaction"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
} 