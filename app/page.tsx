"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const timer = setTimeout(() => {
      router.push("/login");
    }, 3000); // 3 second delay before redirect

    return () => clearTimeout(timer);
  }, [router]);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-background">
      <div className="relative w-[200px] h-[56px] mb-8">
        <Image
          src="/logo.png"
          alt="Logo"
          fill
          priority
          className="object-contain dark:hidden"
        />
        <Image
          src="/logo_white.png"
          alt="Logo"
          fill
          priority
          className="object-contain hidden dark:block"
        />
      </div>
      
      {/* Loading bar container */}
      <div className="w-48 h-1 bg-muted rounded-full overflow-hidden">
        {/* Animated loading bar */}
        <div 
          className="h-full bg-primary rounded-full transition-all duration-2000 ease-in-out"
          style={{
            animation: "loading 2s ease-in-out forwards"
          }}
        />
      </div>

      <style jsx global>{`
        @keyframes loading {
          0% {
            width: 0%;
          }
          100% {
            width: 100%;
          }
        }
      `}</style>
    </div>
  );
}
