import type { ReactNode } from "react"
import { getSession } from "@/lib/auth"
import { redirect } from "next/navigation"
import { DashboardLayoutClient } from "./dashboard-layout-client"

interface DashboardLayoutProps {
  children: ReactNode
  requiredRoles?: string[]
}

// This is now a pure Server Component
export async function DashboardLayout({ children, requiredRoles }: DashboardLayoutProps) {
  const session = await getSession()

  // Perform session and role checks here on the server
  if (!session) {
    console.log("No session found, redirecting to login")
    redirect("/login")
  }

  if (requiredRoles && !requiredRoles.includes(session.role as string)) {
    console.log(`User with role '${session.role}' attempted to access a page restricted to roles: ${requiredRoles.join(", ")}`)
    redirect("/unauthorized")
  }

  // Direct admins to accounts page if they try to access the main dashboard
  if (session.role === 'admin' && !requiredRoles?.includes('admin') && !requiredRoles?.length) {
    redirect("/dashboard/accounts")
  }

  // Pass the fetched session to the Client Component
  return (
    <DashboardLayoutClient 
      session={{
        id: session.id as number,
        username: session.username as string,
        role: session.role as string,
        avatarUrl: session.avatarUrl as string | undefined
      }} 
      requiredRoles={requiredRoles}
    >
      {children}
    </DashboardLayoutClient>
  )
}
