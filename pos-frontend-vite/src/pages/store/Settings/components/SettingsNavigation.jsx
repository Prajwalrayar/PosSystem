import React from "react";
import { Store, Bell, Shield, CreditCard } from "lucide-react";

const SettingsNavigation = ({ activeSection }) => {
  const navItems = [
    {
      id: "store-settings",
      label: "Store Settings",
      icon: Store,
      href: "#store-settings"
    },
    {
      id: "notification-settings",
      label: "Notification Settings",
      icon: Bell,
      href: "#notification-settings"
    },
    {
      id: "security-settings",
      label: "Security Settings",
      icon: Shield,
      href: "#security-settings"
    },
    {
      id: "payment-settings",
      label: "Payment Settings",
      icon: CreditCard,
      href: "#payment-settings"
    }
  ];

  return (
    <nav className="space-y-1">
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = activeSection === item.id;
        
        return (
          <a
            key={item.id}
            href={item.href}
            className={`flex items-center px-3 py-2 text-sm font-medium rounded-md ${
              isActive
                ? "bg-emerald-100 text-emerald-700"
                : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
            }`}
          >
            <Icon className={`mr-3 h-5 w-5 ${
              isActive ? "text-emerald-500" : "text-gray-400"
            }`} />
            {item.label}
          </a>
        );
      })}
    </nav>
  );
};

export default SettingsNavigation; 