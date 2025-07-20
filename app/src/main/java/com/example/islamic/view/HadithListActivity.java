package com.example.islamic.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.islamic.R;
import com.example.islamic.adapter.HadithAdapter;
import com.example.islamic.model.Hadith;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HadithListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Hadith> hadithList = new ArrayList<>();
    private HadithAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hadith_list);

        recyclerView = findViewById(R.id.hadith_list_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HadithAdapter(hadithList, hadith -> {
            Intent intent = new Intent(HadithListActivity.this, HadithDetailActivity.class);
            intent.putExtra("hadith_object", hadith);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadHadithData();
    }

    private void loadHadithData() {
        try {
            InputStream is = getAssets().open("hadith.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            hadithList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String fullHadith = obj.getString("hadith");
                String description = obj.getString("description");

                // استخراج العنوان ونص الحديث من حقل "hadith"
                String[] parts = fullHadith.split("\n\n", 2);
                String title = parts.length > 0 ? parts[0] : "حديث " + (i + 1);
                String hadithText = parts.length > 1 ? parts[1] : fullHadith;

                hadithList.add(new Hadith(title, hadithText, description));
            }

            adapter.notifyDataSetChanged();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في تحميل ملف الأحاديث!", Toast.LENGTH_LONG).show();
        }
    }
}