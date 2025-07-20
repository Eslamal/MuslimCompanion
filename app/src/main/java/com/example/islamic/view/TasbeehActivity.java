package com.example.islamic.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView; // تم حذف استيراد ImageView
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.islamic.R;

public class TasbeehActivity extends AppCompatActivity {

    // تعريف المتغيرات للواجهات
    private CardView cardSubhanAllah, cardAlhamdulillah, cardAllahuAkbar, cardAstaghfirullah, cardResetAll;
    private TextView tvSubhanAllahCount, tvAlhamdulillahCount, tvAllahuAkbarCount, tvAstaghfirullahCount;

    // تم تغيير النوع من ImageView إلى TextView
    private TextView tvResetSubhanallah, tvResetAlhamdulillah, tvResetAllahuakbar, tvResetAstaghfirullah;

    // تعريف متغيرات العدادات
    private int subhanAllahCount = 0;
    private int alhamdulillahCount = 0;
    private int allahuAkbarCount = 0;
    private int astaghfirullahCount = 0;

    // تعريف SharedPreferences لحفظ البيانات
    private SharedPreferences sharedPreferences;
    public static final String SHARED_PREFS = "tasbeehSharedPrefs";
    public static final String SUBHANALLAH_KEY = "subhanAllahKey";
    public static final String ALHAMDULILLAH_KEY = "alhamdulillahKey";
    public static final String ALLAHUAKBAR_KEY = "allahuAkbarKey";
    public static final String ASTAGHFIRULLAH_KEY = "astaghfirullahKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasbeeh);

        initializeViews();
        loadData();
        updateViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardSubhanAllah = findViewById(R.id.card_subhanAllah);
        cardAlhamdulillah = findViewById(R.id.card_alhamdulillah);
        cardAllahuAkbar = findViewById(R.id.card_allahuAkbar);
        cardAstaghfirullah = findViewById(R.id.card_astaghfirullah);

        tvSubhanAllahCount = findViewById(R.id.tv_subhanAllah_count);
        tvAlhamdulillahCount = findViewById(R.id.tv_alhamdulillah_count);
        tvAllahuAkbarCount = findViewById(R.id.tv_allahuAkbar_count);
        tvAstaghfirullahCount = findViewById(R.id.tv_astaghfirullah_count);

        // ربط أزرار التصفير النصية
        tvResetSubhanallah = findViewById(R.id.tv_reset_subhanallah);
        tvResetAlhamdulillah = findViewById(R.id.tv_reset_alhamdulillah);
        tvResetAllahuakbar = findViewById(R.id.tv_reset_allahuakbar);
        tvResetAstaghfirullah = findViewById(R.id.tv_reset_astaghfirullah);

        cardResetAll = findViewById(R.id.card_reset_all);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }

    private void setupClickListeners() {
        // مستمعي النقر على البطاقات لزيادة العداد
        cardSubhanAllah.setOnClickListener(v -> {
            subhanAllahCount++;
            tvSubhanAllahCount.setText(String.valueOf(subhanAllahCount));
        });

        cardAlhamdulillah.setOnClickListener(v -> {
            alhamdulillahCount++;
            tvAlhamdulillahCount.setText(String.valueOf(alhamdulillahCount));
        });

        cardAllahuAkbar.setOnClickListener(v -> {
            allahuAkbarCount++;
            tvAllahuAkbarCount.setText(String.valueOf(allahuAkbarCount));
        });

        cardAstaghfirullah.setOnClickListener(v -> {
            astaghfirullahCount++;
            tvAstaghfirullahCount.setText(String.valueOf(astaghfirullahCount));
        });

        // مستمعي النقر على أزرار التصفير النصية
        tvResetSubhanallah.setOnClickListener(v -> {
            subhanAllahCount = 0;
            tvSubhanAllahCount.setText(String.valueOf(subhanAllahCount));
        });

        tvResetAlhamdulillah.setOnClickListener(v -> {
            alhamdulillahCount = 0;
            tvAlhamdulillahCount.setText(String.valueOf(alhamdulillahCount));
        });

        tvResetAllahuakbar.setOnClickListener(v -> {
            allahuAkbarCount = 0;
            tvAllahuAkbarCount.setText(String.valueOf(allahuAkbarCount));
        });

        tvResetAstaghfirullah.setOnClickListener(v -> {
            astaghfirullahCount = 0;
            tvAstaghfirullahCount.setText(String.valueOf(astaghfirullahCount));
        });

        // مستمع النقر على زر التصفير الكلي
        cardResetAll.setOnClickListener(v -> {
            resetAllCounts();
        });
    }

    private void resetAllCounts() {
        subhanAllahCount = 0;
        alhamdulillahCount = 0;
        allahuAkbarCount = 0;
        astaghfirullahCount = 0;
        updateViews();
    }

    private void updateViews() {
        tvSubhanAllahCount.setText(String.valueOf(subhanAllahCount));
        tvAlhamdulillahCount.setText(String.valueOf(alhamdulillahCount));
        tvAllahuAkbarCount.setText(String.valueOf(allahuAkbarCount));
        tvAstaghfirullahCount.setText(String.valueOf(astaghfirullahCount));
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SUBHANALLAH_KEY, subhanAllahCount);
        editor.putInt(ALHAMDULILLAH_KEY, alhamdulillahCount);
        editor.putInt(ALLAHUAKBAR_KEY, allahuAkbarCount);
        editor.putInt(ASTAGHFIRULLAH_KEY, astaghfirullahCount);
        editor.apply();
    }

    private void loadData() {
        subhanAllahCount = sharedPreferences.getInt(SUBHANALLAH_KEY, 0);
        alhamdulillahCount = sharedPreferences.getInt(ALHAMDULILLAH_KEY, 0);
        allahuAkbarCount = sharedPreferences.getInt(ALLAHUAKBAR_KEY, 0);
        astaghfirullahCount = sharedPreferences.getInt(ASTAGHFIRULLAH_KEY, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }
}