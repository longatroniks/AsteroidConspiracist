package dte.masteriot.mdp.asteroidconspiracist.activities.modals;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class ObservationModal extends Dialog {
    public ObservationModal(Context context, OnSaveListener listener) {
        super(context);
        setContentView(R.layout.modal_observation);

        EditText descriptionInput = findViewById(R.id.descriptionInput);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(view -> {
            String description = descriptionInput.getText().toString();
            listener.onSave(description);
            dismiss();
        });
    }

    public interface OnSaveListener {
        void onSave(String description);
    }
}
