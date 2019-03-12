package com.victorbmm.aplicacionmapas;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MisEventos extends AppCompatActivity implements View.OnClickListener {

    List<Evento> listaEventos;
    List<String> adapterArrayEventos;
    ListView listEvents;
    ArrayAdapter<String> adapter;

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_nuevo_contacto){
            Intent intent = new Intent(this, CrearEvento.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }

    class CargarDatos extends AsyncTask<String, Integer, Void> {

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(String... strings) {
            listaEventos = new ArrayList<>();

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Evento[] opinionesArray = restTemplate.getForObject("http://10.0.2.2:8082" + "/eventos", Evento[].class);
            listaEventos.addAll(Arrays.asList(opinionesArray));
            for (Evento evento : listaEventos) {
                adapterArrayEventos.add(evento.getNombre());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_eventos);

        listEvents = findViewById(R.id.lvEventos);
        adapterArrayEventos = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, adapterArrayEventos);

        listEvents.setAdapter(adapter);

        CargarDatos cargarDatos = new CargarDatos();
        cargarDatos.execute();

        listEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Evento evento = listaEventos.get(i);
                System.out.println(evento.getLatitud());
                 Intent intent = new Intent(getApplicationContext(),Mapa.class);
                 intent.putExtra("latitud",evento.getLatitud());
                 intent.putExtra("longitud",evento.getLongitud());
                 startActivity(intent);

            }
        });

    }
}
