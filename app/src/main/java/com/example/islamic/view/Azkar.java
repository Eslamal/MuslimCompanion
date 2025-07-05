package com.example.islamic.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.islamic.R;
import com.example.islamic.databinding.ActivityAzkarBinding;

public class Azkar extends AppCompatActivity {

    private ActivityAzkarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAzkarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.cardAzkarAlsabah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Azkar.this, Azkar_Sabah.class);
                startActivity(intent);
            }
        });

        binding.cardAzkarAlmassa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Azkar.this, Azkar_Massa.class);
                startActivity(intent);
            }
        });

        binding.cardAzkarAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Azkar.this, Azkar_After.class);
                startActivity(intent);
            }
        });
    }
}