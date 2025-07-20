package com.example.islamic.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.islamic.R;
import com.example.islamic.adapter.TafseerSurahAdapter;
import com.example.islamic.listener.SurahListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;

public class TafseerSurahActivity extends AppCompatActivity implements SurahListener {
    RecyclerView recyclerView;
    ArrayList<Integer> index;
    ArrayList<String> name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tafseer_surah);
        recyclerView = findViewById(R.id.tafseer_surah_rv);
        index = new ArrayList<>();
        name = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        try {
            JSONObject jsonObject = new JSONObject(JsonDataFromAsset());
            JSONArray jsonArray = jsonObject.getJSONArray("surah");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject data = jsonArray.getJSONObject(i);
                index.add(data.getInt("index"));
                name.add(data.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TafseerSurahAdapter adapter = new TafseerSurahAdapter(index, name, this, this);
        recyclerView.setAdapter(adapter);
    }

    private String JsonDataFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("Quran.json");
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onSurahListener(int position) {
        Intent intent = new Intent(TafseerSurahActivity.this, TafseerDetailActivity.class);
        intent.putExtra("SURA_NO", index.get(position));
        startActivity(intent);
    }
}