import type { ReactNode } from "react"
import { redirect } from "next/navigation"
import { getSession, logout } from "@/lib/auth"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { LogOut, Menu } from "lucide-react"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { DashboardNav } from "./dashboard-nav"

interface DashboardLayoutProps {
  children: ReactNode
  requiredRoles?: string[]
}

export async function DashboardLayout({ children, requiredRoles }: DashboardLayoutProps) {
  const session = await getSession()

  if (!session) {
    redirect("/login")
  }

  if (requiredRoles && !requiredRoles.includes(session.role as string)) {
    redirect("/unauthorized")
  }

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-14 items-center">
          <div className="mr-4 hidden md:flex">
            <Link href="/dashboard" className="mr-6 flex items-center space-x-2">
              <span className="font-bold">Investment Management System</span>
            </Link>
            <nav className="flex items-center space-x-6 text-sm font-medium">
              <Link href="/dashboard" className="transition-colors hover:text-foreground/80">
                Dashboard
              </Link>
              <Link href="/dashboard/products" className="transition-colors hover:text-foreground/80">
                Products
              </Link>
              <Link href="/dashboard/portfolios" className="transition-colors hover:text-foreground/80">
                Portfolios
              </Link>
              <Link href="/dashboard/transactions" className="transition-colors hover:text-foreground/80">
                Transactions
              </Link>
              <Link href="/dashboard/performance" className="transition-colors hover:text-foreground/80">
                Performance
              </Link>
              {session.role === "admin" && (
                <Link href="/dashboard/accounts" className="transition-colors hover:text-foreground/80">
                  Accounts
                </Link>
              )}
            </nav>
          </div>
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" size="icon" className="mr-2 md:hidden">
                <Menu className="h-5 w-5" />
                <span className="sr-only">Toggle Menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="pr-0">
              <MobileNav role={session.role as string} />
            </SheetContent>
          </Sheet>
          <div className="flex flex-1 items-center justify-between space-x-2 md:justify-end">
            <div className="w-full flex-1 md:w-auto md:flex-none">
              <div className="hidden md:block">
                <p className="text-sm">
                  Logged in as <span className="font-medium">{session.username}</span> ({session.role})
                </p>
              </div>
            </div>
            <form
              action={async () => {
                "use server"
                await logout()
                redirect("/login")
              }}
            >
              <Button variant="ghost" size="icon">
                <LogOut className="h-5 w-5" />
                <span className="sr-only">Log out</span>
              </Button>
            </form>
          </div>
        </div>
      </header>
      <div className="container flex-1 items-start md:grid md:grid-cols-[220px_1fr] md:gap-6 lg:grid-cols-[240px_1fr] lg:gap-10">
        <aside className="fixed top-14 z-30 -ml-2 hidden h-[calc(100vh-3.5rem)] w-full shrink-0 md:sticky md:block">
          <div className="h-full py-6 pr-6 lg:py-8">
            <DashboardNav role={session.role as string} />
          </div>
        </aside>
        <main className="flex w-full flex-col overflow-hidden py-6">{children}</main>
      </div>
    </div>
  )
}

function MobileNav({ role }: { role: string }) {
  return <DashboardNav role={role} />
}
