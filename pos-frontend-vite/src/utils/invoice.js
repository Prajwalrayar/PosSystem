export const getInvoiceIdFromOrder = (order) =>
  order?.invoiceId ||
  order?.invoice?.id ||
  order?.invoice?.invoiceId ||
  order?.invoice?.data?.id ||
  null;

export const getInvoicePdfUrlFromOrder = (order) =>
  order?.invoicePdfUrl ||
  order?.invoice?.pdfUrl ||
  order?.invoice?.invoicePdfUrl ||
  order?.invoice?.data?.pdfUrl ||
  null;

export const getInvoiceDeliveryStatusFromOrder = (order) =>
  order?.invoiceDeliveryStatus ||
  order?.deliveryStatus ||
  order?.invoice?.deliveryStatus ||
  order?.invoice?.status ||
  null;
