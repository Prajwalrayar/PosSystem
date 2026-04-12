# Backend Implementation Guide

## Super Admin: Notification + System Settings

All super-admin settings are API-driven and stored in a singleton database record.

### Endpoints

`GET /api/super-admin/settings`
- Loads both notification and system settings.
- Returns a single response object with `notificationSettings` and `systemSettings`.

`PATCH /api/super-admin/settings/notifications`
- Updates only notification toggles.

`PATCH /api/super-admin/settings/system`
- Updates only system toggles.

`PATCH /api/super-admin/settings`
- Optional combined save endpoint.
- Accepts both `notificationSettings` and `systemSettings`.

### Response Format

```json
{
  "success": true,
  "message": "Settings fetched",
  "data": {
    "notificationSettings": {
      "newStoreRequests": true,
      "storeApprovals": true,
      "commissionUpdates": false,
      "systemAlerts": true,
      "emailNotifications": true
    },
    "systemSettings": {
      "autoApproveStores": false,
      "requireDocumentVerification": true,
      "commissionAutoCalculation": true,
      "maintenanceMode": false
    }
  }
}
```

### Validation Notes

- Use request body validation for required JSON shapes in the frontend.
- The backend stores the settings as JSON columns and creates a default record on first access.
- The default values match the initial UI state shown in the sample response.
