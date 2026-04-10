import React, { useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Store } from "lucide-react";
import StoreSettingsForm from "./StoreSettingsForm";

const StoreSettings = ({ settings, onChange }) => {
  const handleValuesChange = useCallback((values) => {
    Object.entries(values).forEach(([key, value]) => {
      onChange(key, value);
    });
  }, [onChange]);

  return (
    <Card id="store-settings">
      <CardHeader>
        <CardTitle className="flex items-center">
          <Store className="mr-2 h-5 w-5 text-emerald-500" />
          Store Settings
        </CardTitle>
        <CardDescription>
          Configure your store's basic information
        </CardDescription>
      </CardHeader>
      <CardContent>
        <StoreSettingsForm
          initialValues={settings}
          onValuesChange={handleValuesChange}
        />
      </CardContent>
    </Card>
  );
};

export default StoreSettings; 