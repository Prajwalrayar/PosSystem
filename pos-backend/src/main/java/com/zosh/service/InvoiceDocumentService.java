package com.zosh.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.zosh.modal.Invoice;
import com.zosh.modal.InvoiceLineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

@Service
@RequiredArgsConstructor
public class InvoiceDocumentService {

    @Value("${app.brand-name:SmartPos}")
    private String brandName;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generatePdf(Invoice invoice) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(buildHtml(invoice), "https://smartpos.local/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            try {
                return generateFallbackPdf(invoice);
            } catch (Exception fallbackEx) {
                throw new IllegalStateException("Failed to generate invoice PDF", fallbackEx);
            }
        }
    }

    public String buildHtml(Invoice invoice) {
        StringBuilder rows = new StringBuilder();
        for (InvoiceLineItem item : invoice.getLineItems()) {
            rows.append("<tr>")
                    .append(td(escape(item.getItemName()), "text"))
                    .append(td(escape(nullSafe(item.getSku())), "text"))
                    .append(td(String.valueOf(item.getQuantity()), "num"))
                    .append(td(money(item.getUnitPrice()), "num"))
                    .append(td(tax(item.getTaxPercent()), "num"))
                    .append(td(money(item.getDiscountAmount()), "num"))
                    .append(td(money(item.getLineTotal()), "num"))
                    .append("</tr>");
        }

        StringBuilder html = new StringBuilder();
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        html.append("<!DOCTYPE html>");
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
        html.append("<meta charset=\"UTF-8\" />");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;color:#1f2937;margin:0;padding:0;background:#d7d7d7;}");
        html.append(".shell{padding:20px 0;}");
        html.append(".sheet{width:92%;max-width:720px;margin:0 auto;background:#ffffff;border:1px solid #d7d7d7;}");
        html.append(".pad{padding:14px 12px 12px 12px;}");
        html.append(".top{width:100%;border-collapse:collapse;}");
        html.append(".top td{vertical-align:top;}");
        html.append(".brand{font-size:15px;font-weight:700;color:#222;line-height:1.1;}");
        html.append(".invoice-no{font-size:8px;color:#7a7a7a;line-height:1.2;}");
        html.append(".invoice-date{text-align:right;font-size:9px;color:#222;line-height:1.25;}");
        html.append(".invoice-date .label{font-size:8px;font-weight:700;color:#222;}");
        html.append(".section{margin-top:10px;}");
        html.append(".section-title{font-size:10px;font-weight:700;color:#222;line-height:1.2;margin-bottom:2px;}");
        html.append(".block{font-size:9px;line-height:1.3;color:#222;}");
        html.append(".block .line{display:block;}");
        html.append(".block .muted{color:#222;}");
        html.append(".items{width:100%;border-collapse:collapse;margin-top:2px;table-layout:fixed;}");
        html.append(".items th,.items td{padding:4px 4px;font-size:8.5px;line-height:1.2;color:#222;vertical-align:top;word-wrap:break-word;overflow-wrap:anywhere;}");
        html.append(".items th{font-weight:700;border-top:1px solid #bdbdbd;border-bottom:1px solid #bdbdbd;text-align:left;}");
        html.append(".items td{border-bottom:1px solid #d0d0d0;}");
        html.append(".items th.num,.items td.num{text-align:right;}");
        html.append(".items td.text{text-align:left;}");
        html.append(".summary{width:100%;border-collapse:collapse;margin-top:7px;}");
        html.append(".summary td{padding:1px 0;font-size:9px;line-height:1.2;color:#222;}");
        html.append(".summary .label{text-align:right;padding-right:12px;width:82%;}");
        html.append(".summary .value{text-align:right;width:18%;white-space:nowrap;font-weight:700;}");
        html.append(".summary .grand .label,.summary .grand .value{padding-top:3px;}");
        html.append(".footer{margin-top:14px;font-size:7px;color:#9a9a9a;line-height:1.2;}");
        html.append(".summary-wrap{width:100%;border-collapse:collapse;}");
        html.append(".summary-wrap td{padding:0;vertical-align:top;}");
        html.append("</style></head><body><div class=\"shell\"><div class=\"sheet\"><div class=\"pad\">");
        html.append("<table class=\"top\"><tr>");
        html.append("<td><div class=\"brand\">")
                .append(escape(brandName))
                .append("</div><div class=\"invoice-no\">Invoice ")
                .append(escape(invoice.getInvoiceNumber()))
                .append("</div></td>");
        html.append("<td class=\"invoice-date\"><div class=\"label\">Invoice Date</div><div>")
                .append(escape(invoice.getInvoiceDateTime().format(DATE_TIME_FORMATTER)))
                .append("</div></td>");
        html.append("</tr></table>");

        html.append("<div class=\"section\">")
                .append(sectionTitle("Bill To"))
                .append(block(invoice.getCustomerName(), invoice.getCustomerEmail(), invoice.getCustomerPhone()))
                .append(sectionTitle("Store / Branch"))
                .append(block(invoice.getStoreName(),
                        invoice.getBranchName(),
                        invoice.getStoreAddress(),
                        invoice.getBranchAddress(),
                        invoice.getStorePhone(),
                        invoice.getBranchPhone(),
                        invoice.getStoreEmail(),
                        invoice.getBranchEmail()))
                .append("</div>");

        html.append("<div class=\"section\">");
        html.append("<table class=\"items\"><colgroup>");
        html.append("<col style=\"width:18%\" />");
        html.append("<col style=\"width:13%\" />");
        html.append("<col style=\"width:8%\" />");
        html.append("<col style=\"width:16%\" />");
        html.append("<col style=\"width:12%\" />");
        html.append("<col style=\"width:15%\" />");
        html.append("<col style=\"width:18%\" />");
        html.append("</colgroup><thead><tr>");
        html.append("<th>Item</th><th>SKU</th><th class=\"num\">Qty</th><th class=\"num\">Unit Price</th><th class=\"num\">Tax</th><th class=\"num\">Discount</th><th class=\"num\">Line Total</th>");
        html.append("</tr></thead><tbody>");
        html.append(rows);
        html.append("</tbody></table></div>");

        html.append("<div class=\"section\"><table class=\"summary-wrap\"><tr><td>");
        html.append("<table class=\"summary\">");
        html.append(summaryRow("Subtotal", money(invoice.getSubtotal())));
        html.append(summaryRow("Tax", money(invoice.getTaxTotal())));
        html.append(summaryRow("Discount", money(invoice.getDiscountTotal())));
        html.append(summaryRow("Grand Total", money(invoice.getGrandTotal()), "grand"));
        html.append("</table>");
        html.append("</td></tr></table></div>");

        html.append("<div class=\"section\">")
                .append(sectionTitle("Cashier"))
                .append(block(invoice.getCashierName()))
                .append(sectionTitle("Payment"))
                .append(block("Method: " + nullSafe(invoice.getPaymentMethod()), "Reference: " + nullSafe(invoice.getPaymentReference())))
                .append("</div>");

        html.append("<div class=\"footer\">");
        html.append("Generated by ").append(escape(brandName)).append(". ");
        html.append("Keep this invoice for records and returns.");
        html.append("</div>");
        html.append("</div></div></div></body></html>");
        return html.toString();
    }

    public String buildPlainText(Invoice invoice) {
        StringBuilder text = new StringBuilder();
        text.append(brandName).append("\n");
        text.append("Invoice No: ").append(invoice.getInvoiceNumber()).append("\n");
        text.append("Invoice Date: ").append(invoice.getInvoiceDateTime().format(DATE_TIME_FORMATTER)).append("\n\n");
        text.append("Bill To\n");
        text.append(nullSafe(invoice.getCustomerName())).append("\n");
        text.append(nullSafe(invoice.getCustomerEmail())).append("\n");
        text.append(nullSafe(invoice.getCustomerPhone())).append("\n\n");
        text.append("Store / Branch\n");
        text.append(nullSafe(invoice.getStoreName())).append("\n");
        text.append(nullSafe(invoice.getBranchName())).append("\n");
        text.append(nullSafe(invoice.getStoreAddress())).append("\n");
        text.append(nullSafe(invoice.getStorePhone())).append("\n\n");
        text.append("Items\n");
        text.append("Item        SKU   Qty   Unit Price   Tax   Disc   Total\n");
        for (InvoiceLineItem item : invoice.getLineItems()) {
            text.append(String.format(Locale.US,
                    "%s        %s   %s     %s   %s   %s   %s%n",
                    nullSafe(item.getItemName()),
                    nullSafe(item.getSku()),
                    item.getQuantity() == null ? "0" : item.getQuantity().toString(),
                    money(item.getUnitPrice()),
                    tax(item.getTaxPercent()),
                    money(item.getDiscountAmount()),
                    money(item.getLineTotal())));
        }
        text.append("\nSummary\n");
        text.append("Subtotal        ").append(money(invoice.getSubtotal())).append("\n");
        text.append("Tax             ").append(money(invoice.getTaxTotal())).append("\n");
        text.append("Discount        ").append(money(invoice.getDiscountTotal())).append("\n");
        text.append("Grand Total     ").append(money(invoice.getGrandTotal())).append("\n\n");
        text.append("Payment\n");
        text.append("Method: ").append(nullSafe(invoice.getPaymentMethod())).append("\n");
        text.append("Reference: ").append(nullSafe(invoice.getPaymentReference())).append("\n");
        text.append("Cashier: ").append(nullSafe(invoice.getCashierName())).append("\n");
        return text.toString();
    }

    private String sectionTitle(String title) {
        return "<div class=\"section-title\">" + escape(title) + "</div>";
    }

    private String summaryRow(String label, String value) {
        return summaryRow(label, value, "");
    }

    private String summaryRow(String label, String value, String rowClass) {
        return "<tr class=\"" + escape(rowClass) + "\"><td class=\"label\">" + escape(label) + "</td><td class=\"value\">" + escape(value) + "</td></tr>";
    }

    private String tax(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        return safeValue.toPlainString() + "%";
    }

    private String money(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "INR %.2f", safeValue);
    }

    private String td(String value, String className) {
        return "<td class=\"" + escape(className) + "\">" + escape(value) + "</td>";
    }

    private String block(String... lines) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"block\">");
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            html.append("<span class=\"line\">").append(escape(line)).append("</span>");
        }
        html.append("</div>");
        return html.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private byte[] generateFallbackPdf(Invoice invoice) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float y = 780;
                content.beginText();
                content.setFont(boldFont, 18);
                content.newLineAtOffset(50, y);
                content.showText(brandName);

                y -= 24;
                content.setFont(boldFont, 12);
                content.newLineAtOffset(0, -24);
                content.showText("Invoice: " + nullSafe(invoice.getInvoiceNumber()));

                y -= 18;
                content.newLineAtOffset(0, -18);
                content.showText("Customer: " + nullSafe(invoice.getCustomerName()));

                y -= 18;
                content.newLineAtOffset(0, -18);
                content.showText("Email: " + nullSafe(invoice.getCustomerEmail()));

                y -= 24;
                content.newLineAtOffset(0, -24);
                content.showText("Items:");

                content.setFont(normalFont, 11);
                for (InvoiceLineItem item : invoice.getLineItems()) {
                    y -= 16;
                    content.newLineAtOffset(0, -16);
                    String line = String.format(
                            "%s | Qty: %s | Total: %s",
                            nullSafe(item.getItemName()),
                            item.getQuantity() == null ? "0" : item.getQuantity().toString(),
                            money(item.getLineTotal())
                    );
                    content.showText(line);
                }

                y -= 20;
                content.newLineAtOffset(0, -20);
                content.setFont(boldFont, 11);
                content.showText("Grand Total: " + money(invoice.getGrandTotal()));
                content.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
