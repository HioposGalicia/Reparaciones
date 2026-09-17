package icg.es.reparacionesjl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReparacionActivity extends Activity {

    private EditText etCodigo;
    private Button btnBuscar;
    private Button btnConfirmar;
    private Button btnCancelar;
    private Button btnCerrar;
    private LinearLayout panelResultado;
    private TextView tvReferencia;
    private TextView tvEstado;
    private TextView tvImporte;
    private TextView tvError;

    private ReparacionResponse.Reparacion reparacionEncontrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reparacion);

        etCodigo       = findViewById(R.id.etCodigo);
        btnBuscar      = findViewById(R.id.btnBuscar);
        btnConfirmar   = findViewById(R.id.btnConfirmar);
        btnCancelar    = findViewById(R.id.btnCancelar);
        btnCerrar      = findViewById(R.id.btnCerrar);
        panelResultado = findViewById(R.id.panelResultado);
        tvReferencia   = findViewById(R.id.tvReferencia);
        tvEstado       = findViewById(R.id.tvEstado);
        tvImporte      = findViewById(R.id.tvImporte);
        tvError        = findViewById(R.id.tvError);

        etCodigo.setOnEditorActionListener((v, actionId, event) -> {
            buscarReparacion();
            etCodigo.setText("");
            return true;
        });

        btnBuscar.setOnClickListener(v -> buscarReparacion());
        btnConfirmar.setOnClickListener(v -> confirmar());
        btnCancelar.setOnClickListener(v -> cancelar());
        btnCerrar.setOnClickListener(v -> cancelar());
    }

    private void buscarReparacion() {
        String codigo = etCodigo.getText().toString().trim();

        if (codigo.isEmpty()) {
            mostrarError("Introduce un código de resguardo");
            return;
        }

        etCodigo.setText("");

        panelResultado.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnBuscar.setEnabled(false);
        btnBuscar.setText("BUSCANDO...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ReparacionResponse response = HttpClient.getReparacion(codigo);

            handler.post(() -> {
                btnBuscar.setEnabled(true);
                btnBuscar.setText("BUSCAR");

                // Sin conexión
                if (response == null) {
                    mostrarError("Sin conexión con el servidor");
                    return;
                }

                // Error de la API (400, 404, etc.)
                if (!response.isOk()) {
                    String mensajeError = "Error en la reparación";
                    if (response.data != null &&
                            response.data.errors != null &&
                            response.data.errors.message != null) {
                        mensajeError = response.data.errors.message;
                    }
                    mostrarError(mensajeError);
                    return;
                }

                // Reparación no encontrada en la respuesta
                if (response.getReparacion() == null) {
                    mostrarError("Reparación no encontrada");
                    return;
                }

                // Validamos estado - si ya está entregada no permitimos continuar
                if (response.getReparacion().estado != null &&
                        response.getReparacion().estado.identificador != null &&
                        response.getReparacion().estado.identificador
                                .equals("ENTREGADA_A_CLIENTE")) {
                    mostrarError("Esta reparación ya ha sido entregada al cliente");
                    return;
                }

                reparacionEncontrada = response.getReparacion();
                mostrarResultado(reparacionEncontrada);
            });
        });
    }

    private void mostrarResultado(ReparacionResponse.Reparacion reparacion) {
        tvReferencia.setText("Ref: " + reparacion.ticket_reparacion);
        tvEstado.setText("Estado: " + reparacion.estado.nombre);
        tvImporte.setText("Importe: " + reparacion.importe);
        panelResultado.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
        panelResultado.setVisibility(View.GONE);
        reparacionEncontrada = null;
    }

    private void confirmar() {
        if (reparacionEncontrada == null) return;

        Intent resultado = new Intent();
        resultado.putExtra("ticket_reparacion",
                reparacionEncontrada.ticket_reparacion);
        resultado.putExtra("importe",
                reparacionEncontrada.importe);
        setResult(RESULT_OK, resultado);
        finish();
    }

    private void cancelar() {
        setResult(RESULT_CANCELED);
        finish();
    }
}