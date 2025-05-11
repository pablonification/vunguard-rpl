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
    redirect("/login")
    // Important: When redirecting in a Server Component, you usually don't render anything else.
    // However, Next.js handles this. For clarity, often people return null or an empty fragment,
    // but redirect itself should suffice to stop further rendering of this path.
  }

  if (requiredRoles && !requiredRoles.includes(session.role as string)) {
    redirect("/unauthorized")
  }

  // Pass the fetched session to the Client Component
  return (
    <DashboardLayoutClient session={session} requiredRoles={requiredRoles}>
      {children}
    </DashboardLayoutClient>
  )
}
