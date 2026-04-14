import { createSlice } from "@reduxjs/toolkit";
import { logout } from "../user/userThunks";

const getItemUnitPrice = (item) => {
  const value = Number(item?.sellingPrice ?? item?.price ?? 0);
  return Number.isFinite(value) ? value : 0;
};

const toMoney = (value) => Number(Number(value || 0).toFixed(2));

const initialState = {
  items: [],
  selectedCustomer: null,
  note: "",
  discount: { type: "percentage", value: 0 },
  taxRate: 18,
  paymentMethod: "cash",
  heldOrders: [],
  currentOrder: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    addToCart: (state, action) => {
      const product = action.payload;
      const existingItem = state.items.find((item) => item.id === product.id);

      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        const productWithPrice = {
          ...product,
          quantity: 1,
        };
        state.items.push(productWithPrice);
      }
    },

    updateCartItemQuantity: (state, action) => {
      const { id, quantity } = action.payload;
      if (quantity <= 0) {
        state.items = state.items.filter((item) => item.id !== id);
      } else {
        const item = state.items.find((item) => item.id === id);
        if (item) {
          item.quantity = quantity;
        }
      }
    },

    removeFromCart: (state, action) => {
      const id = action.payload;
      state.items = state.items.filter((item) => item.id !== id);
    },

    clearCart: (state) => {
      state.items = [];
      state.selectedCustomer = null;
      state.note = "";
      state.discount = { type: "percentage", value: 0 };
      state.paymentMethod = "cash";
      state.currentOrder = null;
    },

    setSelectedCustomer: (state, action) => {
      state.selectedCustomer = action.payload;
    },

    setNote: (state, action) => {
      state.note = action.payload;
    },

    setDiscount: (state, action) => {
      state.discount = action.payload;
    },

    setTaxRate: (state, action) => {
      const parsed = Number(action.payload);
      state.taxRate = Number.isFinite(parsed) ? Math.max(0, parsed) : state.taxRate;
    },

    setPaymentMethod: (state, action) => {
      state.paymentMethod = action.payload;
    },

    holdOrder: (state) => {
      if (state.items.length > 0) {
        const heldOrder = {
          id: Date.now(),
          items: [...state.items],
          customer: state.selectedCustomer,
          note: state.note,
          discount: state.discount,
          timestamp: new Date(),
        };

        state.heldOrders.push(heldOrder);

        // Reset current order
        state.items = [];
        state.selectedCustomer = null;
        state.note = "";
        state.discount = { type: "percentage", value: 0 };
      }
    },

    resumeOrder: (state, action) => {
      const order = action.payload;
      state.items = order.items;
      state.selectedCustomer = order.customer;
      state.note = order.note;
      state.discount = order.discount;

      // Remove from held orders
      state.heldOrders = state.heldOrders.filter((o) => o.id !== order.id);
    },

    setCurrentOrder: (state, action) => {
      state.currentOrder = action.payload;
    },

    resetOrder: (state) => {
      state.items = [];
      state.selectedCustomer = null;
      state.note = "";
      state.discount = { type: "percentage", value: 0 };
      state.paymentMethod = "cash";
      state.currentOrder = null;
    },

    resetCheckoutContext: (state) => {
      state.selectedCustomer = null;
      state.note = "";
      state.discount = { type: "percentage", value: 0 };
      state.currentOrder = null;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(logout.fulfilled, () => ({ ...initialState }));
  },
});

// Selectors
export const selectCartItems = (state) => state.cart.items;
export const selectCartItemCount = (state) => state.cart.items.length;
export const selectSelectedCustomer = (state) => state.cart.selectedCustomer;
export const selectNote = (state) => state.cart.note;
export const selectDiscount = (state) => state.cart.discount;
export const selectTaxRate = (state) => state.cart.taxRate;
export const selectPaymentMethod = (state) => state.cart.paymentMethod;
export const selectHeldOrders = (state) => state.cart.heldOrders;
export const selectCurrentOrder = (state) => state.cart.currentOrder;

// Calculation selectors
export const selectSubtotal = (state) => {
  return toMoney(
    state.cart.items.reduce(
      (total, item) => total + getItemUnitPrice(item) * Number(item?.quantity || 0),
      0
    )
  );
};

export const selectTax = (state) => {
  const subtotal = selectSubtotal(state);
  const taxRate = Number(state.cart.taxRate || 0);
  return toMoney(subtotal * (taxRate / 100));
};

export const selectDiscountAmount = (state) => {
  const subtotal = selectSubtotal(state);
  const discount = state.cart.discount;
  const discountValue = Number(discount?.value || 0);

  if (discount.type === "percentage") {
    return toMoney(subtotal * (discountValue / 100));
  } else {
    return toMoney(discountValue);
  }
};

export const selectTotal = (state) => {
  const subtotal = selectSubtotal(state);
  const tax = selectTax(state);
  const discountAmount = selectDiscountAmount(state);
  return toMoney(subtotal + tax - discountAmount);
};

export const selectCartPricingBreakdown = (state) => {
  const subtotal = selectSubtotal(state);
  const taxRate = Number(state.cart.taxRate || 0);
  const taxAmount = selectTax(state);
  const discountType = state.cart.discount?.type || "percentage";
  const discountValue = Number(state.cart.discount?.value || 0);
  const discountAmount = selectDiscountAmount(state);
  const total = selectTotal(state);

  return {
    subtotal,
    taxRate,
    taxAmount,
    discountType,
    discountValue,
    discountAmount,
    total,
  };
};

export const {
  addToCart,
  updateCartItemQuantity,
  removeFromCart,
  clearCart,
  setSelectedCustomer,
  setNote,
  setDiscount,
  setTaxRate,
  setPaymentMethod,
  holdOrder,
  resumeOrder,
  setCurrentOrder,
  resetOrder,
  resetCheckoutContext,
} = cartSlice.actions;

export default cartSlice.reducer;
