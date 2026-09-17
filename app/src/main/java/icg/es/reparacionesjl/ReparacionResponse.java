package icg.es.reparacionesjl;

import java.util.List;

public class ReparacionResponse {

    public String status;
    public String code;
    public String message;
    public Data data;

    public static class Reparacion {
        public int id;
        public String ticket_reparacion;
        public String importe;
        public boolean en_garantia;
        public List<Trabajo> trabajos;
        public Estado estado;
    }

    public static class Trabajo {
        public int id;
        public String descripcion;
        public Integer unidades;
        public String importe;
        public String metal;
        public TrabajTipo trabajo_tipo;
    }

    public static class TrabajTipo {
        public int id;
        public String identificador;
        public String descripcion;
        public List<CodigoPais> codigos_x_pais;
    }

    public static class CodigoPais {
        public int id_pais;
        public String codigo;
        public String descripcion;
    }

    public static class Estado {
        public int id;
        public String identificador;
        public String nombre;
    }

    public static class Errors {
        public String message;
    }

    public static class Data {
        public Errors errors;
        public Reparacion reparacion;
    }

    // Helpers
    public boolean isOk() {
        return "ok".equals(status) && data != null && data.reparacion != null;
    }

    public Reparacion getReparacion() {
        return data != null ? data.reparacion : null;
    }
}