package com.yura.buttleship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Intent selectIntent;
    Intent recordsIntent;
    Button button;
    Button recordsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.main_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(selectIntent);
            }
        });
        recordsButton = findViewById(R.id.records);
        recordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(recordsIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectIntent == null) {
            selectIntent = new Intent(this, BattleShipCreatingActivity.class);
        }
        if (recordsIntent == null) {
            recordsIntent = new Intent(this, RecordsActivity.class);
        }
    }

}
