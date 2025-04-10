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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Toast.LENGTH_SHORT;
                if(db.checkUser(etEmail.getText().toString(), etPass.getText().toString())) {
                    Toast.makeText(MainActivity.this, "CONNECTÉ", duration).show();
                    Intent ___ = new Intent(getApplicationContext(), ___);
                    startActivity(___);
                } else {
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