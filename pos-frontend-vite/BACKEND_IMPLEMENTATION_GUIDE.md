# POS Backend Implementation Guide

Date: 2026-04-08
Scope: APIs missing behind current frontend simulations

## 1. Goal
This guide defines the backend APIs you should implement first, based on frontend handlers that currently run only local state, toast messages, mock data, or console logs.

It includes:
- Priority order
- Endpoint contracts
- Validation rules
- Suggested DB fields
- Frontend mapping
- Rollout checklist

---

## 2. Priority Roadmap

### P0 (implement first)
1. Change password for logged-in user (Super Admin settings)
2. Update profile for logged-in user (Super Admin settings)
3. Employee password reset (Branch Manager)
4. Customer loyalty points add/adjust (Cashier)

### P1
5. Super Admin commission management
6. Branch Manager settings save (printer, tax, payment, discount)
7. Store settings save all (store profile + notification + security + payment)

### P2
8. Export generation/download (Super Admin + Branch Manager)
9. Print workflow integration (invoice/shift summary)
10. Customer return flow initiation from order history
11. Employee performance analytics API

---

## 3. API Standards (recommended)

## Base
- Prefix: /api
- Auth: Bearer JWT
- Content-Type: application/json

## Response envelope
Success:
{
  "success": true,
  "message": "...",
  "data": {}
}

Error:
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    { "field": "newPassword", "message": "Must be at least 8 characters" }
  ]
}

## HTTP conventions
- 200 OK: fetch or update success
- 201 Created: new resource
- 400 Bad Request: payload invalid
- 401 Unauthorized: missing/invalid token
- 403 Forbidden: role restriction
- 404 Not Found: resource missing
- 409 Conflict: duplicate/illegal transition
- 422 Unprocessable Entity: business validation failed

---

## 4. Endpoint Specification by Feature

## 4.1 Super Admin: Update Profile

Frontend currently simulated:
- src/pages/SuperAdminDashboard/settings/components/useSettingsState.js
- handleProfileUpdate

### Endpoint
PATCH /api/users/me/profile

### Request
{
  "fullName": "John Doe",
  "phone": "+919876543210"
}

### Validation
- fullName: required, 2-100 chars
- phone: optional, E.164 or local normalized format

### Response
{
  "success": true,
  "message": "Profile updated",
  "data": {
    "id": "user_id",
    "fullName": "John Doe",
    "email": "john@x.com",
    "mobile": "+919876543210"
  }
}

---

## 4.2 Super Admin: Change Password (logged-in)

Frontend currently simulated:
- src/pages/SuperAdminDashboard/settings/components/useSettingsState.js
- handlePasswordChange

### Endpoint
PATCH /api/auth/change-password

### Request
{
  "currentPassword": "OldPass@123",
  "newPassword": "NewPass@123",
  "confirmPassword": "NewPass@123"
}

### Validation
- currentPassword required
- newPassword min 8, upper+lower+digit+special recommended
- newPassword != currentPassword
- confirmPassword must match newPassword

### Response
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}

### Security
- Hash with bcrypt/argon2
- Invalidate other sessions/tokens (recommended)
- Rate limit by user and IP

---

## 4.3 Branch Manager: Employee Password Reset

Frontend currently simulated:
- src/pages/Branch Manager/Employees/BranchEmployees.jsx
- handleResetPassword

### Endpoint Option A (admin sets temporary password)
PATCH /api/employees/:employeeId/reset-password

Request:
{
  "temporaryPassword": "Temp@1234",
  "forceChangeOnNextLogin": true
}

### Endpoint Option B (server generates password and emails)
POST /api/employees/:employeeId/reset-password

Request:
{
  "sendEmail": true
}

Response:
{
  "success": true,
  "message": "Password reset initiated",
  "data": {
    "employeeId": "...",
    "forceChangeOnNextLogin": true
  }
}

### Authorization
- ROLE_BRANCH_ADMIN can reset only employees of their branch

---

## 4.4 Cashier: Add Loyalty Points

Frontend currently simulated:
- src/pages/cashier/customer/CustomerLookupPage.jsx
- handleAddPoints

### Endpoint
POST /api/customers/:customerId/loyalty/transactions

### Request
{
  "type": "ADD",
  "points": 100,
  "reason": "manual_adjustment",
  "note": "Festival bonus"
}

### Response
{
  "success": true,
  "message": "Points added",
  "data": {
    "customerId": "...",
    "pointsBefore": 240,
    "pointsChanged": 100,
    "pointsAfter": 340,
    "transactionId": "..."
  }
}

