package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Configureactivity extends Activity {

    private static final String TAG = "ReparacionesJL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        String action = getIntent().getAction();
        boolean isReadOnly = getIntent().getBooleanExtra("isReadOnly", false);
        String shopId = getIntent().getStringExtra("ShopId");
        String posId = getIntent().getStringExtra("PosId");

        Log.d(TAG, "Configure - ShopId: " + shopId + " PosId: " + posId + " isReadOnly: " + isReadOnly);

        // Aseguramos que el SessionManager tiene contexto
        SessionManager.getInstance().setContext(this);

        RadioGroup radioGroupEntorno = findViewById(R.id.radioGroupEntorno);
        RadioButton radioPre = findViewById(R.id.radioPre);
        RadioButton radioPro = findViewById(R.id.radioPro);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnProbarConexion = findViewById(R.id.btnProbarConexion);
        TextView tvResultadoConexion = findViewById(R.id.tvResultadoConexion);

        // Cargar preferencia actual
        boolean usarPro = SessionManager.getInstance().isUsarPre();
        if (usarPro) {
            radioPro.setChecked(true);
        } else {
            radioPre.setChecked(true);
        }

        // Si es solo lectura, deshabilitar controles
        radioGroupEntorno.setEnabled(!isReadOnly);
        radioPre.setEnabled(!isReadOnly);
        radioPro.setEnabled(!isReadOnly);
        btnGuardar.setEnabled(!isReadOnly);
        btnProbarConexion.setEnabled(!isReadOnly);

        TextView tvCerrar = findViewById(R.id.tvCerrar);
        tvCerrar.setOnClickListener(v -> {
            Intent resultIntent = new Intent(action);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnProbarConexion.setOnClickListener(v -> {
            tvResultadoConexion.setVisibility(View.VISIBLE);
            tvResultadoConexion.setTextColor(0xFF999999);
            tvResultadoConexion.setText("Probando conexión...");

            boolean seleccionadoPro = radioPro.isChecked();
            String urlAProbar = seleccionadoPro ? BuildConfig.API_URL_PRO : BuildConfig.API_URL_PRE;
            String keyAProbar = seleccionadoPro ? BuildConfig.API_KEY_PRO : BuildConfig.API_KEY_PRE;

            new Thread(() -> {
                boolean conectado = false;
                try {
                    java.net.URL url = new java.net.URL(urlAProbar + "/api/v1/reparacion/ticket/TEST");
                    javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + keyAProbar);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    int responseCode = conn.getResponseCode();
                    conectado = responseCode > 0;
                    conn.disconnect();
                } catch (Exception e) {
                    conectado = false;
                }

                boolean resultado = conectado;
                runOnUiThread(() -> {
                    tvResultadoConexion.setVisibility(View.VISIBLE);
                    if (resultado) {
                        tvResultadoConexion.setText("✓ Conexión correcta");
                        tvResultadoConexion.setTextColor(0xFF4CAF50);
                    } else {
                        tvResultadoConexion.setText("✗ Sin conexión");
                        tvResultadoConexion.setTextColor(0xFFE53935);
                    }
                });
            }).start();
        });

        btnGuardar.setOnClickListener(v -> {
            boolean seleccionadoPro = radioPro.isChecked();
            SessionManager.getInstance().setUsarPre(seleccionadoPro);
            Log.d(TAG, "Configure - Entorno guardado: " + (seleccionadoPro ? "PRO" : "PRE"));

            Intent resultIntent = new Intent(action);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}

