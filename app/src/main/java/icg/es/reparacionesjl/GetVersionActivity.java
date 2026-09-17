package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GetVersionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();

        Intent resultIntent = new Intent(action);
        resultIntent.putExtra("Version", BuildConfig.VERSION_CODE);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
