package dte.masteriot.mdp.asteroidconspiracist.activity.modal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.EditText;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class ShelterModal extends Dialog {

    @SuppressLint("ResourceAsColor")
    public ShelterModal(Context context, OnSaveListener listener) {
        super(context);
        setContentView(R.layout.modal_shelter);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        EditText nameInput = findViewById(R.id.shelterNameInput);
        EditText cityInput = findViewById(R.id.cityNameInput);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString();
            String city = cityInput.getText().toString();
            listener.onSave(name, city);
            dismiss();
        });
    }

    public interface OnSaveListener {
        void onSave(String name, String city);
    }

}
