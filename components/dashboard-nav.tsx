"use client"

import type React from "react"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { BarChart3, Briefcase, CreditCard, Home, Package, Users } from "lucide-react"

interface NavItem {
  title: string
  href: string
  icon: React.ReactNode
  roles: string[]
}

const navItems: NavItem[] = [
  {
    title: "Dashboard",
    href: "/dashboard",
    icon: <Home className="mr-2 h-4 w-4" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Products",
    href: "/dashboard/products",
    icon: <Package className="mr-2 h-4 w-4" />,
    roles: ["manager", "analyst", "admin"],
  },
  {
    title: "Portfolios",
    href: "/dashboard/portfolios",
    icon: <Briefcase className="mr-2 h-4 w-4" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Transactions",
    href: "/dashboard/transactions",
    icon: <CreditCard className="mr-2 h-4 w-4" />,
    roles: ["investor", "manager", "admin"],
  },
  {
    title: "Performance",
    href: "/dashboard/performance",
    icon: <BarChart3 className="mr-2 h-4 w-4" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Accounts",
    href: "/dashboard/accounts",
    icon: <Users className="mr-2 h-4 w-4" />,
    roles: ["admin"],
  },
]

export function DashboardNav({ role }: { role: string }) {
  const pathname = usePathname()

  const filteredNavItems = navItems.filter((item) => item.roles.includes(role))

  return (
    <nav className="grid items-start gap-2">
      {filteredNavItems.map((item, index) => (
        <Link
          key={index}
          href={item.href}
          className={cn(
            "group flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-accent hover:text-accent-foreground",
            pathname === item.href ? "bg-accent" : "transparent",
          )}
        >
          {item.icon}
          <span>{item.title}</span>
        </Link>
      ))}
    </nav>
  )
}
