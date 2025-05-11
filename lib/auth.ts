"use server"

import { hash, compare } from "bcryptjs"
import { executeQuery } from "./db"
import { cookies } from "next/headers"
import { SignJWT, jwtVerify } from "jose"
import { redirect } from "next/navigation"

const secretKey = new TextEncoder().encode(process.env.JWT_SECRET || "default_secret_key_change_in_production")

export async function hashPassword(password: string): Promise<string> {
  return hash(password, 10)
}

export async function comparePasswords(password: string, hashedPassword: string): Promise<boolean> {
  return compare(password, hashedPassword)
}

export async function createUser(
  username: string,
  password: string,
  email: string,
  fullName: string,
  role: "investor" | "manager" | "analyst" | "admin",
) {
  const hashedPassword = await hashPassword(password)

  const query = `
    INSERT INTO accounts (username, password, email, full_name, role)
    VALUES ($1, $2, $3, $4, $5)
    RETURNING id, username, email, full_name, role
  `

  const result = await executeQuery(query, [username, hashedPassword, email, fullName, role])
  return result[0]
}

export async function authenticateUser(username: string, password: string) {
  try {
    const query = `
      SELECT id, username, password, email, full_name as "fullName", role
      FROM accounts
      WHERE username = $1
    `

    const result = await executeQuery(query, [username])
    console.log("Query result:", JSON.stringify(result, null, 2))

    // Check if we got any results
    if (!result || result.length === 0) {
      console.log("No user found with username:", username)
      return null
    }

    const user = result[0]
    console.log("Found user:", { ...user, password: '[REDACTED]' })

    // Verify the password
    const passwordMatch = await comparePasswords(password, user.password)
    console.log("Password match result:", passwordMatch)

    if (!passwordMatch) {
      console.log("Password doesn't match for user:", username)
      return null
    }

    // Remove password from user object
    const { password: _, ...userWithoutPassword } = user
    console.log("Authentication successful for user:", username)

    return userWithoutPassword
  } catch (error) {
    console.error("Authentication error:", error)
    return null
  }
}

export async function createSession(user: any) {
  // Create a JWT token
  const token = await new SignJWT({
    id: user.id,
    username: user.username,
    role: user.role,
  })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime("24h")
    .sign(secretKey)

  // Set the token in a cookie
  const cookieStore = await cookies()
  cookieStore.set("session", token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    maxAge: 60 * 60 * 24, // 1 day
    path: "/",
  })

  return token
}

export async function getSession() {
  const cookieStore = await cookies()
  const session = cookieStore.get("session")?.value

  if (!session) return null

  try {
    const { payload } = await jwtVerify(session, secretKey)
    return payload
  } catch (error) {
    return null
  }
}

export async function logout() {
  const cookieStore = await cookies()
  cookieStore.delete("session")
}

export async function requireAuth(requiredRoles?: string[]) {
  const session = await getSession()

  if (!session) {
    redirect("/login")
  }

  if (requiredRoles && !requiredRoles.includes(session.role as string)) {
    redirect("/unauthorized")
  }

  return session
}
