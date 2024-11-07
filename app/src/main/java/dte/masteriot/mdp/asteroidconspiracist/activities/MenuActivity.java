package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inicializar los botones
        Button btnCompass = findViewById(R.id.btn_compass);
        Button btnAsteroidList = findViewById(R.id.btn_asteroid_list);
        Button btnMqtt = findViewById(R.id.btn_mqtt);

        // Configurar el listener para el botón Compass
        btnCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CompassActivity.class);
                startActivity(intent);
            }
        });

        // Configurar el listener para el botón Asteroid List
        btnAsteroidList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        // Configurar el listener para el botón MQTT
        btnMqtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
