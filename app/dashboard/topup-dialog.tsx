'use client'

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/components/ui/use-toast"
import { Coins } from "lucide-react"
import { getInvestorPortfolios, performTopUp } from "./actions" // Actions we need to create

interface Portfolio {
  id: number
  name: string
}

interface TopUpDialogProps {
  accountId: number
}

export function TopUpDialog({ accountId }: TopUpDialogProps) {
  const router = useRouter()
  const { toast } = useToast()
  const [isOpen, setIsOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [portfolios, setPortfolios] = useState<Portfolio[]>([])
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string>("")
  const [amount, setAmount] = useState<string>("")

  // Fetch portfolios when the dialog opens
  useEffect(() => {
    if (isOpen) {
      setIsLoading(true)
      getInvestorPortfolios(accountId)
        .then((data) => {
          setPortfolios(data)
          // Pre-select the first portfolio if available
          if (data.length > 0) {
            setSelectedPortfolioId(data[0].id.toString())
          }
        })
        .catch((error) => {
          toast({ title: "Error", description: "Failed to load portfolios.", variant: "destructive" })
        })
        .finally(() => setIsLoading(false))
    }
  }, [isOpen, accountId, toast])

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)

    const portfolioIdNum = parseInt(selectedPortfolioId, 10)
    const amountNum = parseFloat(amount)

    if (isNaN(portfolioIdNum) || isNaN(amountNum) || amountNum <= 0) {
      toast({
        title: "Invalid Input",
        description: "Please select a portfolio and enter a valid positive amount.",
        variant: "destructive",
      })
      setIsLoading(false)
      return
    }

    try {
      await performTopUp(portfolioIdNum, amountNum)
      toast({
        title: "Success",
        description: "Funds added successfully.",
      })
      setIsOpen(false) // Close dialog
      setAmount("") // Reset amount
      router.refresh() // Refresh dashboard data
    } catch (error: any) {
      toast({
        title: "Top Up Failed",
        description: error.message || "An error occurred during the top up.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">
          <Coins className="mr-2 h-4 w-4" /> Top Up Funds
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Top Up Funds</DialogTitle>
          <DialogDescription>
            Add funds to your selected investment portfolio.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="portfolio" className="text-right">
                Portfolio
              </Label>
              <Select
                value={selectedPortfolioId}
                onValueChange={setSelectedPortfolioId}
                required
                disabled={isLoading || portfolios.length === 0}
              >
                <SelectTrigger id="portfolio" className="col-span-3">
                  <SelectValue placeholder={portfolios.length === 0 ? "No portfolios available" : "Select a portfolio"} />
                </SelectTrigger>
                <SelectContent>
                  {portfolios.map((portfolio) => (
                    <SelectItem key={portfolio.id} value={portfolio.id.toString()}>
                      {portfolio.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="amount" className="text-right">
                Amount
              </Label>
              <Input
                id="amount"
                type="number"
                step="0.01" // Allow cents
                min="0.01" // Minimum amount
                placeholder="e.g., 1000.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="col-span-3"
                required
                disabled={isLoading}
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="submit" disabled={isLoading || !selectedPortfolioId || !amount || parseFloat(amount) <= 0}>
              {isLoading ? "Processing..." : "Confirm Top Up"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
} 