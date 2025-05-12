"use client"

import type React from "react"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useToast } from "@/components/ui/use-toast"
import { register } from "./actions"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Eye, EyeOff } from "lucide-react"
import { cn } from "@/lib/utils"

interface ValidationError {
  username?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
}

export function RegisterForm() {
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [errors, setErrors] = useState<ValidationError>({})
  const router = useRouter()
  const { toast } = useToast()

  // Password requirements (internal validation only)
  const requirements = [
    { re: /.{8,}/, label: "At least 8 characters" },
    { re: /[0-9]/, label: "Includes number" },
    { re: /[a-z]/, label: "Includes lowercase letter" },
    { re: /[A-Z]/, label: "Includes uppercase letter" },
    { re: /[$&+,:;=?@#|'<>.^*()%!-]/, label: "Includes special symbol" },
  ]

  const getStrength = (pass: string) => {
    let multiplier = 0;
    requirements.forEach((requirement) => {
      if (requirement.re.test(pass)) {
        multiplier += 1;
      }
    });
    return Math.max((multiplier * 100) / requirements.length, 10);
  }

  const validatePassword = (pass: string) => {
    const errors: string[] = [];
    requirements.forEach((requirement) => {
      if (!requirement.re.test(pass)) {
        errors.push(requirement.label);
      }
    });
    return errors;
  }

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newPassword = event.target.value;
    setPassword(newPassword);
    
    const validationErrors = validatePassword(newPassword);
    if (validationErrors.length > 0) {
      setErrors(prev => ({ ...prev, password: undefined }));
    } else {
      setErrors(prev => ({ ...prev, password: undefined }));
    }

    // Check confirm password match
    if (confirmPassword && newPassword !== confirmPassword) {
      setErrors(prev => ({ ...prev, confirmPassword: "The passwords you entered don't match" }));
    } else {
      setErrors(prev => ({ ...prev, confirmPassword: undefined }));
    }
  }

  const handleConfirmPasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newConfirmPassword = event.target.value;
    setConfirmPassword(newConfirmPassword);
    
    if (password !== newConfirmPassword) {
      setErrors(prev => ({ ...prev, confirmPassword: "The passwords you entered don't match" }));
    } else {
      setErrors(prev => ({ ...prev, confirmPassword: undefined }));
    }
  }

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setErrors({})

    const formData = new FormData(event.currentTarget)
    const username = formData.get("username") as string
    const email = formData.get("email") as string
    const fullName = formData.get("fullName") as string

    // Only validate password match since other requirements are enforced by disabled button
    if (password !== confirmPassword) {
      setErrors(prev => ({ ...prev, confirmPassword: "The passwords you entered don't match" }));
      setIsLoading(false);
      return;
    }

    try {
      const result = await register(username, password, email, fullName, "investor")

      if (result.success) {
        toast({
          title: "Account created successfully",
          description: "You can now sign in with your credentials",
        })
        router.push("/login")
      } else {
        // Handle specific error cases
        const errorMessage = result.error || "Unable to create account";
        if (errorMessage.includes("Username already exists")) {
          setErrors(prev => ({ ...prev, username: "This username is already taken" }));
        } else if (errorMessage.includes("Email already exists")) {
          setErrors(prev => ({ ...prev, email: "An account with this email already exists" }));
        }
        
        toast({
          variant: "destructive",
          title: "Unable to create account",
          description: errorMessage,
        })
      }
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Something went wrong",
        description: "We couldn't create your account. Please try again.",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const strength = getStrength(password);
  const getStrengthText = () => {
    if (strength <= 20) return "Very Weak";
    if (strength <= 40) return "Weak";
    if (strength <= 60) return "Fair";
    if (strength <= 80) return "Good";
    return "Strong";
  }

  const strengthClass = cn(
    "h-1 transition-all duration-300",
    strength <= 20 ? "bg-red-500" : 
    strength <= 40 ? "bg-orange-500" :
    strength <= 60 ? "bg-yellow-500" :
    strength <= 80 ? "bg-lime-500" :
    "bg-green-500"
  );

  const allRequirementsMet = password ? requirements.every(req => req.re.test(password)) : false;
  
  // Check only password-related errors
  const hasPasswordErrors = Boolean(errors.password || errors.confirmPassword);

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="fullName">Full Name</Label>
        <Input 
          id="fullName" 
          name="fullName" 
          placeholder="Enter your full name" 
          required 
          className="bg-background"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="email">Email</Label>
        <Input 
          id="email" 
          name="email" 
          type="email" 
          placeholder="Enter your email" 
          required 
          className={cn("bg-background", errors.email && "border-red-500")}
        />
        {errors.email && (
          <p className="text-sm text-red-500 mt-1">{errors.email}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="username">Username</Label>
        <Input 
          id="username" 
          name="username" 
          placeholder="Choose a username" 
          required 
          className={cn("bg-background", errors.username && "border-red-500")}
        />
        {errors.username && (
          <p className="text-sm text-red-500 mt-1">{errors.username}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="password">Password</Label>
        <div className="relative">
          <Input 
            id="password" 
            name="password" 
            type={showPassword ? "text" : "password"}
            placeholder="Choose a password" 
            value={password}
            onChange={handlePasswordChange}
            required 
            className={cn("bg-background pr-10", errors.password && "border-red-500")}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
          >
            {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {password && (
          <div className="mt-2 space-y-2">
            <div className="h-1 w-full bg-gray-200 rounded-full overflow-hidden">
              <div
                className={strengthClass}
                style={{ width: `${strength}%` }}
              />
            </div>
            <p className={cn(
              "text-xs transition-colors",
              strength <= 40 ? "text-red-500" :
              strength <= 60 ? "text-yellow-500" :
              "text-green-500"
            )}>
              Password strength: {getStrengthText()}
            </p>
            <div className="space-y-1">
              {requirements.map((requirement, index) => (
                <p 
                  key={index}
                  className={cn(
                    "text-xs flex items-center gap-1",
                    requirement.re.test(password) 
                      ? "text-green-500" 
                      : "text-muted-foreground"
                  )}
                >
                  {requirement.re.test(password) ? "✓" : "○"} {requirement.label}
                </p>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="confirmPassword">Confirm Password</Label>
        <div className="relative">
          <Input 
            id="confirmPassword" 
            name="confirmPassword" 
            type={showConfirmPassword ? "text" : "password"}
            placeholder="Confirm your password" 
            value={confirmPassword}
            onChange={handleConfirmPasswordChange}
            required 
            className={cn("bg-background pr-10", errors.confirmPassword && "border-red-500")}
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
          >
            {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-sm text-red-500 mt-1">{errors.confirmPassword}</p>
        )}
      </div>

      <Button 
        type="submit" 
        className="w-full" 
        disabled={isLoading || hasPasswordErrors || !allRequirementsMet}
      >
        {isLoading ? (
          <>
            <span className="loading loading-spinner loading-sm mr-2"></span>
            Creating account...
          </>
        ) : (
          "Create account"
        )}
      </Button>
    </form>
  )
}
