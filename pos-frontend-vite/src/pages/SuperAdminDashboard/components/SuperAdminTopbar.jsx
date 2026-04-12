import React, { useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router";
import { Bell, User, Search, ArrowRight } from "lucide-react";
import { Button } from "../../../components/ui/button";
import { Input } from "../../../components/ui/input";

export default function SuperAdminTopbar() {
  const { userProfile } = useSelector((state) => state.user);
  const { stores } = useSelector((state) => state.store);
  const { plans } = useSelector((state) => state.subscriptionPlan);
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);

  const searchResults = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (normalizedQuery.length < 2) return [];

    const pageTargets = [
      { type: "page", title: "Dashboard", description: "Open dashboard overview", path: "/super-admin/dashboard" },
      { type: "page", title: "Stores", description: "Open all stores", path: "/super-admin/stores" },
      { type: "page", title: "Pending Requests", description: "Review store approvals", path: "/super-admin/requests" },
      { type: "page", title: "Subscription Plans", description: "Manage plans", path: "/super-admin/subscriptions" },
      { type: "page", title: "Settings", description: "Manage profile and system settings", path: "/super-admin/settings" },
    ].filter((item) =>
      `${item.title} ${item.description}`.toLowerCase().includes(normalizedQuery)
    );

    const storeMatches = (stores || [])
      .filter((store) => {
        const searchableText = [
          store.brand,
          store.id,
          store.storeAdmin?.fullName,
          store.storeAdmin?.email,
          store.contact?.phone,
          store.contact?.email,
        ]
          .filter(Boolean)
          .join(" ")
          .toLowerCase();
        return searchableText.includes(normalizedQuery);
      })
      .map((store) => ({
        type: store.status === "pending" ? "request" : "store",
        title: store.brand || "Unnamed Store",
        description: `${store.storeAdmin?.fullName || "Owner unavailable"}${store.status ? ` • ${store.status}` : ""}`,
        path: store.status === "pending" ? `/super-admin/requests?q=${encodeURIComponent(query)}` : `/super-admin/stores?q=${encodeURIComponent(query)}`,
      }));

    const planMatches = (plans || [])
      .filter((plan) => {
        const searchableText = [plan.name, plan.description, plan.billingCycle]
          .filter(Boolean)
          .join(" ")
          .toLowerCase();
        return searchableText.includes(normalizedQuery);
      })
      .map((plan) => ({
        type: "plan",
        title: plan.name || "Unnamed Plan",
        description: `${plan.billingCycle || "Plan"} • ₹${plan.price ?? "-"}`,
        path: `/super-admin/subscriptions?q=${encodeURIComponent(query)}`,
      }));

    return [...pageTargets, ...storeMatches, ...planMatches].slice(0, 8);
  }, [plans, query, stores]);

  const handleResultClick = (path) => {
    navigate(path);
    setQuery("");
    setIsOpen(false);
  };

  return (
    <header className="bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b border-border px-6 py-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <h1 className="text-2xl font-bold text-foreground">Super Admin Panel</h1>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="relative hidden md:block">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground w-4 h-4" />
            <Input
              value={query}
              onChange={(event) => {
                setQuery(event.target.value);
                setIsOpen(true);
              }}
              onFocus={() => setIsOpen(true)}
              onBlur={() => {
                window.setTimeout(() => setIsOpen(false), 150);
              }}
              onKeyDown={(event) => {
                if (event.key === "Enter" && searchResults[0]) {
                  handleResultClick(searchResults[0].path);
                }
              }}
              placeholder="Search stores, plans, pages..."
              className="pl-10 w-72"
            />

            {isOpen && query.trim().length >= 2 && (
              <div className="absolute z-50 mt-2 w-full rounded-xl border bg-background shadow-xl overflow-hidden" onMouseDown={(event) => event.preventDefault()}>
                {searchResults.length > 0 ? (
                  <div className="max-h-80 overflow-y-auto">
                    {searchResults.map((result, index) => (
                      <button
                        key={`${result.type}-${result.title}-${index}`}
                        type="button"
                        className="w-full text-left px-4 py-3 hover:bg-muted/70 border-b last:border-b-0 flex items-start gap-3"
                        onMouseDown={(event) => {
                          event.preventDefault();
                          handleResultClick(result.path);
                        }}
                      >
                        <Search className="w-4 h-4 mt-0.5 text-muted-foreground" />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <p className="text-sm font-medium truncate">{result.title}</p>
                            <span className="text-[10px] uppercase tracking-wide px-2 py-0.5 rounded-full bg-muted text-muted-foreground">
                              {result.type}
                            </span>
                          </div>
                          <p className="text-xs text-muted-foreground truncate">{result.description}</p>
                        </div>
                        <ArrowRight className="w-4 h-4 text-muted-foreground" />
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="px-4 py-3 text-sm text-muted-foreground">No results found.</div>
                )}
              </div>
            )}
          </div>

          {/* Notifications */}
          <Button variant="ghost" size="icon" className="relative">
            <Bell className="w-5 h-5" />
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              3
            </span>
          </Button>
          
          {/* User Profile */}
          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-medium text-foreground">
                {userProfile?.fullName || "Super Admin"}
              </p>
              <p className="text-xs text-muted-foreground">
                {userProfile?.email || "superadmin@pos.com"}
              </p>
            </div>
            <Button variant="ghost" size="icon" className="rounded-full">
              <User className="w-5 h-5" />
            </Button>
          </div>
        </div>
      </div>
    </header>
  );
} 