### Validation
- points > 0
- upper safety cap per operation (example 5000)
- audit log required

---

## 4.5 Super Admin: Commission Management

Frontend currently simulated:
- src/pages/SuperAdminDashboard/CommissionsPage.jsx
- mockCommissions + confirmEditCommission

### Endpoints
GET /api/super-admin/commissions
PATCH /api/super-admin/commissions/:storeId

### PATCH Request
{
  "rate": 2.75
}

### Validation
- rate range: 0 to 10 (or your business rule)
- optional effectiveFrom date if needed

### Response
{
  "success": true,
  "message": "Commission updated",
  "data": {
    "storeId": "...",
    "previousRate": 2.5,
    "currentRate": 2.75,
    "updatedAt": "2026-04-08T12:30:00Z"
  }
}

---

## 4.6 Branch Manager: Settings Save APIs

Frontend currently simulated:
- src/pages/Branch Manager/Settings/Settings.jsx
- handleSaveSettings for printer/tax/payment/discount

### Option A (single endpoint)
PATCH /api/branches/:branchId/settings

Request:
{
  "printer": {
    "printerName": "Epson TM-T88VI",
    "paperSize": "80mm",
    "printLogo": true,
    "printCustomerDetails": true,
    "printItemizedTax": true,
    "footerText": "Thank you"
  },
  "tax": {
    "gstEnabled": true,
    "gstPercentage": 18,
    "applyGstToAll": true,
    "showTaxBreakdown": true
  },
  "payment": {
    "acceptCash": true,
    "acceptUPI": true,
    "acceptCard": true,
    "upiId": "store@upi",
    "cardTerminalId": "TERM123"
  },
  "discount": {
    "allowDiscount": true,
    "maxDiscountPercentage": 10,
    "requireManagerApproval": true,
    "discountReasons": ["Damaged Product", "Bulk Purchase"]
  }
}

### Option B (split endpoints)
- PATCH /api/branches/:branchId/settings/printer
- PATCH /api/branches/:branchId/settings/tax
- PATCH /api/branches/:branchId/settings/payment
- PATCH /api/branches/:branchId/settings/discount

---

## 4.7 Store Admin: Save All Settings

Frontend currently simulated:
- src/pages/store/Settings/components/SettingsContent.jsx
- onSave

### Endpoint
PATCH /api/stores/:storeId/settings

### Request
{
  "storeSettings": {
    "storeName": "My POS Store",
    "storeEmail": "contact@store.com",
    "storePhone": "+1...",
    "storeAddress": "...",
    "storeDescription": "...",
    "currency": "USD",
    "taxRate": 7.5,
    "timezone": "America/New_York",
    "dateFormat": "MM/DD/YYYY",
    "receiptFooter": "Thanks"
  },
  "notificationSettings": {
    "emailNotifications": true,
    "smsNotifications": false,
    "lowStockAlerts": true,
    "salesReports": true,
    "employeeActivity": true
  },
  "securitySettings": {
    "twoFactorAuth": false,
    "passwordExpiry": 90,
    "sessionTimeout": 30,
    "ipRestriction": false
  },
  "paymentSettings": {
    "acceptCash": true,
    "acceptCredit": true,
    "acceptDebit": true,
    "acceptMobile": true,
    "stripeEnabled": false,
    "paypalEnabled": false
  }
}

---

## 4.8 Export APIs (Super Admin + Branch Manager)

Frontend currently simulated:
- src/pages/SuperAdminDashboard/ExportsPage.jsx
- src/pages/Branch Manager/Reports/Reports.jsx handleExport

### Recommended async export flow
1. POST /api/exports
2. GET /api/exports/:exportId (status polling)
3. GET /api/exports/:exportId/download (signed URL or stream)

### Create export request
{
  "type": "STORE_LIST",
  "format": "CSV",
  "filters": {
    "from": "2026-04-01",
    "to": "2026-04-08",
    "branchId": "..."
  }
}

### Export status response
{
  "success": true,
  "data": {
    "id": "exp_123",
    "status": "PROCESSING",
    "progress": 45,
    "expiresAt": "2026-04-09T00:00:00Z"
  }
}

---

## 4.9 Print Integration APIs

Frontend currently simulated:
- src/pages/Branch Manager/Orders/Orders.jsx handlePrintInvoice
- src/pages/cashier/order/OrderHistoryPage.jsx handlePrintInvoice
- src/pages/cashier/order/OrderDetails/InvoiceDialog.jsx handlePrintInvoice
- src/pages/cashier/ShiftSummary/ShiftSummaryPage.jsx handlePrintSummary

