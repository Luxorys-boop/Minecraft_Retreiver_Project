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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        db = new DBHandler(this);
        buttonReg = findViewById(R.id.buttonReg);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.registerPassword);
        pseudoField = findViewById(R.id.pseudoField);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Toast.LENGTH_SHORT;
                if(db.registerUser(etEmail.getText().toString(), etPass.getText().toString(), pseudoField.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "REGISTERED", duration).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "ERREUR RÉESSAYEZ", duration).show();
                }

            }
        });
    }


}
