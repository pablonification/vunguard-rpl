import Link from "next/link"
import { Button } from "@/components/ui/button"
import { getSession } from "@/lib/auth"

export default async function Home() {
  const session = await getSession()

  return (
    <div className="flex min-h-screen flex-col">
      <header className="bg-primary text-primary-foreground py-6">
        <div className="container mx-auto px-4">
          <h1 className="text-3xl font-bold">Investment Asset Management System</h1>
        </div>
      </header>

      <main className="flex-1 container mx-auto px-4 py-12">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-4xl font-bold mb-6">Manage Your Investment Assets Efficiently</h2>
          <p className="text-xl mb-8">
            A comprehensive system designed to help Investment Management Companies manage various investment products
            efficiently, accurately, and transparently.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            {session ? (
              <Link href="/dashboard">
                <Button size="lg" className="w-full sm:w-auto">
                  Go to Dashboard
                </Button>
              </Link>
            ) : (
              <>
                <Link href="/login">
                  <Button size="lg" className="w-full sm:w-auto">
                    Login
                  </Button>
                </Link>
                <Link href="/register">
                  <Button size="lg" variant="outline" className="w-full sm:w-auto">
                    Register
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>

        <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-card text-card-foreground rounded-lg p-6 shadow">
            <h3 className="text-xl font-semibold mb-3">Investment Product Management</h3>
            <p>
              Add, edit, and manage investment products with detailed information about strategies and asset
              allocations.
            </p>
          </div>
          <div className="bg-card text-card-foreground rounded-lg p-6 shadow">
            <h3 className="text-xl font-semibold mb-3">Performance Tracking</h3>
            <p>
              Track asset values, calculate returns, and compare performance against benchmarks with powerful analytics
              tools.
            </p>
          </div>
          <div className="bg-card text-card-foreground rounded-lg p-6 shadow">
            <h3 className="text-xl font-semibold mb-3">Comprehensive Reporting</h3>
            <p>
              Generate detailed reports on portfolio performance for internal use, investors, and regulatory compliance.
            </p>
          </div>
        </div>
      </main>

      <footer className="bg-muted py-6">
        <div className="container mx-auto px-4 text-center">
          <p>© {new Date().getFullYear()} Investment Asset Management System. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}
