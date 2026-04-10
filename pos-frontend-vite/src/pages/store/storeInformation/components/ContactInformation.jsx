import React from "react";
import { MapPin, Phone, Mail, User } from "lucide-react";
import { Label } from "@/components/ui/label";

const ContactInformation = ({ storeData }) => {
  const ownerName =
    storeData.storeAdmin?.fullName ||
    storeData.storeAdmin?.username ||
    storeData.storeAdmin?.email ||
    "No owner name provided";
  const phone = storeData.contact?.phone || storeData.storeAdmin?.phone || "No phone provided";
  const email = storeData.contact?.email || storeData.storeAdmin?.email || "No email provided";

  return (
    <div>
      <h3 className="text-lg font-semibold mb-4">Contact Information</h3>
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <User className="h-4 w-4 text-gray-400" />
          <div>
            <Label className="text-sm text-muted-foreground">Store Admin</Label>
            <p className="text-base">{ownerName}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <MapPin className="h-4 w-4 text-gray-400" />
          <div>
            <Label className="text-sm text-muted-foreground">Address</Label>
            <p className="text-base">{storeData.contact?.address || "No address provided"}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Phone className="h-4 w-4 text-gray-400" />
          <div>
            <Label className="text-sm text-muted-foreground">Phone</Label>
            <p className="text-base">{phone}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Mail className="h-4 w-4 text-gray-400" />
          <div>
            <Label className="text-sm text-muted-foreground">Email</Label>
            <p className="text-base">{email}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactInformation; 