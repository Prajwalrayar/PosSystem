import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { Bell } from "lucide-react";

const NotificationItem = ({ id, title, description, checked, onChange, disabled }) => (
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

const NotificationSettingsForm = ({ notifications, onChange, onSave, isSaving, isLoading }) => {
  const notificationItems = [
    {
      id: "newStoreRequests",
      title: "New Store Requests",
      description: "Get notified when new stores register",
    },
    {
      id: "storeApprovals",
      title: "Store Approvals",
      description: "Notifications for store approval actions",
    },
    {
      id: "commissionUpdates",
      title: "Commission Updates",
      description: "Alerts for commission rate changes",
    },
    {
      id: "systemAlerts",
      title: "System Alerts",
      description: "Important system notifications",
    },
    {
      id: "emailNotifications",
      title: "Email Notifications",
      description: "Receive notifications via email",
    },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Bell className="w-5 h-5" />
          Notification Preferences
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {notificationItems.map((item, index) => (
          <React.Fragment key={item.id}>
            <NotificationItem
              id={item.id}
              title={item.title}
              description={item.description}
              checked={notifications[item.id]}
              onChange={onChange}
              disabled={isLoading || isSaving}
            />
            {index === notificationItems.length - 2 && <Separator />}
          </React.Fragment>
        ))}
        <div className="flex justify-end">
          <Button onClick={onSave} disabled={isLoading || isSaving}>
            {isSaving ? "Saving..." : "Save Notifications"}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default NotificationSettingsForm; 