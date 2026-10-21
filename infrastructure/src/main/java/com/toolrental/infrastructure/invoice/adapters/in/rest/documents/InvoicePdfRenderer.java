package com.toolrental.infrastructure.invoice.adapters.in.rest.documents;

import com.toolrental.application.invoice.dto.InvoiceDocumentResponse;
import com.toolrental.application.invoiceitem.dto.InvoiceItemResponse;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class InvoicePdfRenderer {
  private static final Charset PDF_CHARSET = Charset.forName("windows-1252");
  private static final int LINES_PER_PAGE = 13;
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "CO"))
          .withZone(ZoneId.of("America/Bogota"));

  public byte[] render(InvoiceDocumentResponse document) {
    var pages = paginate(document.lines());
    var objects = new ArrayList<byte[]>();
    objects.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));

    var pageReferences = new StringBuilder();
    for (var index = 0; index < pages.size(); index++) {
      pageReferences.append(3 + index * 2).append(" 0 R ");
    }
    objects.add(
        bytes(
            "<< /Type /Pages /Kids ["
                + pageReferences
                + "] /Count "
                + pages.size()
                + " >>"));

    for (var index = 0; index < pages.size(); index++) {
      var content = pageContent(document, pages.get(index), index + 1, pages.size());
      var contentId = 4 + index * 2;
      objects.add(
          bytes(
              "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                  + "/Resources << /Font << "
                  + "/F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >> "
                  + "/F2 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >> "
                  + ">> >> /Contents "
                  + contentId
                  + " 0 R >>"));
      var contentBytes = bytes(content);
      var stream = new ByteArrayOutputStream();
      write(stream, "<< /Length " + contentBytes.length + " >>\nstream\n");
      stream.writeBytes(contentBytes);
      write(stream, "\nendstream");
      objects.add(stream.toByteArray());
    }
    return assemble(objects);
  }

  private List<List<InvoiceItemResponse>> paginate(List<InvoiceItemResponse> lines) {
    var pages = new ArrayList<List<InvoiceItemResponse>>();
    if (lines.isEmpty()) {
      pages.add(List.of());
      return pages;
    }
    for (var start = 0; start < lines.size(); start += LINES_PER_PAGE) {
      pages.add(lines.subList(start, Math.min(lines.size(), start + LINES_PER_PAGE)));
    }
    return pages;
  }

  private String pageContent(
      InvoiceDocumentResponse document,
      List<InvoiceItemResponse> lines,
      int page,
      int pageCount) {
    var canvas = new StringBuilder();
    rectangle(canvas, 0, 742, 595, 100, "0.027 0.075 0.110");
    rectangle(canvas, 40, 764, 5, 48, "0 0.851 1");
    text(canvas, "F2", 22, 58, 793, "TOOL RENTAL", "1 1 1");
    text(canvas, "F1", 9, 59, 775, "EQUIPOS PARA CONSTRUIR MÁS", "0.690 0.784 0.835");
    text(canvas, "F2", 10, 365, 803, "COMPROBANTE ACADÉMICO", "0 0.851 1");
    text(canvas, "F2", 16, 365, 780, clip(document.invoice().number(), 25), "1 1 1");
    text(
        canvas,
        "F1",
        8,
        365,
        763,
        DATE_FORMAT.format(document.invoice().issuedAt()).toUpperCase(Locale.ROOT),
        "0.690 0.784 0.835");

    card(
        canvas,
        40,
        650,
        "PROVEEDOR",
        document.invoice().issuedSupplierName(),
        document.invoice().issuedSupplierDocument());
    card(
        canvas,
        307,
        650,
        "CLIENTE",
        document.invoice().issuedCustomerName(),
        document.invoice().issuedCustomerDocument());

    text(canvas, "F2", 9, 40, 626, "DETALLE", "0.118 0.165 0.227");
    text(
        canvas,
        "F1",
        8,
        475,
        626,
        "PÁGINA " + page + " / " + pageCount,
        "0.365 0.424 0.486");
    rectangle(canvas, 40, 596, 515, 24, "0.929 0.953 0.965");
    text(canvas, "F2", 8, 50, 604, "CONCEPTO", "0.118 0.165 0.227");
    text(canvas, "F2", 8, 365, 604, "CANT.", "0.118 0.165 0.227");
    text(canvas, "F2", 8, 420, 604, "VALOR", "0.118 0.165 0.227");
    text(canvas, "F2", 8, 495, 604, "TOTAL", "0.118 0.165 0.227");

    var y = 570;
    for (var line : lines) {
      line(canvas, 40, y - 7, 555, y - 7, "0.855 0.886 0.902");
      text(canvas, "F1", 9, 50, y, clip(line.issuedDescription(), 52), "0.086 0.133 0.188");
      text(canvas, "F1", 9, 370, y, quantity(line.quantity()), "0.278 0.333 0.396");
      text(canvas, "F1", 9, 420, y, money(line.issuedUnitPrice()), "0.278 0.333 0.396");
      text(
          canvas,
          "F2",
          9,
          495,
          y,
          money(line.quantity().multiply(line.issuedUnitPrice())),
          "0.086 0.133 0.188");
      y -= 28;
    }

    if (page == pageCount) {
      var totalY = Math.max(128, y - 60);
      rectangle(canvas, 338, totalY, 217, 54, "0.027 0.075 0.110");
      text(canvas, "F1", 9, 352, totalY + 31, "TOTAL " + document.currency(), "0.690 0.784 0.835");
      text(canvas, "F2", 19, 430, totalY + 26, money(document.total()), "1 1 1");
    }

    line(canvas, 40, 74, 555, 74, "0 0.851 1");
    text(canvas, "F2", 8, 40, 56, clip(document.legalNotice(), 82), "0.216 0.267 0.322");
    text(
        canvas,
        "F1",
        7,
        40,
        42,
        "El tratamiento tributario definitivo requiere validación profesional.",
        "0.420 0.467 0.518");
    return canvas.toString();
  }

  private void card(
      StringBuilder canvas, int x, int y, String label, String name, String document) {
    rectangle(canvas, x, y, 248, 70, "0.965 0.976 0.980");
    text(canvas, "F2", 8, x + 13, y + 48, label, "0 0.580 0.690");
    text(canvas, "F2", 11, x + 13, y + 29, clip(name, 38), "0.086 0.133 0.188");
    text(canvas, "F1", 8, x + 13, y + 13, clip(document, 38), "0.365 0.424 0.486");
  }

  private void text(
      StringBuilder canvas,
      String font,
      int size,
      int x,
      int y,
      String value,
      String color) {
    canvas
        .append("BT /")
        .append(font)
        .append(' ')
        .append(size)
        .append(" Tf ")
        .append(color)
        .append(" rg ")
        .append(x)
        .append(' ')
        .append(y)
        .append(" Td (")
        .append(escape(value))
        .append(") Tj ET\n");
  }

  private void rectangle(
      StringBuilder canvas, int x, int y, int width, int height, String color) {
    canvas
        .append(color)
        .append(" rg ")
        .append(x)
        .append(' ')
        .append(y)
        .append(' ')
        .append(width)
        .append(' ')
        .append(height)
        .append(" re f\n");
  }

  private void line(
      StringBuilder canvas, int fromX, int fromY, int toX, int toY, String color) {
    canvas
        .append(color)
        .append(" RG 0.7 w ")
        .append(fromX)
        .append(' ')
        .append(fromY)
        .append(" m ")
        .append(toX)
        .append(' ')
        .append(toY)
        .append(" l S\n");
  }

  private String money(BigDecimal value) {
    var symbols = DecimalFormatSymbols.getInstance(new Locale("es", "CO"));
    symbols.setDecimalSeparator(',');
    symbols.setGroupingSeparator('.');
    return "$ " + new DecimalFormat("#,##0.00", symbols).format(value);
  }

  private String quantity(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString().replace('.', ',');
  }

  private String clip(String value, int max) {
    if (value == null) return "";
    return value.length() <= max ? value : value.substring(0, max - 1) + "…";
  }

  private String escape(String value) {
    return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
  }

  private byte[] assemble(List<byte[]> objects) {
    var pdf = new ByteArrayOutputStream();
    write(pdf, "%PDF-1.4\n");
    pdf.writeBytes(
        new byte[] {
          (byte) '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, (byte) '\n'
        });
    var offsets = new long[objects.size() + 1];
    for (var index = 0; index < objects.size(); index++) {
      offsets[index + 1] = pdf.size();
      write(pdf, index + 1 + " 0 obj\n");
      pdf.writeBytes(objects.get(index));
      write(pdf, "\nendobj\n");
    }
    var xref = pdf.size();
    write(pdf, "xref\n0 " + (objects.size() + 1) + "\n");
    write(pdf, "0000000000 65535 f \n");
    for (var index = 1; index <= objects.size(); index++) {
      write(pdf, String.format(Locale.ROOT, "%010d 00000 n \n", offsets[index]));
    }
    write(
        pdf,
        "trailer\n<< /Size "
            + (objects.size() + 1)
            + " /Root 1 0 R >>\nstartxref\n"
            + xref
            + "\n%%EOF");
    return pdf.toByteArray();
  }

  private byte[] bytes(String value) {
    return value.getBytes(PDF_CHARSET);
  }

  private void write(ByteArrayOutputStream output, String value) {
    output.writeBytes(bytes(value));
  }
}
