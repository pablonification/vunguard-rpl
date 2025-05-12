"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
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
import { AlertCircle } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useToast } from "@/components/ui/use-toast"
import { createProduct } from "@/lib/db/models/product"

export function NewProductForm() {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()
  const { toast } = useToast()

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setError(null)

    const formData = new FormData(event.currentTarget)
    const data = {
      code: formData.get("code") as string,
      name: formData.get("name") as string,
      description: formData.get("description") as string,
      investment_strategy: formData.get("investment_strategy") as string,
      risk_level: formData.get("risk_level") as string,
    }

    try {
      const product = await createProduct(data)
      toast({
        title: "Product created",
        description: "The product has been created successfully.",
      })
      router.push(`/dashboard/products/${product.id}`)
      router.refresh()
    } catch (error) {
      setError("Failed to create product. Please check your input and try again.")
      toast({
        variant: "destructive",
        title: "Error",
        description: "Failed to create product. Please try again.",
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
        <Label htmlFor="code">Product Code *</Label>
        <Input
          id="code"
          name="code"
          required
          disabled={isLoading}
          placeholder="e.g. TGF001"
        />
        <p className="text-xs text-muted-foreground">
          A unique identifier for this product
        </p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="name">Product Name *</Label>
        <Input
          id="name"
          name="name"
          required
          disabled={isLoading}
          placeholder="e.g. Tech Growth Fund"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Description *</Label>
        <Textarea
          id="description"
          name="description"
          required
          disabled={isLoading}
          className="min-h-[100px]"
          placeholder="Brief description of the investment product"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="investment_strategy">Investment Strategy *</Label>
        <Textarea
          id="investment_strategy"
          name="investment_strategy"
          required
          disabled={isLoading}
          className="min-h-[100px]"
          placeholder="Details about the investment approach and strategy"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="risk_level">Risk Level *</Label>
        <Select name="risk_level" required disabled={isLoading}>
          <SelectTrigger>
            <SelectValue placeholder="Select risk level" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="Low">Low</SelectItem>
            <SelectItem value="Medium">Medium</SelectItem>
            <SelectItem value="High">High</SelectItem>
          </SelectContent>
        </Select>
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
          {isLoading ? "Creating..." : "Create Product"}
        </Button>
      </div>
    </form>
  )
} 