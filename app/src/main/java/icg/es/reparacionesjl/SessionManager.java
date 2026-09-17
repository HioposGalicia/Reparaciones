package icg.es.reparacionesjl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {

    private static final String TAG = "ReparacionesJL";
    private static final String PREFS_NAME = "arreglosjl_config";
    private static final String KEY_USAR_PRE = "usar_pre";

    private static SessionManager instance;

    // Datos de configuración recibidos en Initialize en el XML de Hiopos
    private String token;
    private String license;
    private String shopXml;
    private String parametersXml;

    private String apiKey;
    private String apiBaseUrl;

    // País de la tienda extraído del XML de Shop
    private String paisIso;

    /* Código de reparación activo en este momento
       se pone cuando añadimos la línea al ticket
       se limpia cuando se cobra o se cancela */
    private String codigoReparacionActivo;

    // Contexto necesario para SharedPreferences
    private Context context;

    // Constructor privado, nadie puede crear instancias desde fuera
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public void init(String parametersXml, String shopXml,
                     String token, String license) {
        this.parametersXml = parametersXml;
        this.shopXml = shopXml;
        this.token = token;
        this.license = license;

        // Las claves se eligen según la preferencia pre/pro guardada
        aplicarEntorno();

        // El país lo extraemos del XML de Shop
        this.paisIso = extraerCampoShop(shopXml, "CountryIsoCode");
    }

    /**
     * Aplica las claves de API según si está configurado pre o pro.
     * Se llama en init() y también cuando el usuario cambia el entorno
     * desde la pantalla de configuración.
     */
    public void aplicarEntorno() {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean usarPro = prefs.getBoolean(KEY_USAR_PRE, false); // false = PRE por defecto

        if (usarPro) {
            this.apiKey = BuildConfig.API_KEY_PRO;
            this.apiBaseUrl = BuildConfig.API_URL_PRO;
        } else {
            this.apiKey = BuildConfig.API_KEY_PRE;
            this.apiBaseUrl = BuildConfig.API_URL_PRE;
        }
        Log.d(TAG, "SessionManager - Entorno aplicado: " + (usarPro ? "PRO - " : "PRE - ") + this.apiBaseUrl);
    }

    /**
     * Guarda la preferencia de entorno y aplica las claves inmediatamente.
     */
    public void setUsarPre(boolean usarPre) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_USAR_PRE, usarPre).apply();
        aplicarEntorno();
    }

    /**
     * Devuelve si actualmente está configurado el entorno de preproducción.
     */
    public boolean isUsarPre() {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USAR_PRE, false);
    }

    // Extrae un campo del XML de Shop
    private String extraerCampoShop(String xml, String key) {
        if (xml == null) return null;
        try {
            String search = "Key=\"" + key + "\">";
            int start = xml.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = xml.indexOf("</ShopField>", start);
            if (end == -1) return null;
            return xml.substring(start, end).trim();
        } catch (Exception e) {
            return null;
        }
    }

    // Getters
    public String getToken() { return token; }
    public String getLicense() { return license; }
    public String getApiKey() { return apiKey; }
    public String getApiBaseUrl() { return apiBaseUrl; }
    public String getPaisIso() { return paisIso; }
    public String getCodigoReparacionActivo() { return codigoReparacionActivo; }

    // Gestión del estado de reparación activa
    public void setCodigoReparacionActivo(String codigo) {
        this.codigoReparacionActivo = codigo;
    }

    public void limpiarReparacionActiva() {
        this.codigoReparacionActivo = null;
    }

    public boolean hayReparacionActiva() {
        return codigoReparacionActivo != null;
    }
}
