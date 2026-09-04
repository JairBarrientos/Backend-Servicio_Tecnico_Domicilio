package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    public byte[] generarPdf(Factura factura) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = 780;

                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(50, y);
                cs.showText("Servicio Técnico a Domicilio");
                cs.endText();
                y -= 25;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Recibo de pago");
                cs.endText();
                y -= 40;

                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(50, y);
                cs.showText("Factura N°: " + factura.getNumeroFactura());
                cs.endText();
                y -= 20;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Fecha de emisión: " +
                        (factura.getFechaEmision() != null ? factura.getFechaEmision().format(formato) : "-"));
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Ticket: " + factura.getTicket().getCodigoTicket());
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Cliente: " + factura.getTicket().getCliente().getNombres() + " "
                        + factura.getTicket().getCliente().getApellidos());
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Método de pago: " + factura.getMetodoPago());
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Estado de pago: " + factura.getEstadoPago());
                cs.endText();
                y -= 35;

                cs.moveTo(50, y);
                cs.lineTo(545, y);
                cs.stroke();
                y -= 25;

                cs.beginText();
                cs.setFont(fontNormal, 11);
                cs.newLineAtOffset(50, y);
                cs.showText("Subtotal:");
                cs.newLineAtOffset(400, 0);
                cs.showText("S/ " + factura.getSubtotal());
                cs.endText();
                y -= 18;

                cs.beginText();
                cs.setFont(fontNormal, 11);
                cs.newLineAtOffset(50, y);
                cs.showText("Descuento:");
                cs.newLineAtOffset(400, 0);
                cs.showText("S/ " + factura.getDescuento());
                cs.endText();
                y -= 18;

                cs.beginText();
                cs.setFont(fontNormal, 11);
                cs.newLineAtOffset(50, y);
                cs.showText("Impuesto:");
                cs.newLineAtOffset(400, 0);
                cs.showText("S/ " + factura.getImpuesto());
                cs.endText();
                y -= 25;

                cs.beginText();
                cs.setFont(fontBold, 14);
                cs.newLineAtOffset(50, y);
                cs.showText("TOTAL:");
                cs.newLineAtOffset(400, 0);
                cs.showText("S/ " + factura.getTotal());
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}