import api from "@/utils/api";

const getAuthConfig = () => {
  const token = localStorage.getItem("jwt") || localStorage.getItem("token");
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

export const getSuperAdminSettings = () => {
  return api
    .get("/api/super-admin/settings", getAuthConfig())
    .then((response) => response.data);
};

export const saveSuperAdminNotificationSettings = (payload) => {
  return api
    .patch("/api/super-admin/settings/notifications", payload, getAuthConfig())
    .then((response) => response.data);
};

export const saveSuperAdminSystemSettings = (payload) => {
  return api
    .patch("/api/super-admin/settings/system", payload, getAuthConfig())
    .then((response) => response.data);
};

export const saveSuperAdminSettings = (payload) => {
  return api
    .patch("/api/super-admin/settings", payload, getAuthConfig())
    .then((response) => response.data);
};
