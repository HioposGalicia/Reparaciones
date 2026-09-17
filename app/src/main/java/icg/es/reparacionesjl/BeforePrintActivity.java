package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BeforePrintActivity extends Activity {

    private static final String TAG = "ReparacionesJL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String actionRecibida = getIntent().getAction();
        Log.d("ReparacionesJL", "BeforePrint - Action recibida: " + actionRecibida);

        String codigoReparacion = SessionManager.getInstance()
                .getCodigoReparacionActivo();

        if (codigoReparacion == null) {
            Log.d(TAG, "BeforePrint - Sin reparación activa, continuamos");
            setResult(RESULT_OK);
            finish();
            return;
        }

        Log.d(TAG, "BeforePrint - Procesando PATCH para: " + codigoReparacion);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean exito = HttpClient.patchReparacionEntregada(codigoReparacion);

            handler.post(() -> {
                if (exito) {
                    Log.d(TAG, "BeforePrint - PATCH exitoso");
                } else {
                    Log.e(TAG, "BeforePrint - PATCH fallido");
                }
                SessionManager.getInstance().limpiarReparacionActiva();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}