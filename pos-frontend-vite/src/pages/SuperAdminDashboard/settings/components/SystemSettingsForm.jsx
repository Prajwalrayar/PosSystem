import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { Settings } from "lucide-react";

const SystemSettingItem = ({ id, title, description, checked, onChange, disabled }) => (
  <>
    <div className="flex items-center justify-between">
      <div>
        <h4 className="font-medium">{title}</h4>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      <Switch
        id={id}
        checked={Boolean(checked)}
        onCheckedChange={(value) => onChange(id, value)}
        disabled={disabled}
      />
    </div>
    <Separator />
  </>
);

const SystemSettingsForm = ({ systemSettings, onChange, onSave, isSaving, isLoading }) => {
  const systemSettingItems = [
    {
      id: "autoApproveStores",
      title: "Auto-approve Stores",
      description: "Automatically approve new store registrations",
    },
    {
      id: "requireDocumentVerification",
      title: "Require Document Verification",
      description: "Mandatory document verification for store approval",
    },
    {
      id: "commissionAutoCalculation",
      title: "Auto Commission Calculation",
      description: "Automatically calculate commissions",
    },
    {
      id: "maintenanceMode",
      title: "Maintenance Mode",
      description: "Enable maintenance mode for system updates",
    },
  ];
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="w-5 h-5" />
          System Settings
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {systemSettingItems.map((item, index) => (
          <React.Fragment key={item.id}>
            <SystemSettingItem
              id={item.id}
              title={item.title}
              description={item.description}
              checked={systemSettings[item.id]}
              onChange={onChange}
              disabled={isLoading || isSaving}
            />
            {index === systemSettingItems.length - 2 && <Separator />}
          </React.Fragment>
        ))}
        <div className="flex justify-end">
          <Button onClick={onSave} disabled={isLoading || isSaving}>
            {isSaving ? "Saving..." : "Save System Settings"}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default SystemSettingsForm; 