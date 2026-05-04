import React from "react";
import { Sparkles, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface HeroBannerProps {
  heading: string;
  subheading?: string;
  count?: number;
}

export const HeroBanner: React.FC<HeroBannerProps> = ({
  heading,
  subheading = "Discover the best products at unbeatable prices.",
  count,
}) => {
  return (
    <div className="relative overflow-hidden rounded-[2.5rem] bg-gradient-to-br from-blue-900 via-indigo-900 to-purple-950 p-10 lg:p-16 text-white shadow-2xl shadow-blue-900/20">
      {/* Decorative Elements */}
      <div className="absolute top-0 right-0 -mt-20 -mr-20 w-96 h-96 bg-blue-500/10 rounded-full blur-[100px]" />
      <div className="absolute bottom-0 left-0 -mb-20 -ml-20 w-80 h-80 bg-purple-500/10 rounded-full blur-[80px]" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full h-full bg-[radial-gradient(circle_at_center,_var(--tw-gradient-from)_0%,_transparent_70%)] from-white/5 pointer-events-none" />

      <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-12">
        <div className="max-w-2xl space-y-6">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-white/10 backdrop-blur-md rounded-full border border-white/10">
            <Sparkles className="h-4 w-4 text-amber-400" />
            <span className="text-xs font-black uppercase tracking-[0.2em] text-blue-100">
              Premium Marketplace
            </span>
          </div>

          <h1 className="text-5xl lg:text-7xl font-black tracking-tight leading-tight">
            {heading}
          </h1>

          <p className="text-xl text-blue-100/70 font-medium leading-relaxed max-w-xl">
            {subheading}
          </p>

          <div className="flex flex-wrap items-center gap-6 pt-4">
            <Button
              variant="primary"
              size="lg"
              className="rounded-2xl px-8 h-14 font-black uppercase tracking-widest bg-blue-600 text-white hover:bg-blue-700"
            >
              Start Shopping <ArrowRight className="ml-2 h-5 w-5" />
            </Button>

            {count !== undefined && (
              <div className="flex flex-col">
                <span className="text-3xl font-black text-white leading-none">
                  {count}
                </span>
                <span className="text-[10px] font-bold text-blue-100/50 uppercase tracking-[0.2em]">
                  Products Available
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Floating Product Badge / Social Proof */}
        <div className="hidden lg:block">
          <div className="bg-white/5 backdrop-blur-2xl border border-white/10 p-8 rounded-[2.5rem] shadow-2xl space-y-6 rotate-2 hover:rotate-0 transition-transform duration-500">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-emerald-500/20 rounded-full flex items-center justify-center">
                <div className="w-4 h-4 bg-emerald-500 rounded-full animate-pulse" />
              </div>
              <div>
                <div className="text-xs font-black text-white uppercase tracking-widest">
                  Live Inventory
                </div>
                <div className="text-[10px] text-emerald-400 font-bold uppercase tracking-widest">
                  Global Sync Active
                </div>
              </div>
            </div>
            <div className="h-px bg-white/10 w-full" />
            <div className="flex -space-x-3">
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className="w-10 h-10 rounded-full border-2 border-indigo-900 bg-gray-800 flex items-center justify-center overflow-hidden"
                >
                  <img src={`https://i.pravatar.cc/100?u=${i}`} alt="user" />
                </div>
              ))}
              <div className="w-10 h-10 rounded-full border-2 border-indigo-900 bg-white/10 flex items-center justify-center text-[10px] font-black text-white">
                +12k
              </div>
            </div>
            <p className="text-[10px] font-bold text-blue-100/40 uppercase tracking-widest">
              Trusted by shoppers worldwide
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
