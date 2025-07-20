package com.example.islamic.view;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic.R;
import com.example.islamic.model.Hadith;

public class HadithDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hadith_detail);

        TextView hadithTitle = findViewById(R.id.hadith_detail_title);
        TextView hadithText = findViewById(R.id.hadith_detail_text);
        TextView hadithDescription = findViewById(R.id.hadith_detail_description);

        Hadith hadith = (Hadith) getIntent().getSerializableExtra("hadith_object");

        if (hadith != null) {
            hadithTitle.setText(hadith.getTitle());
            hadithText.setText(hadith.getHadithText());
            hadithDescription.setText(hadith.getDescription());
        }
    }
}