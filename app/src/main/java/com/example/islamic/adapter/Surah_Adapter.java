package com.example.islamic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.islamic.R;
import com.example.islamic.listener.SurahListener;

import java.util.ArrayList;

public class Surah_Adapter extends RecyclerView.Adapter<Surah_Adapter.viewHolder> {
    ArrayList<Integer>index=new ArrayList<>();
    ArrayList<String>name=new ArrayList<>();
    private SurahListener surahListener;
    Context context ;

    public Surah_Adapter(ArrayList<Integer> index, ArrayList<String> name, SurahListener surahListener, Context context) {
        this.index = index;
        this.name = name;
        this.surahListener = surahListener;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.surah,null,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        if (holder.textView1 != null && holder.textView2 != null && name.get(position) != null && index.get(position) != null) {
            holder.textView1.setText(index.get(position).toString());
            holder.textView2.setText(name.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return index.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView textView1,textView2;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            textView1=itemView.findViewById(R.id.surah_number);
            textView2=itemView.findViewById(R.id.arabic_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
             surahListener.onSurahListener(getAdapterPosition());
                }
            });
        }
    }
}
