package icg.es.reparacionesjl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlBuilder {

    private static final String BARCODE_REPARACION = "REPJL001";
    private static final int ID_PAIS_MEXICO = 144;
    private static final int MAX_CHARS_LINEA = 26;

    // Mapeo código SAT → barcode de artículo Hiopos
    private static final Map<String, String> SAT_A_BARCODE = new HashMap<>();
    static {
        SAT_A_BARCODE.put("73121601", "REPJL002"); // ACHICAR
        SAT_A_BARCODE.put("73121508", "REPJL003"); // AUMENTAR, ENDEREZAR, HACER PASADOR
        SAT_A_BARCODE.put("73121507", "REPJL004"); // BORRAR, ENGASTAR, PULIR, RODINAR
        SAT_A_BARCODE.put("73181010", "REPJL007"); // GRABAR
        SAT_A_BARCODE.put("73141602", "REPJL008"); // HILAR
        SAT_A_BARCODE.put("73181908", "REPJL009"); // PEGAR, SOLDAR
        SAT_A_BARCODE.put("54101600", "REPJL014"); // HACER ESPIGO
        SAT_A_BARCODE.put("54101500", "REPJL015"); // CAMBIAR CIERRE
    }

    public static String buildModifyDocumentResult(
            ReparacionResponse.Reparacion reparacion, String paisIso, String saleId) {

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<ModifyDocumentResult>");

        // Campo personalizado visible en HiOffice
        xml.append("<CustomDocHeaderFields>");
        xml.append("<CustomDocHeaderField Key=\"SaleId\">")
                .append(saleId != null ? saleId : "")
                .append("</CustomDocHeaderField>");
        xml.append("<CustomDocHeaderField Key=\"CodReparacion\">")
                .append(reparacion.ticket_reparacion)
                .append("</CustomDocHeaderField>");
        xml.append("</CustomDocHeaderFields>");

        xml.append("<NewLines>");

        if ("MX".equals(paisIso) || "MEX".equals(paisIso)) {
            // México: una NewLine por cada trabajo con su barcode correspondiente
            xml.append(buildLineasMexico(reparacion));
        } else {
            // España / Portugal: una sola línea REPJL001 con comentarios simplificados
            xml.append(buildLineaEspana(reparacion));
        }

        xml.append("</NewLines>");
        xml.append("</ModifyDocumentResult>");
        return xml.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // ESPAÑA / PORTUGAL
    // ─────────────────────────────────────────────────────────────

    private static String buildLineaEspana(ReparacionResponse.Reparacion reparacion) {
        StringBuilder sb = new StringBuilder();
        sb.append("<NewLine>");
        sb.append("<NewLineField Key=\"Barcode\">REPJL001</NewLineField>");
        sb.append("<NewLineField Key=\"Units\">1</NewLineField>");
        sb.append("<NewLineField Key=\"Price\">").append(formatImporte(reparacion.importe.replace(",", "."))).append("</NewLineField>");
        sb.append("<NewLineField Key=\"Description\">").append(reparacion.ticket_reparacion).append("</NewLineField>");

        sb.append("<Modifiers>");

        if (reparacion.trabajos != null) {
            for (ReparacionResponse.Trabajo trabajo : reparacion.trabajos) {

                // Descripción del servicio (campo descripcion de trabajo_tipo)
                if (trabajo.trabajo_tipo != null && trabajo.trabajo_tipo.descripcion != null) {
                    sb.append(buildComentarioMultilinea(trabajo.trabajo_tipo.descripcion, MAX_CHARS_LINEA));
                }

                // Metal si hay
                if (trabajo.metal != null && !trabajo.metal.equals("0.0000")) {
                    sb.append("<Modifier>");
                    sb.append("<NewLineField Key=\"IsComment\">true</NewLineField>");
                    sb.append("<NewLineField Key=\"Description\">Metal: ").append(trabajo.metal).append(" g</NewLineField>");
                    sb.append("</Modifier>");
                }
            }
        }

        // Garantía al final con código de reparación
        sb.append(buildComentarioMultilinea(
                "Garantía por reparación de 90 días, presentando el ticket. ",
                MAX_CHARS_LINEA));

        // Línea en blanco separadora
        sb.append("<Modifier>");
        sb.append("<NewLineField Key=\"IsComment\">true</NewLineField>");
        sb.append("<NewLineField Key=\"Description\"> </NewLineField>");
        sb.append("</Modifier>");

        sb.append(buildComentarioMultilinea(
                "Su código de reparación: " + reparacion.ticket_reparacion,
                MAX_CHARS_LINEA));

        sb.append("</Modifiers>");
        sb.append("</NewLine>");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // MÉXICO
    // ─────────────────────────────────────────────────────────────

    private static String buildLineasMexico(ReparacionResponse.Reparacion reparacion) {
        StringBuilder sb = new StringBuilder();

        if (reparacion.trabajos == null || reparacion.trabajos.isEmpty()) {
            // Fallback: si no hay trabajos, línea genérica
            sb.append("<NewLine>");
            sb.append("<NewLineField Key=\"Barcode\">REPJL001</NewLineField>");
            sb.append("<NewLineField Key=\"Units\">1</NewLineField>");
            sb.append("<NewLineField Key=\"Price\">").append(reparacion.importe).append("</NewLineField>");
            sb.append("<NewLineField Key=\"Description\">").append(reparacion.ticket_reparacion).append("</NewLineField>");
            sb.append("</NewLine>");
            return sb.toString();
        }

        for (int i = 0; i < reparacion.trabajos.size(); i++) {
            boolean esUltimoTrabajo = (i == reparacion.trabajos.size() - 1);
            ReparacionResponse.Trabajo trabajo = reparacion.trabajos.get(i);

            // Obtener código SAT y barcode correspondiente
            String codigoSat = obtenerCodigoSat(trabajo);
            String barcode = codigoSat != null ? SAT_A_BARCODE.get(codigoSat) : null;
            if (barcode == null) barcode = BARCODE_REPARACION; // fallback a REPJL001

            // Nombre del artículo: identificador del trabajo_tipo
            String nombreArticulo = (trabajo.trabajo_tipo != null && trabajo.trabajo_tipo.identificador != null)
                    ? trabajo.trabajo_tipo.identificador
                    : reparacion.ticket_reparacion;

            sb.append("<NewLine>");
            sb.append("<NewLineField Key=\"Barcode\">").append(barcode).append("</NewLineField>");
            sb.append("<NewLineField Key=\"Units\">1</NewLineField>");
            sb.append("<NewLineField Key=\"Price\">").append(trabajo.importe).append("</NewLineField>");
            sb.append("<NewLineField Key=\"Description\">").append(nombreArticulo).append("</NewLineField>");

            sb.append("<Modifiers>");

            // Descripción larga del SAT como comentario
            if (trabajo.trabajo_tipo != null && trabajo.trabajo_tipo.codigos_x_pais != null) {
                for (ReparacionResponse.CodigoPais cp : trabajo.trabajo_tipo.codigos_x_pais) {
                    if (cp.id_pais == ID_PAIS_MEXICO) {
                        sb.append(buildComentarioMultilinea(cp.descripcion, MAX_CHARS_LINEA));
                        break;
                    }
                }
            }

            // Metal si hay
            if (trabajo.metal != null && !trabajo.metal.equals("0.0000")) {
                sb.append("<Modifier>");
                sb.append("<NewLineField Key=\"IsComment\">true</NewLineField>");
                sb.append("<NewLineField Key=\"Description\">Metal: ").append(trabajo.metal).append(" g</NewLineField>");
                sb.append("</Modifier>");
            }

            // Garantía solo en el último trabajo con código de reparación
            if (esUltimoTrabajo) {
                sb.append(buildComentarioMultilinea(
                        "Garantía por reparación de 90 días, presentando el ticket. ",
                        MAX_CHARS_LINEA));
            }
            // Línea en blanco separadora
            sb.append("<Modifier>");
            sb.append("<NewLineField Key=\"IsComment\">true</NewLineField>");
            sb.append("<NewLineField Key=\"Description\"> </NewLineField>");
            sb.append("</Modifier>");

            sb.append(buildComentarioMultilinea(
                    "Su código de reparación: " + reparacion.ticket_reparacion,
                    MAX_CHARS_LINEA));

            sb.append("</Modifiers>");
            sb.append("</NewLine>");
        }

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────

    /**
     * Normaliza el importe a formato con coma decimal (ej: 176,00)
     */
    private static String formatImporte(String importe) {
        if (importe == null) return "0,00";
        return importe.replace(".", ",");
    }

    /**
     * Obtiene el código SAT del trabajo para México (id_pais = 144)
     */
    private static String obtenerCodigoSat(ReparacionResponse.Trabajo trabajo) {
        if (trabajo.trabajo_tipo == null
                || trabajo.trabajo_tipo.codigos_x_pais == null
                || trabajo.trabajo_tipo.codigos_x_pais.isEmpty()) {
            return null;
        }
        for (ReparacionResponse.CodigoPais codigo : trabajo.trabajo_tipo.codigos_x_pais) {
            if (codigo.id_pais == ID_PAIS_MEXICO) {
                return codigo.codigo;
            }
        }
        return null;
    }

    /**
     * Divide un texto en múltiples Modifier de comentario,
     * cortando siempre por palabras completas sin superar maxChars por línea.
     */
    private static String buildComentarioMultilinea(String texto, int maxChars) {
        StringBuilder sb = new StringBuilder();
        List<String> lineas = splitTexto(texto, maxChars);
        for (String linea : lineas) {
            sb.append("<Modifier>");
            sb.append("<NewLineField Key=\"IsComment\">true</NewLineField>");
            sb.append("<NewLineField Key=\"Description\">").append(linea).append("</NewLineField>");
            sb.append("</Modifier>");
        }
        return sb.toString();
    }

    /**
     * Divide un texto en líneas que no superen maxChars sin cortar palabras.
     */
    private static List<String> splitTexto(String texto, int maxChars) {
        List<String> lineas = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return lineas;

        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            if (lineaActual.length() == 0) {
                lineaActual.append(palabra);
            } else if (lineaActual.length() + 1 + palabra.length() <= maxChars) {
                lineaActual.append(" ").append(palabra);
            } else {
                lineas.add(lineaActual.toString());
                lineaActual = new StringBuilder(palabra);
            }
        }

        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }

        return lineas;
    }
}