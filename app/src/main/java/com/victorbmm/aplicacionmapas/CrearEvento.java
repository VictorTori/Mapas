package com.victorbmm.aplicacionmapas;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.text.ParseException;

public class CrearEvento extends Activity implements MapboxMap.OnMapClickListener, View.OnClickListener {

    MapView mapaView;
    MapboxMap mapa;
    Button btAceptar;
    Button btRestablecer;
    EditText etNombre;
    EditText etDescripcion;
    EditText etFecha;
    MarkerOptions marcador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, "pk.eyJ1IjoidmljdG9ydG9yaSIsImEiOiJjanNjNW81OWswaWZ5NDRxY2Z4OXN6em51In0.an5c1dHRSg6bHkSUvQhusw");

        setContentView(R.layout.activity_crear_evento);

        btAceptar = findViewById(R.id.btAceptar);
        btRestablecer = findViewById(R.id.btRestablecer);
        btAceptar.setOnClickListener(this);
        btRestablecer.setOnClickListener(this);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);

        mapaView = findViewById(R.id.mapaView);
        mapaView.onCreate(savedInstanceState);

        mapaView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapa = mapboxMap;
                anadirListeners();
            }
        });
    }
    private void anadirListeners() {
        mapa.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        mapa.clear();
        marcador = new MarkerOptions().setPosition(point);
        mapa.addMarker(marcador);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btAceptar){

            Evento evento = new Evento();
            evento.setNombre(etNombre.getText().toString());
            evento.setDescripcion(etDescripcion.getText().toString());
            try {
                evento.setFecha(Util.parsearFecha(etFecha.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            evento.setLatitud(marcador.getPosition().getLatitude());
            evento.setLongitud(marcador.getPosition().getLongitude());


            SubirDatos descargaDatos = new SubirDatos();
            descargaDatos.execute(evento.getNombre(),evento.getDescripcion(),Util.formatearFecha(evento.getFecha()),
                    Double.toString(evento.getLongitud()), Double.toString(evento.getLatitud()));

            Toast.makeText(this,"Ha sido guardado",Toast.LENGTH_SHORT).show();

            limpiarTodo();

        } else {

            limpiarTodo();

        }
    }

    private void limpiarTodo() {
        etNombre.setText("");
        etDescripcion.setText("");
        etFecha.setText("");
        mapa.clear();
    }

    class SubirDatos extends AsyncTask<String, Integer, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.getForObject("http://10.0.2.2:8082" + "/nuevoevento?nombre=" + strings[0] + "&descripcion=" + strings[1] +
                    "&fecha=" + strings[2] + "&longitud=" + strings[3] + "&latitud=" + strings[4], Void.class);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }
}
