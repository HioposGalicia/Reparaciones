package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InitializeActivity extends Activity {

    private static final String TAG = "ReparacionesJL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();

        try {

            String parameters = getIntent().getStringExtra("Parameters");

            String shop = getIntent().getStringExtra("Shop");

            String token = getIntent().getStringExtra("Token");

            String license = getIntent().getStringExtra("License");


            // Guardamos en SessionManager para uso durante toda la sesión
            SessionManager.getInstance().setContext(this);
            SessionManager.getInstance().init(parameters, shop, token, license);
            SessionManager.getInstance().init(parameters, shop, token, license);

            Intent resultIntent = new Intent(action);
            setResult(RESULT_OK, resultIntent);

        } catch (Exception e) {
            Log.e(TAG, "Error en Initialize: " + e.getMessage());
            Intent resultIntent = new Intent(action);
            resultIntent.putExtra("ErrorMessage", e.getMessage());
            setResult(RESULT_CANCELED, resultIntent);
        }

        finish();
    }
}