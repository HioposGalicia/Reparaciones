package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FinalizeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();

        SessionManager.getInstance().limpiarReparacionActiva();

        Intent resultIntent = new Intent(action);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}