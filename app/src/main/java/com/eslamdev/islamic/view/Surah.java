package com.eslamdev.islamic.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.islamic.R;
import com.eslamdev.islamic.adapter.Surah_Adapter;
import com.eslamdev.islamic.listener.SurahListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class Surah extends AppCompatActivity implements SurahListener {
RecyclerView recyclerView ;
    ArrayList<Integer>index;
    ArrayList<String>name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah);
        recyclerView=findViewById(R.id.surah_rv);
        index=new ArrayList<>();
        name=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        try {
            JSONObject jsonObject=new JSONObject(JsonDataFromAsset());
            JSONArray jsonArray=jsonObject.getJSONArray("surah");
            for(int i =0 ;i<jsonArray.length() ; i++ ){
                JSONObject data=jsonArray.getJSONObject(i);
                index.add(data.getInt("index"));
                name.add(data.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Surah_Adapter adapter=new Surah_Adapter(index,name,this::onSurahListener,this);
        recyclerView.setAdapter(adapter);
    }

    private String JsonDataFromAsset() {
        String json=null ;
        try{
            InputStream inputStream=getAssets().open("Quran.json");
            int sizeOfFile = inputStream.available();
            byte[]bufferData=new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json=new String(bufferData,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            return null ;}
        return json ; }

    @Override
    public void onSurahListener(int position) {
        Intent intent=new Intent(Surah.this,SurahDetail.class);
        intent.putExtra("index",index.get(position));
        intent.putExtra("name",name.get(position));
        startActivity(intent);
    }
}