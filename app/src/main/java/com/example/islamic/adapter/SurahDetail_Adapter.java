package com.example.islamic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.islamic.R;

import java.util.ArrayList;

public class SurahDetail_Adapter extends RecyclerView.Adapter<SurahDetail_Adapter.viewHolder> {
    ArrayList<Integer> index=new ArrayList<>();
    ArrayList<String>name=new ArrayList<>();
    Context context ;

    public SurahDetail_Adapter(ArrayList<Integer> index, ArrayList<String> name, Context context) {
        this.index = index;
        this.name = name;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.surah_detail,null,false);
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
            textView1=itemView.findViewById(R.id.index);
            textView2=itemView.findViewById(R.id.arabic_text);

        }
    }
}
