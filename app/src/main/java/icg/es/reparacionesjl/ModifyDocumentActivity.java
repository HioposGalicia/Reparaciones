package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModifyDocumentActivity extends Activity {

    private static final String TAG = "ReparacionesJL";
    private static final int REQUEST_REPARACION = 1001;

    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String actionRecibida = getIntent().getAction();
        Log.d(TAG, "ModifyDocument - Action recibida: " + actionRecibida);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "ModifyDocument extra: " + key + " = " + extras.get(key));
            }
        } else {
            Log.d(TAG, "ModifyDocument - Sin extras");
        }

        // Leer el XML completo del documento desde el Uri
        String documentUri = getIntent().getStringExtra("DocumentUri");
        if (documentUri != null) {
            try {
                Uri uri = Uri.parse(documentUri);
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder xmlCompleto = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    xmlCompleto.append(line);
                }
                reader.close();
                Log.d(TAG, "Document XML completo: " + xmlCompleto.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error leyendo DocumentUri: " + e.getMessage());
            }
        }

        String triggerEvent = getIntent().getStringExtra("TriggerEvent");
        Log.d(TAG, "ModifyDocument - TriggerEvent: " + triggerEvent);

        if ("PRINT".equals(triggerEvent)) {
            action = getIntent().getAction();

            String codigoReparacion = SessionManager.getInstance().getCodigoReparacionActivo();
            String documentoXml = getIntent().getStringExtra("Document");

            if (codigoReparacion != null) {
                if (documentoXml != null && documentoXml.contains("REPJL")) {
                    Log.d(TAG, "ModifyDocument - PRINT detected, haciendo PATCH para: " + codigoReparacion);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        boolean exito = HttpClient.patchReparacionEntregada(codigoReparacion);
                        Log.d(TAG, "ModifyDocument - PATCH resultado: " + exito);
                        SessionManager.getInstance().limpiarReparacionActiva();
                    });
                } else {
                    Log.d(TAG, "ModifyDocument - PRINT detected pero REPJL no está en el documento, cancelando PATCH");
                    SessionManager.getInstance().limpiarReparacionActiva();
                }
            } else {
                Log.d(TAG, "ModifyDocument - PRINT detected pero sin reparación activa");
            }

            String xmlRespuesta = "<?xml version=\"1.0\" encoding=\"utf-8\"?><ModifyDocumentResult><NewLines></NewLines></ModifyDocumentResult>";
            devolverResultado(xmlRespuesta);
            return;
        }

        action = getIntent().getAction();

        // Lanzamos la pantalla de búsqueda de reparación
        Intent intent = new Intent(this, ReparacionActivity.class);
        startActivityForResult(intent, REQUEST_REPARACION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_REPARACION) return;

        if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "ModifyDocument - Cancelado por el usuario");
            String xmlVacio = "<?xml version=\"1.0\" encoding=\"utf-8\"?><ModifyDocumentResult><NewLines></NewLines></ModifyDocumentResult>";
            devolverResultado(xmlVacio);
            return;
        }

        if (resultCode == RESULT_OK && data != null) {
            String codigoReparacion = data.getStringExtra("ticket_reparacion");
            String importe = data.getStringExtra("importe");

            Log.d(TAG, "ModifyDocument - Reparación confirmada: " + codigoReparacion);

            SessionManager.getInstance().setCodigoReparacionActivo(codigoReparacion);

            String paisIso = SessionManager.getInstance().getPaisIso();
            String documentXml = getIntent().getStringExtra("Document");

            try {
                buscarYResponder(codigoReparacion, paisIso, documentXml);
            } catch (Exception e) {
                Log.e(TAG, "Error en ModifyDocument: " + e.getMessage());
                devolverError(e.getMessage());
            }
        }
    }

    private String extraerSaleId(String documentXml) {
        if (documentXml == null) return null;
        try {
            String search = "Key=\"SaleId\">";
            int start = documentXml.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = documentXml.indexOf("</HeaderField>", start);
            if (end == -1) return null;
            return documentXml.substring(start, end).trim();
        } catch (Exception e) {
            return null;
        }
    }

    private void buscarYResponder(String codigoReparacion, String paisIso, String documentXml) {
        new Thread(() -> {
            ReparacionResponse response = HttpClient.getReparacion(codigoReparacion);

            runOnUiThread(() -> {
                if (response == null || !response.isOk()) {
                    devolverError("Error al obtener datos de la reparación");
                    return;
                }

                try {
                    String xml = XmlBuilder.buildModifyDocumentResult(
                            response.getReparacion(), paisIso, extraerSaleId(documentXml));

                    devolverResultado(xml);

                } catch (Exception e) {
                    Log.e(TAG, "Error construyendo XML: " + e.getMessage());
                    devolverError(e.getMessage());
                }
            });
        }).start();
    }

    private void devolverResultado(String xml) {
        Log.d(TAG, "XML generado: " + xml);
        try {
            File directory = new File(getFilesDir(), "ApiTraffic");
            if (!directory.exists()) directory.mkdirs();

            File file = new File(directory, "modify_result.xml");
            if (file.exists()) file.delete();
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(xml.getBytes("UTF-8"));
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file);

            grantUriPermission(
                    "icg.android.start",
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent resultIntent = new Intent(action);
            resultIntent.putExtra("ModifyDocumentResultUri", uri);
            setResult(RESULT_OK, resultIntent);

            Log.d(TAG, "ModifyDocument - Resultado enviado a Hiopos");

        } catch (Exception e) {
            Log.e(TAG, "Error escribiendo fichero: " + e.getMessage());
            devolverError(e.getMessage());
        }

        finish();
    }

    private void devolverError(String mensaje) {
        Intent resultIntent = new Intent(action);
        resultIntent.putExtra("ErrorMessage", mensaje);
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}