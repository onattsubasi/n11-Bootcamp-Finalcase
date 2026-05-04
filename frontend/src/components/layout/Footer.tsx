import React from "react";
import { Link } from "react-router-dom";
import { ShoppingBag, Shield, Globe, Zap, Lock } from "lucide-react";

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-950 text-white border-t border-white/5 pt-24 pb-12 overflow-hidden relative">
      {/* Background Ambience */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-px bg-gradient-to-r from-transparent via-primary/50 to-transparent" />
      <div className="absolute bottom-0 right-0 w-[500px] h-[500px] bg-primary/5 rounded-full blur-[120px] -mr-64 -mb-64" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-16 mb-24">
          {/* Brand Engine */}
          <div className="space-y-8">
            <Link
              to="/"
              className="flex items-center gap-4 group transition-transform active:scale-95 w-fit"
            >
              <div className="bg-primary p-3.5 rounded-2xl shadow-2xl shadow-primary/40 group-hover:rotate-12 transition-all duration-500">
                <ShoppingBag className="w-6 h-6 text-primary-foreground" />
              </div>
              <div className="flex flex-col">
                <span className="text-xl sm:text-2xl font-black tracking-tighter uppercase leading-none text-white">
                  FINALCASE
                </span>
                <span className="text-[8px] font-black tracking-[0.4em] text-primary uppercase opacity-60">
                  E-Commerce Platform
                </span>
              </div>
            </Link>
            <p className="text-white/40 text-[8px] font-medium leading-relaxed max-w-xs uppercase tracking-widest">
              Architecting the future of decentralized commerce through neural
              SKUs and priority logistics protocols.
            </p>
          </div>

          {/* Catalog Segments */}
          <div className="space-y-8">
            <h4 className="text-[10px] font-black uppercase tracking-[0.4em] text-primary">
              Catalog Segments
            </h4>
            <ul className="space-y-4">
              <FooterLink label="Neural Hardware" />
              <FooterLink label="Biometric Gear" />
              <FooterLink label="Priority Assets" />
              <FooterLink label="Legacy Systems" />
            </ul>
          </div>

          {/* Operational Links */}
          <div className="space-y-8">
            <h4 className="text-[10px] font-black uppercase tracking-[0.4em] text-primary">
              Operational
            </h4>
            <ul className="space-y-4">
              <FooterLink label="Logistics Tracking" />
              <FooterLink label="Identity Protocol" />
              <FooterLink label="Node Distribution" />
              <FooterLink label="Merchant Core" />
            </ul>
          </div>

          {/* Compliance & Trust */}
          <div className="space-y-8">
            <h4 className="text-[10px] font-black uppercase tracking-[0.4em] text-primary">
              Compliance
            </h4>
            <div className="space-y-6">
              <div className="flex items-start gap-4">
                <div className="w-8 h-8 bg-emerald-500/10 rounded-lg flex items-center justify-center text-emerald-500 shrink-0">
                  <Lock size={14} />
                </div>
                <div className="space-y-1">
                  <p className="text-[10px] font-black uppercase tracking-widest leading-none">
                    Military Grade
                  </p>
                  <p className="text-[8px] font-medium text-white/20 leading-tight">
                    256-bit SHA Protocol Encryption
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-4">
                <div className="w-8 h-8 bg-blue-500/10 rounded-lg flex items-center justify-center text-blue-500 shrink-0">
                  <Shield size={14} />
                </div>
                <div className="space-y-1">
                  <p className="text-[10px] font-black uppercase tracking-widest leading-none">
                    Verified Identity
                  </p>
                  <p className="text-[8px] font-medium text-white/20 leading-tight">
                    Biometric Hash Integration
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="pt-12 border-t border-white/5 flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2 text-[9px] font-black uppercase tracking-widest text-white/20">
              <Globe size={12} />
              <span>Global Protocol: ACTIVE</span>
            </div>
            <div className="flex items-center gap-2 text-[9px] font-black uppercase tracking-widest text-white/20">
              <Zap size={12} className="text-primary" />
              <span>Latency: 14MS</span>
            </div>
          </div>

          <p className="text-[9px] font-black uppercase tracking-[0.4em] text-white/10">
            © 2026 FINALCASE CORP. ALL PROTOCOLS RESERVED.
          </p>

          <div className="flex items-center gap-6">
            <span className="text-[9px] font-black uppercase tracking-widest text-white/20 hover:text-primary transition-colors cursor-pointer">
              Security Policy
            </span>
            <span className="text-[9px] font-black uppercase tracking-widest text-white/20 hover:text-primary transition-colors cursor-pointer">
              SLA Agreement
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
};

const FooterLink: React.FC<{ label: string }> = ({ label }) => (
  <li>
    <a
      href="#"
      className="text-xs font-bold text-white/40 hover:text-primary hover:translate-x-2 transition-all inline-block uppercase tracking-widest"
    >
      {label}
    </a>
  </li>
);
