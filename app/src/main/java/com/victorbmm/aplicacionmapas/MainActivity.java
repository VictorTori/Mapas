package com.victorbmm.aplicacionmapas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btAnadir;
    Button btMapa;
    Button btMisEventos;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_preferencias){
            Intent intent = new Intent(this, MenuPreferencias.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.menu_acerca_de){

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        btAnadir = findViewById(R.id.btAnadir);
        btMapa = findViewById(R.id.btMapa);
        btMisEventos = findViewById(R.id.btMisEventos);

        btAnadir.setOnClickListener(this);
        btMapa.setOnClickListener(this);
        btMisEventos.setOnClickListener(this);

        preferencias();
    }

    private void preferencias() {

        SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        String color = preferencias.getString("color","colores");
        String nombre = preferencias.getString("opcion_nombre", null);
        String tamano = preferencias.getString("tamano", "15");

        this.setTitle(nombre);
        btAnadir.setTextSize(Float.parseFloat(tamano));
        btMapa.setTextSize(Float.parseFloat(tamano));
        btMisEventos.setTextSize(Float.parseFloat(tamano));

        View view = this.getWindow().getDecorView();
        if(color.equals("NGR")){
            view.setBackgroundColor(Color.BLACK);
        } else {
            if(color.equals("BLA")){
                view.setBackgroundColor(Color.WHITE);
            } else {
                view.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;

        switch (view.getId()){

            case R.id.btMapa :
                intent = new Intent(this, Mapa.class);
                startActivity(intent);
                break;
            case R.id.btAnadir:
                intent = new Intent(this, CrearEvento.class);
                startActivity(intent);
                break;
            case R.id.btMisEventos:
                intent = new Intent(this, MisEventos.class);
                startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferencias();
    }
}
