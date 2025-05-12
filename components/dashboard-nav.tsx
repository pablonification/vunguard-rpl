"use client"

import React from "react"
import Link from "next/link"
import { usePathname, redirect } from "next/navigation"
import { cn } from "@/lib/utils"
import { 
  LayoutGrid, Briefcase, FolderKanban, BarChart2, TrendingUp, UserCircle2,
  Headphones, LogOut as IconLogOut, PanelLeftClose, PanelRightClose
} from "lucide-react"
import { logout } from "@/lib/auth"
import { Button } from "@/components/ui/button"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"

interface NavItem {
  title: string
  href: string
  icon: React.ReactNode
  roles: string[]
  isExternal?: boolean
  action?: () => void
}

const navItems: NavItem[] = [
  {
    title: "Dashboard",
    href: "/dashboard",
    icon: <LayoutGrid className="h-6 w-6" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Products",
    href: "/dashboard/products",
    icon: <Briefcase className="h-6 w-6" />,
    roles: ["manager", "analyst", "admin"],
  },
  {
    title: "Portfolios",
    href: "/dashboard/portfolios",
    icon: <FolderKanban className="h-6 w-6" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Transactions",
    href: "/dashboard/transactions",
    icon: <BarChart2 className="h-6 w-6" />,
    roles: ["investor", "manager", "admin"],
  },
  {
    title: "Performance",
    href: "/dashboard/performance",
    icon: <TrendingUp className="h-6 w-6" />,
    roles: ["investor", "manager", "analyst", "admin"],
  },
  {
    title: "Accounts",
    href: "/dashboard/accounts",
    icon: <UserCircle2 className="h-6 w-6" />,
    roles: ["admin"],
  },
]

const bottomNavItems: NavItem[] = [
  {
    title: "Support",
    href: "#",
    icon: <Headphones className="h-6 w-6" />,
    roles: ["investor", "manager", "analyst", "admin"],
    isExternal: true,
  },
]

export function DashboardNav({ 
  role, 
  isMobile = false, 
  isExpanded = false, 
  setIsExpanded 
}: { 
  role: string, 
  isMobile?: boolean, 
  isExpanded?: boolean, 
  setIsExpanded?: (expanded: boolean) => void 
}) {
  const pathname = usePathname()

  const filteredNavItems = navItems.filter((item) => item.roles.includes(role))
  const filteredBottomNavItems = bottomNavItems.filter((item) => item.roles.includes(role))

  const handleLogout = async () => {
    await logout()
    redirect("/login")
  }

  // Helper to render nav/button content (icon and optional text)
  const renderItemContent = (icon: React.ReactNode, title: string) => (
    <>
      {icon}
      {(isExpanded || isMobile) && <span className={cn("ml-3 whitespace-nowrap")}>{title}</span>}
    </>
  );

  // Helper to wrap with Tooltip if needed
  const renderTooltipWrapper = (content: React.ReactNode, title: string, key: string) => {
    if (isExpanded || isMobile) {
      return React.cloneElement(content as React.ReactElement, { key });
    }
    return (
      <Tooltip key={key}>
        <TooltipTrigger asChild>{content}</TooltipTrigger>
        <TooltipContent side="right" className="ml-2">{title}</TooltipContent>
      </Tooltip>
    );
  };

  return (
    <TooltipProvider delayDuration={0}>
      <div className="flex h-full flex-col justify-between">
        <nav className={cn("grid items-start gap-2 py-4", isExpanded || isMobile ? "px-3" : "px-2")}>
          {filteredNavItems.map((item, index) => renderTooltipWrapper(
            <Link
              href={item.href}
              className={cn(
                "group flex items-center rounded-lg p-3 text-sm font-medium hover:bg-accent hover:text-accent-foreground",
                pathname === item.href ? "bg-accent text-accent-foreground" : "text-muted-foreground hover:text-accent-foreground",
                isExpanded || isMobile ? "justify-start" : "justify-center"
              )}
            >
              {renderItemContent(item.icon, item.title)}
            </Link>,
            item.title,
            `nav-${index}`
          ))}
        </nav>
        <div className={cn("border-t", isExpanded || isMobile ? "px-3 pt-2" : "px-2 pt-2")}>
          {setIsExpanded && renderTooltipWrapper(
            <Button 
              variant="ghost" 
              className={cn(
                "mb-1 flex w-full items-center p-3 text-muted-foreground hover:bg-accent hover:text-accent-foreground",
                isExpanded || isMobile ? "justify-start" : "justify-center"
              )}
              onClick={() => setIsExpanded(!isExpanded)}
              aria-label={isExpanded ? "Collapse sidebar" : "Expand sidebar"}
            >
              {renderItemContent(isExpanded ? <PanelLeftClose className="h-6 w-6" /> : <PanelRightClose className="h-6 w-6" />, "Collapse")}
            </Button>,
            isExpanded ? "Collapse" : "Expand",
            "toggle-expand"
          )}
          <nav className="grid items-start gap-1">
            {filteredBottomNavItems.map((item, index) => renderTooltipWrapper(
              <Link
                href={item.href}
                target={item.isExternal ? "_blank" : undefined}
                rel={item.isExternal ? "noopener noreferrer" : undefined}
                className={cn(
                  "group flex items-center rounded-lg p-3 text-sm font-medium hover:bg-accent hover:text-accent-foreground",
                  "text-muted-foreground hover:text-accent-foreground",
                  isExpanded || isMobile ? "justify-start" : "justify-center"
                )}
              >
                {renderItemContent(item.icon, item.title)}
              </Link>,
              item.title,
              `bottom-nav-${index}`
            ))}
            {renderTooltipWrapper(
              <Button
                variant="ghost"
                className={cn(
                  "group flex h-auto w-full items-center p-3 text-sm font-medium text-muted-foreground hover:bg-accent hover:text-destructive",
                  isExpanded || isMobile ? "justify-start" : "justify-center"
                )}
                onClick={handleLogout}
              >
                {renderItemContent(<IconLogOut className="h-6 w-6" />, "Log out")}
              </Button>,
              "Log out",
              "logout-button"
            )}
          </nav>
        </div>
      </div>
    </TooltipProvider>
  )
}
