package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BehaviorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        Intent resultIntent = new Intent(action);

        resultIntent.putExtra("CanShowAsSaleHeaderOption", true);

        resultIntent.putExtra("CallBeforePrint", true);

        /* resultIntent.putExtra("CallBeforeTotalizePurchase", false);
        resultIntent.putExtra("CallBeforeTotalizeOrder", false);
        resultIntent.putExtra("CallOnTotalizationCanceled", false);
        resultIntent.putExtra("CallBeforeTotalizeSale", false);
        resultIntent.putExtra("CallBeforeSetOnHold", false);
        resultIntent.putExtra("CallAfterCreate", false);
        resultIntent.putExtra("CallOnDocumentCanceled", false);
        resultIntent.putExtra("CallVoidDocument", false);
        resultIntent.putExtra("CallOnSubtotal", false);
        resultIntent.putExtra("CallOnCashCount", false); */

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}