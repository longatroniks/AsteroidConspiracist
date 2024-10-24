// Parts of the code of this example app have ben taken from:
// https://enoent.fr/posts/recyclerview-basics/
// https://developer.android.com/guide/topics/ui/layout/recyclerview

package dte.masteriot.mdp.asteroidconspiracist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Botón de flecha para cambiar de actividad
        Button arrowButton = findViewById(R.id.arrow_button);

        // Cargar animación de parpadeo
        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);
        arrowButton.startAnimation(blinkAnimation);

        // Listener para cambiar de actividad cuando se presione el botón
        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la siguiente actividad
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
    }
}
