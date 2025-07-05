package com.example.islamic.view;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.islamic.R;
import com.example.islamic.adapter.Sabah_Adapter;
import com.example.islamic.databinding.ActivityAzkarSabahBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Azkar_Sabah extends AppCompatActivity {

    ArrayList<String> zekr;
    ArrayList<Integer> repeat;
    Sabah_Adapter adapter;

    private ActivityAzkarSabahBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAzkarSabahBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        zekr = new ArrayList<>();
        repeat = new ArrayList<>();

        try {

            JSONObject jsonObject = new JSONObject(JsonDataFromAsset("AzkarAl-Sabah.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("content");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject data = jsonArray.getJSONObject(i);
                zekr.add(data.getString("zekr"));
                repeat.add(data.getInt("repeat"));
            }
        } catch (JSONException e) {
            Log.e("Azkar_Sabah", "JSON Parsing Error: " + e.getMessage(), e);
            e.printStackTrace();
        }

        adapter = new Sabah_Adapter(repeat, zekr, this);
        binding.rv.setAdapter(adapter);
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setHasFixedSize(true);
    }


    private String JsonDataFromAsset(String fileName) {
        String json = null;
        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            json = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("AzkarActivity", "Error reading JSON from asset: " + fileName, e);
            e.printStackTrace();
        }
        return json;
    }
}