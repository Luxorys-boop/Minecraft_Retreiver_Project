package com.example.minecraft_retreiver_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddServerActivity extends AppCompatActivity {

    private TextInputEditText serverAddressEditText;
    private ProgressBar progressBar;
    private LinearLayout serverInfoLayout;
    private TextView serverNameText, serverMotdText, serverPlayersText, serverVersionText;
    private Button saveServerButton;
    private Button confirmAddButton;
    private DBHandler dbHandler;

    private String serverName, serverIp, serverMotd;
    private int onlinePlayers, maxPlayers;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        dbHandler = new DBHandler(this);

        serverAddressEditText = findViewById(R.id.serverAddressEditText);
        progressBar = findViewById(R.id.progressBar);
        serverInfoLayout = findViewById(R.id.serverInfoLayout);
        serverNameText = findViewById(R.id.serverNameText);
        serverMotdText = findViewById(R.id.serverMotdText);
        serverPlayersText = findViewById(R.id.serverPlayersText);
        serverVersionText = findViewById(R.id.serverVersionText);
        saveServerButton = findViewById(R.id.saveServerButton);
        confirmAddButton = findViewById(R.id.confirmAddButton);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        saveServerButton.setOnClickListener(v -> fetchServerInfo());
        confirmAddButton.setOnClickListener(v -> addServerToDatabase());
    }

    private void fetchServerInfo() {
        String address = serverAddressEditText.getText().toString().trim();

        if (address.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une adresse de serveur", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveServerButton.setEnabled(false);
        serverInfoLayout.setVisibility(View.GONE);

        executorService.execute(() -> {
            String result = fetchServerInfoFromApi(address);
            mainHandler.post(() -> handleServerInfoResult(result));
        });
    }

    private String fetchServerInfoFromApi(String address) {
        try {
            URL url = new URL("https://api.mcstatus.io/v2/status/java/" + address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleServerInfoResult(String result) {
        progressBar.setVisibility(View.GONE);
        saveServerButton.setEnabled(true);

        if (result == null) {
            Toast.makeText(AddServerActivity.this, "Erreur lors de la récupération des informations", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject(result);

            if (!json.getBoolean("online")) {
                Toast.makeText(AddServerActivity.this, "Le serveur est hors ligne", Toast.LENGTH_SHORT).show();
                return;
            }

            // Récupération des données du serveur
            serverIp = json.getString("host");
            serverName = serverIp; // Par défaut, on utilise l'adresse comme nom

            // Si le serveur a un nom personnalisé dans le MOTD
            if (json.has("motd")) {
                JSONObject motd = json.getJSONObject("motd");
                serverMotd = motd.getString("clean");

                // On peut essayer d'extraire un nom plus parlant du MOTD
                String[] motdLines = serverMotd.split("\n");
                if (motdLines.length > 0 && !motdLines[0].trim().isEmpty()) {
                    serverName = motdLines[0].trim();
                }
            }

            // Récupération des joueurs
            if (json.has("players")) {
                JSONObject players = json.getJSONObject("players");
                onlinePlayers = players.getInt("online");
                maxPlayers = players.getInt("max");
            }

            // Affichage des informations
            serverNameText.setText(serverName);
            if(serverMotd != null) {
                serverMotdText.setText(serverMotd);
            } else {
                serverMotdText.setText("Pas de description");
            }
            serverPlayersText.setText(onlinePlayers + " / " + maxPlayers + " joueurs en ligne");

            if (json.has("version")) {
                JSONObject version = json.getJSONObject("version");
                serverVersionText.setText("Version: " + version.getString("name_clean"));
            }

            serverInfoLayout.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AddServerActivity.this, "Erreur lors du traitement des données", Toast.LENGTH_SHORT).show();
        }
    }

    private void addServerToDatabase() {
        // Récupérer l'ID de l'utilisateur connecté
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        int userId = dbHandler.getUserIdByEmail(userEmail);

        if (userId == -1) {
            Toast.makeText(this, "Erreur: utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ajouter le serveur à la base de données
        long serverId = dbHandler.addServer(serverName, serverIp, serverMotd, userId);

        if (serverId != -1) {
            Toast.makeText(this, "Serveur ajouté avec succès", Toast.LENGTH_SHORT).show();
            // Rafraichissement de la page
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout du serveur", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
