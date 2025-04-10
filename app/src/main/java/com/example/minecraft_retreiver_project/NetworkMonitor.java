package com.example.minecraft_retreiver_project;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class NetworkMonitor extends ConnectivityManager.NetworkCallback {
    private final Context context;

    public NetworkMonitor(Context context) {
        this.context = context;
    }

    @Override
    public void onAvailable(Network network) {
        showToast("Réseau disponible !");
    }

    @Override
    public void onLost(Network network) {
        showToast("Aucune connexion internet...");
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}