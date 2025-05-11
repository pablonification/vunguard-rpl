"use server"

import { createUser } from "@/lib/auth"

export async function register(
  username: string,
  password: string,
  email: string,
  fullName: string,
  role: "investor" | "manager" | "analyst" | "admin",
) {
  try {
    // Check if username or email already exists
    // This would be implemented with proper database queries

    await createUser(username, password, email, fullName, role)

    return {
      success: true,
    }
  } catch (error: any) {
    console.error("Registration error:", error)

    // Check for unique constraint violations
    if (error.message?.includes("unique constraint")) {
      if (error.message.includes("username")) {
        return {
          success: false,
          error: "Username already exists",
        }
      }
      if (error.message.includes("email")) {
        return {
          success: false,
          error: "Email already exists",
        }
      }
    }

    return {
      success: false,
      error: "An error occurred during registration",
    }
  }
}
