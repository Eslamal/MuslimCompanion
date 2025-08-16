package com.eslamdev.islamic.view;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.islamic.R;
import com.eslamdev.islamic.adapter.TafseerDetailAdapter;
import com.eslamdev.islamic.model.TafseerAya;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class TafseerDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView surahNameTextView;
    private ArrayList<TafseerAya> tafseerAyaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tafseer_detail);

        recyclerView = findViewById(R.id.tafseer_recycler_view);
        surahNameTextView = findViewById(R.id.surah_name);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int surahNumber = getIntent().getIntExtra("SURA_NO", -1);

        if (surahNumber != -1) {
            loadData(surahNumber);
        } else {
            surahNameTextView.setText("خطأ في تحديد السورة");
        }

        TafseerDetailAdapter adapter = new TafseerDetailAdapter(tafseerAyaList);
        recyclerView.setAdapter(adapter);
    }

    private void loadData(int surahNumber) {
        try {
            // تحميل نصوص الآيات من QuranDetails.json
            HashMap<Integer, String> ayasMap = new HashMap<>();
            JSONObject quranDetailsJson = new JSONObject(loadJSONFromAsset("QuranDetails.json"));
            JSONArray surahsArray = quranDetailsJson.getJSONArray("surah");
            for (int i = 0; i < surahsArray.length(); i++) {
                JSONObject surah = surahsArray.getJSONObject(i);
                if (surah.getInt("index") == surahNumber) {
                    surahNameTextView.setText(surah.getString("name"));
                    JSONArray ayas = surah.getJSONArray("aya");
                    for (int j = 0; j < ayas.length(); j++) {
                        JSONObject aya = ayas.getJSONObject(j);
                        ayasMap.put(aya.getInt("index"), aya.getString("text"));
                    }
                    break;
                }
            }

            // تحميل التفسير من tafseer.json
            JSONArray tafseerJsonArray = new JSONArray(loadJSONFromAsset("tafseer.json"));
            for (int i = 0; i < tafseerJsonArray.length(); i++) {
                JSONObject tafseerObject = tafseerJsonArray.getJSONObject(i);
                if (tafseerObject.getInt("number") == surahNumber) {
                    int ayaNum = tafseerObject.getInt("aya");
                    String tafseerText = tafseerObject.getString("text");
                    String ayaText = ayasMap.get(ayaNum);

                    // إزالة "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" من الآية الأولى إذا وجدت
                    if(ayaNum == 1 && surahNumber != 1 && surahNumber != 9){
                        if(ayaText != null && ayaText.startsWith("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")){
                            ayaText = ayaText.substring(39).trim();
                        }
                    }

                    if (ayaText != null) {
                        tafseerAyaList.add(new TafseerAya(ayaText, tafseerText, ayaNum));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}