import { LoginForm } from "./login-form"
import Link from "next/link"

export default function LoginPage() {
  return (
    <div className="min-h-screen flex flex-col justify-center items-center p-4 bg-muted/40">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold">Login</h1>
          <p className="text-muted-foreground mt-2">Sign in to your account to continue</p>
        </div>
        <div className="bg-card rounded-lg shadow-lg p-6">
          <LoginForm />
          <div className="mt-4 text-center text-sm">
            <p>
              Don't have an account?{" "}
              <Link href="/register" className="text-primary hover:underline">
                Register
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
