"use client";

import type { ReactNode } from "react";
import { useState } from "react";
import Link from "next/link"; // Link might be used here for internal nav if any
import Image from "next/image";
import { redirect } from "next/navigation"; // For client-side redirects if needed, though session checks are better in parent server component
import { useRouter } from "next/navigation"; // Import useRouter for navigation
import { useTheme } from "next-themes";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { DashboardNav } from "./dashboard-nav";
import { cn } from "@/lib/utils";
import { 
  Menu, 
  Search, 
  Bell, 
  Mail, 
  PlusCircle
} from "lucide-react";
import { logout } from "@/lib/auth"; // Import logout
import { ThemeToggle } from "@/components/theme-toggle";

// This is the DashboardLayoutClient component moved from dashboard-layout.tsx
export function DashboardLayoutClient({ 
  children, 
  requiredRoles, 
  session 
}: { 
  children: ReactNode, 
  requiredRoles?: string[], 
  session: { username: string, role: string, avatarUrl?: string } // Added more specific type for session
}) {
  const [isSidebarExpanded, setIsSidebarExpanded] = useState(false);
  const router = useRouter(); // Initialize useRouter
  const { theme } = useTheme();

  // Client-side session checks might be redundant if parent server component handles it
  // but good for robustness if this component could be rendered standalone in future.
  if (!session) {
    // In a real client component, you might redirect or show a login prompt.
    // However, the parent server component should ideally handle the redirect.
    // For now, logging or returning null might be safer than a client-side redirect here
    // if the parent is already handling it. Let's assume parent handles.
    // console.error("Session not found in DashboardLayoutClient");
    // return null; // Or a loading state/redirect
  }

  // Role check can also be primarily handled by parent, but added here for completeness if needed
  // if (requiredRoles && session && !requiredRoles.includes(session.role as string)) {
  //   console.error("Unauthorized access in DashboardLayoutClient");
  //   return <p>Unauthorized</p>; // Or redirect
  // }

  const handleLogout = async () => {
    await logout();
    redirect("/login");
  };

  const handleProfileNavigation = () => {
    router.push("/dashboard/profile"); // Navigate to profile/settings page
  };

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside 
        className={cn(
          "fixed inset-y-0 left-0 z-50 flex flex-col items-center border-r border-border bg-card/80 backdrop-blur-sm py-5 transition-all duration-300 ease-in-out dark:bg-card/40",
          isSidebarExpanded ? "w-60" : "w-20"
        )}
      >
        <Link
          href="/dashboard"
          className={cn(
            "mb-8 relative flex items-center justify-center overflow-hidden", // Added overflow-hidden
            "transition-all duration-300 ease-in-out",
            isSidebarExpanded
              ? "w-[140px] h-[40px]" // Adjusted size for expanded state
              : "w-[40px] h-[40px]" // Fixed size for collapsed state
          )}
          aria-label="Dashboard home"
        >
          {/* Collapsed Logo */}
          <div 
            className={cn(
              "absolute inset-0 flex items-center justify-center",
              "transition-transform duration-300 ease-in-out",
              isSidebarExpanded ? "-translate-x-full opacity-0" : "translate-x-0 opacity-100"
            )}
          >
            <Image
              src={theme === 'dark' ? "/logo_short_white.png" : "/logo_short.png"}
              alt="Logo Icon"
              width={32}
              height={32}
              className="object-contain"
            />
          </div>

          {/* Expanded Logo */}
          <div 
            className={cn(
              "absolute inset-0 flex items-center justify-center",
              "transition-transform duration-300 ease-in-out",
              isSidebarExpanded ? "translate-x-0 opacity-100" : "translate-x-full opacity-0"
            )}
          >
            <Image
              src={theme === 'dark' ? "/logo_white.png" : "/logo.png"}
              alt="Company Logo"
              width={140}
              height={40}
              className="object-contain"
            />
          </div>
        </Link>
        <DashboardNav 
          role={session.role as string} 
          isExpanded={isSidebarExpanded} 
          setIsExpanded={setIsSidebarExpanded} 
        />
      </aside>

      {/* Main content area */}
      <div 
        className={cn(
          "flex flex-1 flex-col transition-all duration-300 ease-in-out", 
          isSidebarExpanded ? "ml-60" : "ml-20"
        )}
      >
        {/* Header */}
        <header className="sticky top-0 z-40 w-full border-b border-border bg-background/80 shadow-sm backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container mx-auto flex h-16 max-w-full items-center justify-between px-4 sm:px-6 lg:px-8">
            {/* Left section: Title or Breadcrumbs (Tabs removed) */}
            <div>
              {/* Placeholder for a page title or breadcrumbs if needed */}
            </div>

            {/* Right section: Search, Actions, User */}
            <div className="flex flex-1 items-center justify-end space-x-3 sm:space-x-4">
              <div className="relative hidden md:block">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                  <Search className="h-5 w-5 text-muted-foreground" aria-hidden="true" />
                </div>
                <Input
                  type="search"
                  name="search"
                  id="search"
                  className="pl-10 pr-3"
                  placeholder="Search"
                />
              </div>

              <Button className="whitespace-nowrap" variant="gradient">
                <PlusCircle className="mr-2 h-5 w-5 flex-shrink-0" />
                Add Invoices
              </Button>

              <button
                type="button"
                className="rounded-full bg-background/50 p-1 text-muted-foreground hover:text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                aria-label="View messages"
              >
                <Mail className="h-6 w-6" aria-hidden="true" />
              </button>

              <button
                type="button"
                className="rounded-full bg-background/50 p-1 text-muted-foreground hover:text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
                aria-label="View notifications"
              >
                <Bell className="h-6 w-6" aria-hidden="true" />
              </button>

              <ThemeToggle />
              
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Avatar className="h-9 w-9 cursor-pointer">
                    <AvatarImage src={session.avatarUrl || "https://github.com/shadcn.png"} alt="User avatar" />
                    <AvatarFallback>{session.username?.charAt(0).toUpperCase() || "U"}</AvatarFallback>
                  </Avatar>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56"> {/* Added width for better layout */}
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">{session.username}</p>
                      <p className="text-xs leading-none text-muted-foreground">{session.role}</p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleProfileNavigation} className="cursor-pointer">
                    Profile
                  </DropdownMenuItem>
                  <DropdownMenuItem className="cursor-pointer">Settings</DropdownMenuItem> {/* Placeholder for now */}
                  <DropdownMenuItem className="cursor-pointer">Support</DropdownMenuItem> {/* Placeholder for now */}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-destructive focus:bg-destructive/10 focus:text-destructive">
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>

              {/* Mobile Menu Trigger */}
              <Sheet>
                <SheetTrigger asChild className="md:hidden">
                  <Button variant="outline" size="icon" className="ml-2 flex-shrink-0">
                    <Menu className="h-5 w-5" />
                    <span className="sr-only">Toggle Menu</span>
                  </Button>
                </SheetTrigger>
                <SheetContent side="left" className="w-64 pr-0 pt-10 sm:w-72">
                  {/* For mobile, sidebar is always expanded in the sheet */}
                  <DashboardNav role={session.role as string} isMobile={true} isExpanded={true} />
                </SheetContent>
              </Sheet>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
} 