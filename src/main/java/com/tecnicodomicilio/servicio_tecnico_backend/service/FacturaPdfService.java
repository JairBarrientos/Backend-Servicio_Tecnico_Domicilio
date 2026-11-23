package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    private static final String NOMBRE_APP = "GabyTech";
    private static final Color AZUL_OSCURO = new Color(20, 33, 61);
    private static final Color INDIGO = new Color(79, 70, 229);
    private static final Color VERDE = new Color(5, 150, 105);
    private static final Color GRIS = new Color(107, 114, 128);
    private static final Color GRIS_CLARO = new Color(203, 213, 225);
    private static final Color TEXTO = new Color(31, 41, 55);
    private static final Color FONDO_SUAVE = new Color(238, 242, 255);
    private static final Color LINEA = new Color(221, 227, 238);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float MARGEN = 40f;

    public byte[] generarPdf(Factura factura) throws IOException {
        Ticket ticket = factura.getTicket();
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            float ancho = pagina.getMediaBox().getWidth();
            float alto = pagina.getMediaBox().getHeight();
            float derecha = ancho - MARGEN;
            float columnaDerecha = ancho / 2 + 20;
            float anchoColumna = columnaDerecha - MARGEN - 20;

            String cliente = ticket.getCliente().getNombres() + " " + ticket.getCliente().getApellidos();
            String tecnico = ticket.getTecnicoAsignado() != null
                    ? ticket.getTecnicoAsignado().getNombres() + " " + ticket.getTecnicoAsignado().getApellidos()
                    : "-";
            String servicio = ticket.getServicio() != null ? ticket.getServicio().getNombreServicio() : ticket.getTitulo();
            String fechaEmision = factura.getFechaEmision() != null ? factura.getFechaEmision().format(FORMATO_FECHA) : "-";

            try (PDPageContentStream cs = new PDPageContentStream(documento, pagina)) {
                rectangulo(cs, AZUL_OSCURO, 0, alto - 120, ancho, 120);
                texto(cs, negrita, 26, Color.WHITE, MARGEN, alto - 62, NOMBRE_APP);
                texto(cs, normal, 11, GRIS_CLARO, MARGEN, alto - 82, "Servicio técnico a domicilio");
                textoDerecha(cs, negrita, 18, Color.WHITE, derecha, alto - 55, "FACTURA");
                textoDerecha(cs, normal, 12, GRIS_CLARO, derecha, alto - 75, factura.getNumeroFactura());

                float y = alto - 165;
                etiquetaValor(cs, normal, negrita, MARGEN, y, anchoColumna, "CLIENTE", cliente);
                etiquetaValor(cs, normal, negrita, columnaDerecha, y, anchoColumna, "FECHA DE EMISIÓN", fechaEmision);
                y -= 44;
                etiquetaValor(cs, normal, negrita, MARGEN, y, anchoColumna, "TÉCNICO", tecnico);
                etiquetaValor(cs, normal, negrita, columnaDerecha, y, anchoColumna, "TICKET", ticket.getCodigoTicket());
                y -= 44;
                etiquetaValor(cs, normal, negrita, MARGEN, y, anchoColumna, "SERVICIO", servicio);
                etiquetaValor(cs, normal, negrita, columnaDerecha, y, anchoColumna, "MÉTODO DE PAGO",
                        metodoLegible(factura.getMetodoPago()));

                y -= 56;
                rectangulo(cs, FONDO_SUAVE, MARGEN, y - 9, derecha - MARGEN, 28);
                texto(cs, negrita, 10, INDIGO, MARGEN + 12, y, "DESCRIPCIÓN");
                textoDerecha(cs, negrita, 10, INDIGO, derecha - 12, y, "IMPORTE");

                y -= 36;
                float anchoDescripcion = derecha - MARGEN - 150;
                texto(cs, negrita, 12, TEXTO, MARGEN + 12, y, ajustar(negrita, 12, servicio, anchoDescripcion));
                textoDerecha(cs, normal, 12, TEXTO, derecha - 12, y, dinero(factura.getSubtotal()));

                y -= 16;
                texto(cs, normal, 10, GRIS, MARGEN + 12, y, ajustar(normal, 10, ticket.getTitulo(), anchoDescripcion));

                y -= 18;
                linea(cs, LINEA, MARGEN, y, derecha, y);

                float etiquetaX = derecha - 210;
                y -= 26;
                texto(cs, normal, 11, GRIS, etiquetaX, y, "Subtotal (sin IGV)");
                textoDerecha(cs, normal, 11, TEXTO, derecha - 12, y, dinero(factura.getSubtotal()));

                y -= 20;
                texto(cs, normal, 11, GRIS, etiquetaX, y, "IGV (18%)");
                textoDerecha(cs, normal, 11, TEXTO, derecha - 12, y, dinero(factura.getImpuesto()));

                if (factura.getDescuento() != null && factura.getDescuento().signum() > 0) {
                    y -= 20;
                    texto(cs, normal, 11, GRIS, etiquetaX, y, "Descuento");
                    textoDerecha(cs, normal, 11, TEXTO, derecha - 12, y, "- " + dinero(factura.getDescuento()));
                }

                y -= 38;
                rectangulo(cs, INDIGO, etiquetaX - 14, y - 11, derecha - etiquetaX + 14, 36);
                texto(cs, negrita, 13, Color.WHITE, etiquetaX, y, "TOTAL");
                textoDerecha(cs, negrita, 15, Color.WHITE, derecha - 12, y, dinero(factura.getTotal()));

                boolean pagado = "PAGADO".equals(factura.getEstadoPago());
                texto(cs, negrita, 12, pagado ? VERDE : GRIS, MARGEN, y, pagado ? "PAGADO" : "PENDIENTE DE PAGO");
                if (pagado && factura.getFechaPago() != null) {
                    texto(cs, normal, 10, GRIS, MARGEN, y - 16, "Pagado el " + factura.getFechaPago().format(FORMATO_FECHA));
                }

                linea(cs, LINEA, MARGEN, 70, derecha, 70);
                centrado(cs, normal, 10, GRIS, ancho, 52, "Gracias por confiar en " + NOMBRE_APP);
                centrado(cs, normal, 9, GRIS, ancho, 38, "El precio final incluye IGV (18%).");
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private void etiquetaValor(PDPageContentStream cs, PDType1Font normal, PDType1Font negrita,
                                float x, float y, float anchoMax, String etiqueta, String valor) throws IOException {
        texto(cs, normal, 9, GRIS, x, y, etiqueta);
        texto(cs, negrita, 12, TEXTO, x, y - 16, ajustar(negrita, 12, valor, anchoMax));
    }

    private void rectangulo(PDPageContentStream cs, Color color, float x, float y, float ancho, float alto) throws IOException {
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, ancho, alto);
        cs.fill();
    }

    private void linea(PDPageContentStream cs, Color color, float x1, float y1, float x2, float y2) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(1f);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private void texto(PDPageContentStream cs, PDType1Font fuente, float tamano, Color color,
                        float x, float y, String texto) throws IOException {
        cs.beginText();
        cs.setFont(fuente, tamano);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(limpiar(texto));
        cs.endText();
    }

    private void textoDerecha(PDPageContentStream cs, PDType1Font fuente, float tamano, Color color,
                               float xDerecha, float y, String texto) throws IOException {
        float ancho = anchoDe(fuente, tamano, limpiar(texto));
        texto(cs, fuente, tamano, color, xDerecha - ancho, y, texto);
    }

    private void centrado(PDPageContentStream cs, PDType1Font fuente, float tamano, Color color,
                           float anchoPagina, float y, String texto) throws IOException {
        float ancho = anchoDe(fuente, tamano, limpiar(texto));
        texto(cs, fuente, tamano, color, (anchoPagina - ancho) / 2, y, texto);
    }

    private float anchoDe(PDType1Font fuente, float tamano, String texto) throws IOException {
        return fuente.getStringWidth(texto) / 1000f * tamano;
    }

    private String ajustar(PDType1Font fuente, float tamano, String texto, float anchoMax) throws IOException {
        String limpio = limpiar(texto);
        if (anchoDe(fuente, tamano, limpio) <= anchoMax) return limpio;

        while (limpio.length() > 1 && anchoDe(fuente, tamano, limpio + "...") > anchoMax) {
            limpio = limpio.substring(0, limpio.length() - 1);
        }
        return limpio + "...";
    }

    private String limpiar(String texto) {
        if (texto == null) return "";

        StringBuilder resultado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if ((c >= 32 && c <= 126) || (c >= 161 && c <= 255)) {
                resultado.append(c);
            } else if (c == '\n' || c == '\r' || c == '\t') {
                resultado.append(' ');
            } else {
                resultado.append('?');
            }
        }
        return resultado.toString();
    }

    private String dinero(BigDecimal valor) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        return "S/ " + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String metodoLegible(String metodo) {
        if (metodo == null) return "-";
        switch (metodo) {
            case "EFECTIVO": return "Efectivo";
            case "CREDITOS": return "Créditos de la app";
            case "TARJETA": return "Tarjeta";
            case "TRANSFERENCIA": return "QR o transferencia";
            default: return metodo;
        }
    }
}