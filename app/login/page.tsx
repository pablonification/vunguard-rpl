import { LoginForm } from "./login-form"
import Link from "next/link"
import Image from "next/image"

export default function LoginPage() {
  return (
    <div className="min-h-screen flex flex-col justify-center items-center p-4 bg-muted/40">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="flex justify-center mb-6">
            <div className="relative w-[180px] h-[50px]">
              <Image
                src="/logo.png"
                alt="Company Logo"
                fill
                priority
                className="object-contain dark:hidden"
              />
              <Image
                src="/logo_white.png"
                alt="Company Logo"
                fill
                priority
                className="object-contain hidden dark:block"
              />
            </div>
          </div>
          {/* <h1 className="text-2xl font-semibold tracking-tight">Welcome back</h1> */}
          <p className="text-sm text-muted-foreground mt-2">Enter your credentials to access your account</p>
        </div>
        <div className="bg-card rounded-xl border shadow-sm p-6">
          <LoginForm />
          <div className="mt-4 text-center text-sm">
            <p className="text-muted-foreground">
              Don't have an account?{" "}
              <Link href="/register" className="text-primary hover:underline font-medium">
                Register
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
