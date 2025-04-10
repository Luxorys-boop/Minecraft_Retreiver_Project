package com.example.minecraft_retreiver_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AccueilActivity extends AppCompatActivity {

    private LinearLayout noServersLayout;
    private ScrollView serversScrollView;
    private ProgressBar progressBar;
    private DBHandler dbHandler;
    private Button addServerButton, addFirstServerButton, suggestServersButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton logOutImageButton;

    /**
     * Lanceur d'activité pour refresh la page quand un serveur est ajouté.
     */
    private final ActivityResultLauncher<Intent> addServerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String userEmail = getIntent().getStringExtra("USER_EMAIL");
                    checkUserServers(userEmail);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        // Initialisation des vues
        noServersLayout = findViewById(R.id.noServersLayout);
        serversScrollView = findViewById(R.id.serversScrollView);
        progressBar = findViewById(R.id.progressBar);
        addServerButton = findViewById(R.id.addServerButton);
        addFirstServerButton = findViewById(R.id.addFirstServerButton);
        suggestServersButton = findViewById(R.id.suggestServersButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        logOutImageButton = findViewById(R.id.logoutButton);

        dbHandler = new DBHandler(this);

        // Récupération de l'email de l'utilisateur
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Erreur: utilisateur non identifié", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialisation des boutons avec l'email passé en paramètres
        setupButtons(userEmail);

        // Réinitialisation afin de rafraîchir la liste des serveurs avec l'email passé en paramètres
        swipeRefreshLayout.setOnRefreshListener(() -> checkUserServers(userEmail));

        // Vérification si l'utilisateur a des serveurs afin de modifier le layout en conséquence
        checkUserServers(userEmail);
    }

    /**
     * Configure les boutons de l'interface utilisateur.
     * @param userEmail L'email de l'utilisateur connecté.
     */
    private void setupButtons(String userEmail) {
        addServerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccueilActivity.this, AddServerActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            addServerLauncher.launch(intent);
        });

        addFirstServerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccueilActivity.this, AddServerActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            addServerLauncher.launch(intent);
        });

        logOutImageButton.setOnClickListener(v -> logoutUser());
    }

    /**
     * Vérifie et affiche les serveurs de l'utilisateur.
     * @param userEmail L'email de l'utilisateur connecté.
     */
    private void checkUserServers(String userEmail) {
        progressBar.setVisibility(View.VISIBLE);
        serversScrollView.setVisibility(View.GONE);
        noServersLayout.setVisibility(View.GONE);

        new Thread(() -> {
            List<Servers> userServers = dbHandler.getUserServers(userEmail);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (userServers == null || userServers.isEmpty()) {
                    noServersLayout.setVisibility(View.VISIBLE);
                    serversScrollView.setVisibility(View.GONE);
                    Log.d("AccueilActivity", "No servers found for user: " + userEmail);
                } else {
                    noServersLayout.setVisibility(View.GONE);
                    serversScrollView.setVisibility(View.VISIBLE);

                    LinearLayout serversLayout = findViewById(R.id.serversLayout);
                    serversLayout.removeAllViews();

                    for (Servers server : userServers) {
                        View serverView = getLayoutInflater().inflate(R.layout.server_item, serversLayout, false);
                        TextView serverNameTextView = serverView.findViewById(R.id.serverName);
                        TextView serverDescriptionTextView = serverView.findViewById(R.id.serverDescription);
                        ImageView serverImg = serverView.findViewById(R.id.serverImg);

                        serverNameTextView.setText(server.getNom());
                        serverDescriptionTextView.setText(server.getMotd());

                        // Récupération de l'icône du serveur
                        fetchServerIcon(server.getIp(), serverImg);

                        serverView.setOnClickListener(v -> fetchServerDetails(server.getIp()));

                        serversLayout.addView(serverView);
                        Log.d("AccueilActivity", "Server displayed: " + server.getNom());
                    }
                }
            });
        }).start();
    }

    /**
     * Récupère l'icône du serveur à partir de l'API.
     * @param serverIp L'adresse IP du serveur.
     * @param serverImg L'ImageView où l'icône sera affichée.
     */
    private void fetchServerIcon(String serverIp, ImageView serverImg) {
        String iconUrl = "https://api.mcstatus.io/v2/icon/" + serverIp;

        new Thread(() -> {
            try {
                URL url = new URL(iconUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                runOnUiThread(() -> serverImg.setImageBitmap(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Récupère les détails du serveur à partir de l'API.
     * @param serverIp L'adresse IP du serveur.
     */
    private void fetchServerDetails(String serverIp) {
        String apiUrl = "https://api.mcstatus.io/v2/status/java/" + serverIp;

        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());
                            String motd = jsonObject.getJSONObject("motd").getString("clean");
                            int playersOnline = jsonObject.getJSONObject("players").getInt("online");
                            int maxPlayers = jsonObject.getJSONObject("players").getInt("max");

                            Intent intent = new Intent(AccueilActivity.this, ServerDetailsActivity.class);
                            intent.putExtra("MOTD", motd);
                            intent.putExtra("PLAYERS_ONLINE", playersOnline);
                            intent.putExtra("MAX_PLAYERS", maxPlayers);
                            intent.putExtra("SERVER_IP", serverIp); // Passer l'adresse IP du serveur
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Échec de la récupération des détails du serveur", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Déconnecte l'utilisateur et retourne à l'écran de connexion.
     */
    private void logoutUser() {
        Intent intent = new Intent(AccueilActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
