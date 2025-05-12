"use client"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { useToast } from "@/components/ui/use-toast"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { AlertCircle } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { createPortfolio } from "./actions"

interface CreatePortfolioFormProps {
  investorId: number | null;
}

export function CreatePortfolioForm({ investorId }: CreatePortfolioFormProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()
  const { toast } = useToast()
  const searchParams = useSearchParams()

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setError(null)

    const formData = new FormData(event.currentTarget)
    const name = formData.get("name") as string
    const description = formData.get("description") as string

    try {
      const result = await createPortfolio(name, description, investorId)

      if (result.success) {
        toast({
          title: "Portfolio created",
          description: "The new portfolio has been created successfully.",
        })
        // If we're creating for another investor, keep their ID in the URL
        if (investorId) {
          router.push(`/dashboard/portfolios?investorId=${investorId}`)
        } else {
          router.push("/dashboard/portfolios")
        }
        router.refresh()
      } else {
        setError(result.error || "Failed to create portfolio")
        toast({
          variant: "destructive",
          title: "Error",
          description: result.error || "Failed to create portfolio",
        })
      }
    } catch (error) {
      const errorMessage = "An unexpected error occurred. Please try again."
      setError(errorMessage)
      toast({
        variant: "destructive",
        title: "Error",
        description: errorMessage,
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={onSubmit} className="space-y-6">
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="space-y-2">
        <Label htmlFor="name">Portfolio Name</Label>
        <Input
          id="name"
          name="name"
          placeholder="Enter portfolio name"
          required
          disabled={isLoading}
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description (Optional)</Label>
        <Textarea
          id="description"
          name="description"
          placeholder="Enter portfolio description"
          disabled={isLoading}
          className="min-h-[100px]"
        />
      </div>

      <div className="flex justify-end gap-4">
        <Button
          type="button"
          variant="outline"
          disabled={isLoading}
          onClick={() => router.back()}
        >
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading}>
          {isLoading ? "Creating..." : "Create Portfolio"}
        </Button>
      </div>
    </form>
  )
} 