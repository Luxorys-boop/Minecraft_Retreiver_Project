package com.example.minecraft_retreiver_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

    private DBHandler db;
    private Button buttonConn;
    private EditText etEmail;
    private EditText etPass;
    private TextView registerLink;

    /**
     * Création de l'activité de connexion (MainActivity). Initialisation de la BDD et mise en place de listener
     * sur les vues nécessaires. Mise en place d'Intents importants afin d'être regirigé vers une autre activité une fois connecté.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = new DBHandler(this);
        buttonConn = findViewById(R.id.buttonConn);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.connPassword);
        registerLink = findViewById(R.id.registerLink);

        // Créer les données exemple
        db.createExampleUser();
        db.addExampleServer();



        buttonConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Toast.LENGTH_SHORT;
                String email = etEmail.getText().toString();
                String password = etPass.getText().toString();

                if(db.checkUser(email, password)) {
                    Toast.makeText(MainActivity.this, "CONNECTÉ", duration).show();
                    Intent accueilIntent = new Intent(getApplicationContext(), AccueilActivity.class);
                    accueilIntent.putExtra("USER_EMAIL", email);
                    startActivity(accueilIntent);
                } else {
                    if (!email.contains("@")) {
                        Toast.makeText(MainActivity.this, "Email invalide", duration).show();
                    }
                    Toast.makeText(MainActivity.this, "ERREUR RÉESSAYEZ", duration).show();
                }

            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}