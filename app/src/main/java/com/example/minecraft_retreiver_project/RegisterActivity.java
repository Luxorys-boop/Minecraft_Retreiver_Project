package com.example.minecraft_retreiver_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    private Button buttonReg;
    private DBHandler db;
    private EditText etEmail;

    private EditText pseudoField;

    private EditText etPass;

    private EditText etPass2;

    /**
     * Création de l'activité de création de compte utilisateur. Initialisation des vues et de la BDD.
     * Mise en place de listener sur les vues nécessaires afin de tester l'email et mot de passe utilisateur.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        db = new DBHandler(this);
        buttonReg = findViewById(R.id.buttonReg);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.registerPassword);
        etPass2 = findViewById(R.id.confirmPassword);
        pseudoField = findViewById(R.id.pseudoField);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Toast.LENGTH_SHORT;
                String email = etEmail.getText().toString();
                String password = etPass.getText().toString();
                String password2 = etPass2.getText().toString();
                String pseudo = pseudoField.getText().toString();

                if (!email.contains("@")) {
                    Toast.makeText(RegisterActivity.this, "Email invalide", duration).show();
                }

                else if (!password.equals(password2)) {
                    Toast.makeText(RegisterActivity.this, "Les mots de passe ne correspondent pas", duration).show();
                }

                else if (email.isEmpty() || password.isEmpty() || pseudo.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Veuillez remplir tous les champs", duration).show();
                }
                else {

                    if (db.registerUser(email, password, pseudo)) {
                        Toast.makeText(RegisterActivity.this, "Enregistré avec succès", duration).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Erreur lors de l'enregistrement", duration).show();
                    }
                }

            }
        });
    }


}
