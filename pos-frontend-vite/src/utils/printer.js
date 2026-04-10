export function getActivePrinterId(settings) {
  return settings?.printer?.printerName || 'branch_printer_1';
}
