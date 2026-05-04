import React from "react";
import { useNavigate } from "react-router-dom";
import { Spinner } from "../components/ui/Spinner";
import { Button } from "../components/ui/Button";
import toast from "react-hot-toast";

import {
  ShoppingCart,
  Trash2,
  Minus,
  Plus,
  ArrowRight,
  Package,
} from "lucide-react";
import {
  useBasketQuery,
  useUpdateBasketItem,
  useRemoveFromBasket,
  useClearBasket,
} from "@/features/basket/hooks/useBasket";
import { errorMessage } from "@/api/problem";
import { formatTRY } from "@/lib/utils/format";

const BasketPage: React.FC = () => {
  const navigate = useNavigate();
  const { data: basket, isPending } = useBasketQuery(true);
  const { mutate: updateQty } = useUpdateBasketItem();
  const { mutate: removeItem } = useRemoveFromBasket();
  const { mutate: clearBasket } = useClearBasket();

  const handleQtyChange = (itemId: string, newQty: number) => {
    if (newQty < 1) return;
    updateQty(
      { itemId, quantity: newQty },
      {
        onError: (err: any) =>
          toast.error(errorMessage(err) || "Failed to update quantity"),
      },
    );
  };

  const handleRemove = (itemId: string) => {
    removeItem(itemId, {
      onSuccess: () => toast.success("Removed from bag"),
      onError: (err: any) =>
        toast.error(errorMessage(err) || "Failed to remove item"),
    });
  };

  const handleClear = () => {
    if (window.confirm("Clear all items from your bag?")) {
      clearBasket(undefined, {
        onSuccess: () => toast.success("Bag cleared"),
        onError: (err: any) => toast.error(errorMessage(err)),
      });
    }
  };

  if (isPending) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[60vh] gap-4">
        <Spinner size="lg" />
        <span className="text-xs font-black uppercase tracking-[0.3em] text-muted-foreground animate-pulse">
          Retrieving your bag...
        </span>
      </div>
    );
  }

  const items = basket?.items ?? [];
  const itemCount = items.reduce(
    (acc: number, item: any) => acc + item.quantity,
    0,
  );

  if (items.length === 0) {
    return (
      <div className="max-w-xl mx-auto text-center py-24 px-6 bg-card border border-border rounded-[3rem] shadow-xl mt-12">
        <div className="relative inline-block mb-10">
          <div className="bg-primary/10 p-10 rounded-full">
            <ShoppingCart className="h-20 w-20 text-primary opacity-20" />
          </div>
          <div className="absolute -top-2 -right-2 bg-destructive text-white text-[10px] font-black rounded-full w-10 h-10 flex items-center justify-center border-4 border-card shadow-lg">
            0
          </div>
        </div>
        <h2 className="text-3xl font-black text-foreground tracking-tight uppercase">
          Your bag is empty
        </h2>
        <p className="text-muted-foreground font-medium mt-4 mb-10 max-w-sm mx-auto leading-relaxed">
          Discover the perfect piece for your collection. Explore our premium
          selection and find what moves you.
        </p>
        <div className="flex flex-col gap-3 justify-center max-w-xs mx-auto">
          <Button
            onClick={() => navigate("/")}
            variant="primary"
            size="lg"
            className="rounded-2xl h-14 font-black uppercase tracking-widest"
          >
            Browse Catalog
          </Button>
          <Button
            onClick={() => navigate("/orders")}
            variant="ghost"
            className="font-black uppercase tracking-widest text-xs"
          >
            View My Orders
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-12 space-y-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 px-2">
        <div className="space-y-2">
          <h1 className="text-6xl font-black text-foreground tracking-tighter uppercase leading-none">
            My Bag
          </h1>
          <p className="text-muted-foreground font-bold uppercase tracking-widest text-[10px]">
            Reserved Items:{" "}
            <span className="text-foreground">{itemCount} units</span>
          </p>
        </div>
        <button
          onClick={handleClear}
          className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-destructive hover:opacity-80 transition-opacity"
        >
          <Trash2 className="h-3.5 w-3.5" /> Empty Entire Bag
        </button>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
        {/* Items List */}
        <div className="lg:col-span-8 space-y-6">
          {items.map((item: any, index: number) => (
            <div
              key={item.id ?? item.basketItemId ?? item.productId ?? index}
              className="group relative bg-card p-6 rounded-[2rem] border border-border/50 flex flex-col sm:flex-row items-center gap-8 hover:border-primary/30 transition-all duration-500 hover:shadow-2xl hover:shadow-primary/5"
            >
              <div className="w-32 h-32 bg-muted rounded-2xl flex-shrink-0 overflow-hidden border border-border group-hover:scale-105 transition-transform duration-500">
                {item.image ? (
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center opacity-20">
                    <Package className="w-10 h-10" />
                  </div>
                )}
              </div>

              <div className="flex-grow text-center sm:text-left space-y-2">
                <div className="space-y-1">
                  <span className="text-[9px] font-black text-primary uppercase tracking-widest">
                    Premium Selection
                  </span>
                  <h3 className="text-xl font-bold text-foreground group-hover:text-primary transition-colors leading-tight">
                    {item.name}
                  </h3>
                </div>

                <div className="flex flex-wrap justify-center sm:justify-start items-center gap-6 pt-2">
                  {/* Quantity Controller */}
                  <div className="flex items-center rounded-xl border border-border bg-muted/30 p-1">
                    <button
                      onClick={() =>
                        handleQtyChange(item.id, item.quantity - 1)
                      }
                      className="flex h-8 w-8 items-center justify-center rounded-lg transition-all hover:bg-card hover:text-primary active:scale-90"
                      disabled={item.quantity <= 1}
                    >
                      <Minus className="h-4 w-4" />
                    </button>
                    <span className="w-10 text-center text-xs font-black tabular-nums">
                      {item.quantity}
                    </span>
                    <button
                      onClick={() =>
                        handleQtyChange(item.id, item.quantity + 1)
                      }
                      className="flex h-8 w-8 items-center justify-center rounded-lg transition-all hover:bg-card hover:text-primary active:scale-90"
                    >
                      <Plus className="h-4 w-4" />
                    </button>
                  </div>

                  <div className="h-4 w-px bg-border hidden sm:block" />

                  <button
                    onClick={() => handleRemove(item.id)}
                    className="text-destructive/60 hover:text-destructive text-[10px] font-black uppercase tracking-widest transition-colors"
                  >
                    Remove
                  </button>
                </div>
              </div>

              <div className="text-center sm:text-right min-w-[140px]">
                <p className="text-2xl font-black text-foreground tabular-nums">
                  {formatTRY(item.price * item.quantity)}
                </p>
                <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest mt-1">
                  {formatTRY(item.price)}{" "}
                  <span className="lowercase">/ unit</span>
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Summary Card */}
        <div className="lg:col-span-4 lg:sticky lg:top-24">
          <div className="bg-gray-950 text-white p-10 rounded-[2.5rem] shadow-2xl relative overflow-hidden border border-white/5">
            {/* Background Gradient */}
            <div className="absolute top-0 right-0 -mr-20 -mt-20 w-80 h-80 bg-primary/10 rounded-full blur-[100px] pointer-events-none" />

            <h3 className="text-2xl font-black mb-8 uppercase tracking-tighter text-white/90">
              Order Summary
            </h3>

            <div className="space-y-6 mb-10">
              <div className="flex justify-between items-center text-white/40">
                <span className="uppercase tracking-[0.2em] text-[10px] font-black">
                  Subtotal
                </span>
                <span className="font-bold tabular-nums text-white/80">
                  {formatTRY(basket?.totalPrice || 0)}
                </span>
              </div>
              <div className="flex justify-between items-center text-white/40">
                <span className="uppercase tracking-[0.2em] text-[10px] font-black">
                  Logistics
                </span>
                <span className="text-emerald-400 font-black text-[10px] uppercase tracking-widest bg-emerald-400/10 px-3 py-1 rounded-full border border-emerald-400/20">
                  Free Shipping
                </span>
              </div>
              <div className="h-px bg-white/5 my-2" />
              <div className="flex justify-between items-end">
                <div className="space-y-1">
                  <span className="text-xs font-black uppercase tracking-widest text-white/30">
                    Total Value
                  </span>
                  <p className="text-4xl font-black text-primary tracking-tighter tabular-nums leading-none">
                    {formatTRY(basket?.totalPrice || 0)}
                  </p>
                </div>
              </div>
            </div>

            <Button
              onClick={() => navigate("/checkout")}
              className="w-full py-8 text-lg font-black uppercase rounded-2xl bg-primary hover:bg-primary/90 border-none shadow-2xl shadow-primary/30 h-16 transition-all active:scale-[0.98]"
            >
              Secure Checkout <ArrowRight className="ml-2 h-5 w-5" />
            </Button>

            <div className="mt-8 flex items-center justify-center gap-3 opacity-30 group grayscale hover:grayscale-0 transition-all duration-500">
              <div className="h-4 w-12 bg-white/20 rounded" />
              <div className="h-4 w-12 bg-white/20 rounded" />
              <div className="h-4 w-12 bg-white/20 rounded" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BasketPage;
