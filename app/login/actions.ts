"use server"

import { authenticateUser, createSession } from "@/lib/auth"

export async function login(username: string, password: string) {
  try {
    const user = await authenticateUser(username, password)

    if (!user) {
      return {
        success: false,
        error: "Invalid username or password",
      }
    }

    await createSession(user)

    return {
      success: true,
      role: user.role,
    }
  } catch (error) {
    console.error("Login error:", error)
    return {
      success: false,
      error: "An error occurred during login",
    }
  }
}
