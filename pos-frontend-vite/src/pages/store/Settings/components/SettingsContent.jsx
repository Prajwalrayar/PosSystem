import React from "react";
import StoreSettings from "./StoreSettings";
import NotificationSettings from "./NotificationSettings";
import SecuritySettings from "./SecuritySettings";
import PaymentSettings from "./PaymentSettings";
import { Save } from "lucide-react";
import { Button } from "../../../../components/ui/button";

const SettingsContent = ({
  storeSettings,
  notificationSettings,
  securitySettings,
  paymentSettings,
  onStoreSettingsChange,
  onNotificationSettingsChange,
  onSecuritySettingsChange,
  onPaymentSettingsChange,
  onSave,
  isSaving,
  fieldErrors,
}) => {
  const firstError = Object.values(fieldErrors || {})[0];

  return (
    <div className="space-y-6">
      <StoreSettings
        settings={storeSettings}
        onChange={onStoreSettingsChange}
      />

      <NotificationSettings
        settings={notificationSettings}
        onChange={onNotificationSettingsChange}
      />

      <SecuritySettings
        settings={securitySettings}
        onChange={onSecuritySettingsChange}
      />

      <PaymentSettings
        settings={paymentSettings}
        onChange={onPaymentSettingsChange}
      />

      {firstError && (
        <p className="text-sm text-red-600">{firstError}</p>
      )}

      <Button className="" variant={"outline"} onClick={onSave} disabled={isSaving}>
        <Save className="mr-2 h-4 w-4" /> {isSaving ? "Saving..." : "Save All Settings"}
      </Button>
    </div>
  );
};

export default SettingsContent;
