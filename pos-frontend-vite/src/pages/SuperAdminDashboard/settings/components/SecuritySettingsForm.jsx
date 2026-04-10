import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Key, Eye, EyeOff } from "lucide-react";

const PasswordInput = ({ id, label, value, onChange, show, onToggle, disabled }) => (
  <div className="space-y-2">
    <Label htmlFor={id}>{label}</Label>
    <div className="relative">
      <Input id={id} type={show ? "text" : "password"} value={value} onChange={onChange} disabled={disabled} />
      <Button
        type="button"
        variant="ghost"
        size="sm"
        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
        onClick={onToggle}
        disabled={disabled}
      >
        {show ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
      </Button>
    </div>
  </div>
);

const SecuritySettingsForm = ({
  passwordData,
  showPasswords,
  onPasswordChange,
  onShowPasswordToggle,
  onUpdate,
  isLoading,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Key className="w-5 h-5" />
          Change Password
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <PasswordInput
          id="current-password"
          label="Current Password"
          value={passwordData.currentPassword}
          onChange={(e) => onPasswordChange("currentPassword", e.target.value)}
          show={showPasswords.current}
          onToggle={() => onShowPasswordToggle("current")}
          disabled={isLoading}
        />
        <PasswordInput
          id="new-password"
          label="New Password"
          value={passwordData.newPassword}
          onChange={(e) => onPasswordChange("newPassword", e.target.value)}
          show={showPasswords.new}
          onToggle={() => onShowPasswordToggle("new")}
          disabled={isLoading}
        />
        <PasswordInput
          id="confirm-password"
          label="Confirm New Password"
          value={passwordData.confirmPassword}
          onChange={(e) => onPasswordChange("confirmPassword", e.target.value)}
          show={showPasswords.confirm}
          onToggle={() => onShowPasswordToggle("confirm")}
          disabled={isLoading}
        />
        <Button onClick={onUpdate} className="flex items-center gap-2" disabled={isLoading}>
          <Key className="w-4 h-4" />
          {isLoading ? "Changing..." : "Change Password"}
        </Button>
      </CardContent>
    </Card>
  );
};

export default SecuritySettingsForm; 