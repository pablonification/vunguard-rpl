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
import type { CreateTransactionInput } from "@/lib/db/models/transaction"

type FormData = z.infer<typeof transactionSchema>;

interface Portfolio {
  id: number;
  name: string;
  cashBalance: number;
  assets: Array<{
    id: number;
    productId: number;
    productName: string;
    quantity: number;
  }>;
}

interface Investor {
  id: number;
  full_name: string;
  email: string;
}

export function RecordTransactionForm() {
  const router = useRouter()
  const { toast } = useToast()
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingInvestors, setIsLoadingInvestors] = useState(true)
  const [isLoadingPortfolios, setIsLoadingPortfolios] = useState(false)
  const [investors, setInvestors] = useState<Investor[]>([])
  const [portfolios, setPortfolios] = useState<Portfolio[]>([])
  const [selectedInvestor, setSelectedInvestor] = useState<Investor | null>(null)
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null)
  const [selectedAsset, setSelectedAsset] = useState<Portfolio['assets'][0] | null>(null)

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
    }
  })

  const transactionType = watch("transactionType")
  const price = watch("price")
  const quantity = watch("quantity")

  // Load investors on component mount
  useEffect(() => {
    async function loadInvestors() {
      try {
        setIsLoadingInvestors(true)
        const response = await fetch('/api/investors')
        if (!response.ok) {
          throw new Error('Failed to fetch investors')
        }
        const data = await response.json()
        setInvestors(data)
      } catch (error) {
        console.error('Failed to load investors:', error)
        toast({
          title: "Error",
          description: "Failed to load investors. Please try refreshing the page.",
          variant: "destructive",
        })
      } finally {
        setIsLoadingInvestors(false)
      }
    }
    loadInvestors()
  }, [toast])

  // Load portfolios when investor is selected
  useEffect(() => {
    async function loadPortfolios() {
      if (!selectedInvestor) {
        setPortfolios([])
        return
      }

      try {
        setIsLoadingPortfolios(true)
        const response = await fetch(`/api/portfolios?userId=${selectedInvestor.id}`)
        if (!response.ok) {
          throw new Error('Failed to fetch portfolios')
        }
        const data = await response.json()
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
  }, [selectedInvestor, toast])

  // Calculate total for display only
  const calculateTotal = () => {
    if (price && quantity) {
      return price * quantity
    }
    return 0
  }

  const handleInvestorChange = (investorId: string) => {
    const investor = investors.find(i => i.id === parseInt(investorId))
    setSelectedInvestor(investor || null)
    setSelectedPortfolio(null)
    setSelectedAsset(null)
  }

  const handlePortfolioChange = (portfolioId: string) => {
    const portfolio = portfolios.find(p => p.id === parseInt(portfolioId))
    setSelectedPortfolio(portfolio || null)
    setSelectedAsset(null)
    setValue("portfolioId", parseInt(portfolioId))
  }

  const handleAssetChange = (assetId: string) => {
    const asset = selectedPortfolio?.assets.find(a => a.id === parseInt(assetId))
    setSelectedAsset(asset || null)
    if (asset) {
      setValue("productId", asset.productId)
    }
  }

  async function onSubmit(data: FormData) {
    if (!selectedInvestor) {
      toast({
        title: "Error",
        description: "Please select an investor.",
        variant: "destructive",
      })
      return
    }

    const transactionData: CreateTransactionInput = {
      ...data,
      userId: selectedInvestor.id,
    }

    // Validate price and quantity are positive numbers
    if (transactionData.price <= 0 || transactionData.quantity <= 0) {
      toast({
        title: "Invalid Input",
        description: "Price and quantity must be positive numbers.",
        variant: "destructive",
      })
      return
    }

    try {
      setIsLoading(true)
      await createTransaction(transactionData)
      toast({
        title: "Success",
        description: "Transaction recorded successfully.",
      })
      router.push("/dashboard/transactions")
      router.refresh()
    } catch (error: any) {
      toast({
        title: "Error Recording Transaction",
        description: error?.message || "Failed to record transaction.",
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
            <Label htmlFor="investorId">Investor</Label>
            <Select
              onValueChange={handleInvestorChange}
              disabled={isLoadingInvestors}
            >
              <SelectTrigger>
                <SelectValue
                  placeholder={
                    isLoadingInvestors
                      ? "Loading investors..."
                      : investors.length === 0
                      ? "No investors available"
                      : "Select an investor"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {investors.map((investor) => (
                  <SelectItem key={investor.id} value={investor.id.toString()}>
                    {investor.full_name} ({investor.email})
                  </SelectItem>
                ))}
                {investors.length === 0 && !isLoadingInvestors && (
                  <SelectItem value="none" disabled>
                    No investors available
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="portfolioId">Portfolio</Label>
            <Select
              onValueChange={handlePortfolioChange}
              disabled={!selectedInvestor || isLoadingPortfolios}
            >
              <SelectTrigger>
                <SelectValue
                  placeholder={
                    !selectedInvestor
                      ? "Select an investor first"
                      : isLoadingPortfolios
                    ? "Loading portfolios..." 
                    : portfolios.length === 0 
                      ? "No portfolios available" 
                      : "Select a portfolio"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {portfolios.map((portfolio) => (
                  <SelectItem key={portfolio.id} value={portfolio.id.toString()}>
                    {portfolio.name} (Cash: ${portfolio.cashBalance.toFixed(2)})
                  </SelectItem>
                ))}
                {portfolios.length === 0 && !isLoadingPortfolios && selectedInvestor && (
                  <SelectItem value="none" disabled>
                    No portfolios available
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
            {errors.portfolioId && (
              <p className="text-sm text-destructive">{errors.portfolioId.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="productId">Product</Label>
            <Select 
              onValueChange={handleAssetChange}
              disabled={!selectedPortfolio || isLoadingPortfolios}
            >
              <SelectTrigger>
                <SelectValue
                  placeholder={
                    !selectedPortfolio
                      ? "Select a portfolio first"
                      : isLoadingPortfolios
                      ? "Loading..."
                      : selectedPortfolio.assets.length === 0
                        ? "No products available"
                        : "Select a product"
                  }
                />
              </SelectTrigger>
              <SelectContent>
                {selectedPortfolio?.assets.map((asset) => (
                  <SelectItem key={asset.id} value={asset.id.toString()}>
                    {asset.productName} (Current: {asset.quantity.toLocaleString(undefined, {
                      minimumFractionDigits: 0,
                      maximumFractionDigits: 6,
                    })})
                  </SelectItem>
                ))}
                {selectedPortfolio && selectedPortfolio.assets.length === 0 && (
                  <SelectItem value="none" disabled>
                    No products available in this portfolio
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
            {errors.productId && (
              <p className="text-sm text-destructive">{errors.productId.message}</p>
            )}
          </div>

          <div className="grid gap-2">
            <Label htmlFor="transactionType">Transaction Type</Label>
            <Select 
              defaultValue={transactionType} 
              onValueChange={(value) =>
                setValue("transactionType", value as FormData["transactionType"])
              }
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
              <p className="text-sm text-destructive">
                {errors.transactionType.message}
              </p>
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
            <Button
              type="submit"
              disabled={isLoading || isLoadingInvestors || isLoadingPortfolios}
            >
              {isLoading ? "Recording..." : "Record Transaction"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
} 