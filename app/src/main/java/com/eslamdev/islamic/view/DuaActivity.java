package com.eslamdev.islamic.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.islamic.R;
import com.eslamdev.islamic.adapter.DuaAdapter;
import com.eslamdev.islamic.model.Dua;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DuaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DuaAdapter adapter;
    private List<Object> itemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua);

        recyclerView = findViewById(R.id.dua_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DuaAdapter();
        recyclerView.setAdapter(adapter);

        loadDuaData();
    }

    private void loadDuaData() {
        try {
            InputStream is = getAssets().open("duas_collection.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray categoriesArray = new JSONArray(json);

            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject categoryObj = categoriesArray.getJSONObject(i);
                String categoryTitle = categoryObj.getString("category");
                itemsList.add(categoryTitle); // إضافة عنوان الفئة

                JSONArray duasArray = categoryObj.getJSONArray("duas");
                for (int j = 0; j < duasArray.length(); j++) {
                    JSONObject duaObj = duasArray.getJSONObject(j);
                    String title = duaObj.getString("title");
                    String duaText = duaObj.getString("dua");
                    itemsList.add(new Dua(title, duaText, categoryTitle)); // إضافة الدعاء
                }
            }

            adapter.setData(itemsList);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في تحميل ملف الأدعية!", Toast.LENGTH_LONG).show();
        }
    }
}