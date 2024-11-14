package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Botón para ListActivity
        Button arrowButton = findViewById(R.id.arrow_button);
        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);
        arrowButton.startAnimation(blinkAnimation);
        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

    }

}
