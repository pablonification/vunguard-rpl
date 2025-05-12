import { Button } from "@/components/ui/button"
import Link from "next/link"
import { getSession } from "@/lib/auth"
import Image from "next/image"

export default async function UnauthorizedPage() {
  const session = await getSession()
  
  // Determine the appropriate redirect based on user role
  let homeLink = "/"
  let homeText = "Back to Home"
  
  if (session) {
    if (session.role === 'admin') {
      homeLink = "/dashboard/accounts"
      homeText = "Go to Accounts"
    } else if (['investor', 'manager', 'analyst'].includes(session.role as string)) {
      homeLink = "/dashboard"
      homeText = "Go to Dashboard"
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-4 bg-background">
      <div className="text-center max-w-md">
        <div className="mb-6 flex justify-center">
          <div className="relative w-24 h-24 text-red-500">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="h-24 w-24">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
        </div>
        <h1 className="text-4xl font-bold mb-4">Access Denied</h1>
        <p className="text-xl text-muted-foreground mb-2">You don't have permission to access this page.</p>
        <p className="text-sm text-muted-foreground mb-8">
          This area is restricted based on your current role {session?.role && `(${session.role})`}.
          Please contact an administrator if you believe this is an error.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link href={homeLink}>
            <Button>{homeText}</Button>
          </Link>
          <Link href="/login">
            <Button variant="outline">Log Out</Button>
          </Link>
        </div>
      </div>
    </div>
  )
}
