const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

export const getBranchCheckoutPolicy = (branchState) => {
  // Try multiple possible locations for settings data
  let settingsData = branchState?.branchSettings?.data;

  if (!settingsData) {
    return null;
  }

  // Ensure we're reading the right level
  // If settingsData has .data property, unwrap it
  if (settingsData.data && !settingsData.tax) {
    settingsData = settingsData.data;
  }

  const policy = {
    tax: {
      gstEnabled: Boolean(settingsData.tax?.gstEnabled),
      gstPercentage: toNumber(settingsData.tax?.gstPercentage),
    },
    payment: {
      acceptCash: Boolean(settingsData.payment?.acceptCash),
      acceptUPI: Boolean(settingsData.payment?.acceptUPI),
      acceptCard: Boolean(settingsData.payment?.acceptCard),
    },
    discount: {
      allowDiscount: Boolean(settingsData.discount?.allowDiscount),
      maxDiscountPercentage: toNumber(settingsData.discount?.maxDiscountPercentage),
      requireManagerApproval: Boolean(settingsData.discount?.requireManagerApproval),
    },
  };
  
  return policy;
};

export const getAllowedPaymentMethods = (policy) => {
  if (!policy) {
    return [];
  }
  
  const methods = [];
  if (policy?.payment?.acceptCash) methods.push("CASH");
  if (policy?.payment?.acceptUPI) methods.push("UPI");
  if (policy?.payment?.acceptCard) methods.push("CARD");
  return methods;
};
