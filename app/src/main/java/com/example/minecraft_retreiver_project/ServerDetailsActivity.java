package com.example.minecraft_retreiver_project;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerDetailsActivity extends AppCompatActivity {

    private DBHandler dbHandler;
    private String serverIp;

    /**
     * Création de l'activité, début de son cycle de vie. Initialisation de la BDD et des vues.
     * Vérification de la présence de l'ip serveur (normalement impossible de rentrer dans cette activité sans).
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_details);

        // Initialisation de la BDD
        dbHandler = new DBHandler(this);

        // Initialisation des VUES et récupération des variables passées en Intent. Activité précédente : AccueilActivity
        String motd = getIntent().getStringExtra("MOTD");
        int playersOnline = getIntent().getIntExtra("PLAYERS_ONLINE", 0);
        int maxPlayers = getIntent().getIntExtra("MAX_PLAYERS", 0);
        serverIp = getIntent().getStringExtra("SERVER_IP");

        if (serverIp == null || serverIp.isEmpty()) {
            Toast.makeText(this, "Server IP non enregistrée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ajout du serveur avec ses détails.
        TextView motdTextView = findViewById(R.id.motdTextView);
        TextView playersTextView = findViewById(R.id.playersTextView);
        ImageView serverLogo = findViewById(R.id.serverLogo);
        Button deleteServerButton = findViewById(R.id.deleteServerButton);

        motdTextView.setText(motd);
        playersTextView.setText("Players Online: " + playersOnline + "/" + maxPlayers);

        // Récupération du logo du serveur
        fetchServerIcon(serverIp, serverLogo);

        // Ajout d'un bouton pour supprimer le serveur.
        deleteServerButton.setOnClickListener(v -> deleteServer());
    }

    /**
     * Récupération de l'icone du serveur en réalisant une requête GET à l'api minecraft.
     * @param serverIp L'ip du serveur minecraft voulu en détail.
     * @param serverLogo La vue pour l'insertion du logo.
     */
    private void fetchServerIcon(String serverIp, ImageView serverLogo) {
        String iconUrl = "https://api.mcstatus.io/v2/icon/" + serverIp;

        new Thread(() -> {
            try {
                URL url = new URL(iconUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                runOnUiThread(() -> serverLogo.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Suppression du serveur de l'activité (ouvert en détail par l'utilisateur.
     */
    private void deleteServer() {
        new Thread(() -> {
            boolean isDeleted = dbHandler.deleteServerByIp(serverIp);

            runOnUiThread(() -> {
                if (isDeleted) {
                    Toast.makeText(ServerDetailsActivity.this, "Server deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ServerDetailsActivity.this, "Failed to delete server", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