### Option A (PDF generation)
- GET /api/orders/:orderId/invoice.pdf
- GET /api/shift-reports/:shiftId/summary.pdf

### Option B (print job service)
- POST /api/print/jobs

Request:
{
  "type": "INVOICE",
  "referenceId": "order_id",
  "printerId": "branch_printer_1"
}

---

## 4.10 Return Initiation API

Frontend currently simulated:
- src/pages/cashier/order/OrderHistoryPage.jsx handleInitiateReturn

### Endpoint
POST /api/returns/initiate

### Request
{
  "orderId": "...",
  "cashierId": "...",
  "branchId": "..."
}

### Response
{
  "success": true,
  "message": "Return session created",
  "data": {
    "returnSessionId": "ret_sess_123",
    "eligibleItems": [ ... ]
  }
}

---

## 4.11 Employee Performance API

Frontend currently simulated:
- src/pages/Branch Manager/Employees/EmployeeDialogs.jsx PerformanceDialog

### Endpoint
GET /api/employees/:employeeId/performance?from=2026-03-01&to=2026-03-31

### Response
{
  "success": true,
  "data": {
    "ordersProcessed": 127,
    "totalSales": 78450,
    "averageOrderValue": 617,
    "dailySales": [
      { "date": "2026-03-01", "amount": 2100 }
    ],
    "activityLog": [
      { "at": "2026-03-20T10:00:00Z", "event": "Updated stock for 12 products" }
    ]
  }
}

---

## 5. Data Model Additions (suggested)

1. user_security
- user_id
- password_changed_at
- force_password_change

2. loyalty_transactions
- id
- customer_id
- type (ADD, DEDUCT)
- points
- reason
- note
- created_by
- created_at

3. commission_history
- id
- store_id
- old_rate
- new_rate
- updated_by
- updated_at

4. branch_settings
- branch_id
- printer_json
- tax_json
- payment_json
- discount_json
- updated_at

5. store_settings
- store_id
- store_json
- notification_json
- security_json
- payment_json
- updated_at

6. export_jobs
- id
- type
- format
- filters_json
- status
- file_url
- progress
- created_by
- created_at
- expires_at

7. print_jobs (if external print queue)
- id
- type
- reference_id
- printer_id
- status
- error
- created_at

---

## 6. Role/Permission Matrix

1. ROLE_SUPER_ADMIN
- manage commissions
- export global data
- update own profile/password

2. ROLE_BRANCH_ADMIN
- update branch settings
- reset branch employee passwords
- view employee performance
- export branch reports

3. ROLE_BRANCH_CASHIER
- add loyalty points (if allowed)
- initiate return for eligible orders
- print invoices/shift summary

4. ROLE_STORE_ADMIN
- save store settings
- update own profile/password

---

## 7. Frontend Wiring Checklist After Backend Ready

1. Add missing thunks in Redux Toolkit
- changePassword
- updateProfile
- resetEmployeePassword
- addCustomerPoints
- updateCommissionRate
- saveBranchSettings
- saveStoreSettings
- createExport / pollExport / downloadExport
- initiateReturn
- getEmployeePerformance

2. Replace local handlers
- swap toast-only handlers with dispatch(thunk)
- show loading states on buttons
- show server errors using toast variant destructive

3. Remove mock/static data where API exists
- Commissions page
- Employee performance dialog
- Export history blocks if backend returns list

---

## 8. Test Cases (minimum)

1. Change password
- wrong current password
- weak new password
- success path

2. Employee reset password
- unauthorized role
- different branch employee access denied
- success + audit log

3. Loyalty points
- negative points rejected
- over-limit rejected
- success updates balance correctly

4. Export flow
- create job
- poll to completed
- download link works

5. Settings save
- partial update
- full update
- invalid schema rejected

---

## 9. Suggested Delivery Order (1-week sprint split)

Day 1-2:
- change password
- profile update
- employee reset password
- loyalty points transaction API

Day 3-4:
- branch settings API
- store settings API
- commission APIs

Day 5:
- export job APIs
- print PDF endpoints
- return initiation API
- employee performance API skeleton

---

## 10. Final Notes

1. Keep all new writes audited (who changed what, when).
2. Avoid silent success in frontend: always return explicit message/data.
3. For every mutation endpoint, include idempotency strategy where needed.
4. Start with P0 to remove critical fake-success behavior from production UX.
