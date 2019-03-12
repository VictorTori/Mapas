package com.victorbmm.aplicacionmapas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v4.DirectionsCriteria;
import com.mapbox.services.directions.v4.MapboxDirections;
import com.mapbox.services.directions.v4.models.DirectionsResponse;
import com.mapbox.services.directions.v4.models.DirectionsRoute;
import com.mapbox.services.directions.v4.models.Waypoint;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Mapa extends AppCompatActivity implements MapboxMap.OnMarkerClickListener, View.OnClickListener, MapboxMap.OnMapClickListener {

    MapView mapaView;
    MapboxMap mapa;
    FloatingActionButton ubicacion;
    FloatingActionButton recorrido;
    LocationServices servicioUbicacion;
    Marker marker;
    List<Evento> listaEventos;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_nuevo_contacto){
            Intent intent = new Intent(this, CrearEvento.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, "pk.eyJ1IjoidmljdG9ydG9yaSIsImEiOiJjanNjNW81OWswaWZ5NDRxY2Z4OXN6em51In0.an5c1dHRSg6bHkSUvQhusw");

        setContentView(R.layout.activity_mapa);

        mapaView = findViewById(R.id.mapaView);
        mapaView.onCreate(savedInstanceState);

        mapaView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapa = mapboxMap;
                anadirListeners();
                Intent intent = getIntent();
                double latitud = intent.getDoubleExtra("latitud",0);
                double longitud = intent.getDoubleExtra("longitud",0);
                System.out.println(latitud);
                if (latitud != 0){
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(latitud,longitud))
                            .zoom(17)
                            .tilt(30)
                            .build();

                    mapa.animateCamera(CameraUpdateFactory.newCameraPosition(position),4000);
                }
                mapa.addMarker(new MarkerOptions().setTitle("voidMuyTofu").position(new LatLng(40.413700,-3.696685)).snippet("Vas a ir muy tofu"));
            }
        });

        CargarDatos cargarDatos = new CargarDatos();
        cargarDatos.execute();
        ubicacion = findViewById(R.id.btUbicacion);
        recorrido = findViewById(R.id.btRecorrido);

        ubicacion.setOnClickListener(this);
        recorrido.setOnClickListener(this);

        recorrido.setVisibility(View.INVISIBLE);

        ubicarUsuario();

    }

    private void ubicarUsuario() {

        servicioUbicacion = LocationServices.getLocationServices(this);

    }

    private void anadirListeners() {
        mapa.setOnMarkerClickListener(this);
        mapa.setOnMapClickListener(this);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        recorrido.setVisibility(View.VISIBLE);
        this.marker = marker;
        return false;
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
            for(Evento evento : listaEventos){
                mapa.addMarker(new MarkerOptions().setTitle(evento.getNombre()).position(new LatLng(evento.getLatitud(),evento.getLongitud())).snippet(evento.getDescripcion() + "\n" + evento.getFecha()));
            }
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

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btUbicacion){
            Location lastLocation = servicioUbicacion.getLastLocation();
            if(lastLocation != null)
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 15));
            mapa.setMyLocationEnabled(true);
        }

        if(view.getId() == R.id.btRecorrido){

            Waypoint posMarker = new Waypoint(marker.getPosition().getLongitude(),marker.getPosition().getLatitude());
            Waypoint posUsuario = new Waypoint(servicioUbicacion.getLastLocation().getLongitude(),servicioUbicacion.getLastLocation().getLatitude());

            MapboxDirections direccion = null;

            try {
                direccion = new MapboxDirections.Builder()
                        .setOrigin(posMarker)
                        .setDestination(posUsuario)
                        .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                        .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                        .build();
            } catch (ServicesException e) {
                e.printStackTrace();
            }

            direccion.enqueueCall(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                    DirectionsRoute ruta = response.body().getRoutes().get(0);
                    Toast.makeText(Mapa.this, "Distancia: " + ruta.getDistance() + " metros", Toast.LENGTH_SHORT).show();

                    pintarRuta(ruta);
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                    // Qué hacer en caso de que falle el cálculo de la ruta
                }
            });

        }

    }

    private void pintarRuta(DirectionsRoute ruta) {
        LineString lineString = LineString.fromPolyline(ruta.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordenadas = lineString.getCoordinates();
        LatLng[] puntos = new LatLng[coordenadas.size()];
        for (int i = 0; i < coordenadas.size(); i++) {
            puntos[i] = new LatLng(coordenadas.get(i).getLatitude(), coordenadas.get(i).getLongitude());
        }

        // Pinta los puntos en el mapa
        mapa.addPolyline(new PolylineOptions()
                .add(puntos)
                .color(Color.BLACK)
                .width(5));

        // Resalta la posición del usuario si no lo estaba ya
        if (!mapa.isMyLocationEnabled())
            mapa.setMyLocationEnabled(true);
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        recorrido.setVisibility(View.INVISIBLE);
    }
}
