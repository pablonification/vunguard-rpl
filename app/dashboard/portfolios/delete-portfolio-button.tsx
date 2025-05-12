"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Trash2 } from "lucide-react"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/components/ui/use-toast"

interface DeletePortfolioButtonProps {
  portfolioId: number
  portfolioName: string
  investorId?: string
}

export function DeletePortfolioButton({ portfolioId, portfolioName, investorId }: DeletePortfolioButtonProps) {
  const router = useRouter()
  const { toast } = useToast()
  const [isLoading, setIsLoading] = useState(false)
  const [isOpen, setIsOpen] = useState(false)

  async function handleDelete() {
    setIsLoading(true)
    try {
      // Call API to delete portfolio
      const response = await fetch(`/api/portfolios/${portfolioId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.error || 'Failed to delete portfolio')
      }

      toast({
        title: "Success",
        description: `Portfolio '${portfolioName}' deleted successfully.`,
      })
      
      setIsOpen(false) // Close the dialog
      
      // Redirect to portfolios page with success message
      let redirectUrl = '/dashboard/portfolios'
      if (investorId) {
        redirectUrl += `?investorId=${investorId}&success=portfolio-deleted`
      } else {
        redirectUrl += '?success=portfolio-deleted'
      }
      
      router.push(redirectUrl)
      router.refresh() // Refresh the page to reflect the deletion
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to delete portfolio.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
      <AlertDialogTrigger asChild>
        <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive">
          <Trash2 className="h-4 w-4" />
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
          <AlertDialogDescription>
            This action cannot be undone. This will permanently delete the portfolio
            <strong> {portfolioName}</strong> and all associated transactions and assets.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading}>Cancel</AlertDialogCancel>
          <AlertDialogAction onClick={handleDelete} disabled={isLoading} className="bg-destructive hover:bg-destructive/90">
            {isLoading ? "Deleting..." : "Delete"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
} 