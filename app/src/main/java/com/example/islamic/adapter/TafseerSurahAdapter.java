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

public class TafseerSurahAdapter extends RecyclerView.Adapter<TafseerSurahAdapter.ViewHolder> {
    private ArrayList<Integer> index;
    private ArrayList<String> name;
    private SurahListener surahListener;
    private Context context;

    public TafseerSurahAdapter(ArrayList<Integer> index, ArrayList<String> name, SurahListener surahListener, Context context) {
        this.index = index;
        this.name = name;
        this.surahListener = surahListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.surah_item_for_tafseer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.surahNumber.setText(String.valueOf(index.get(position)));
        holder.surahName.setText(name.get(position));
    }

    @Override
    public int getItemCount() {
        return index.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView surahNumber, surahName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            surahNumber = itemView.findViewById(R.id.surah_number);
            surahName = itemView.findViewById(R.id.arabic_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (surahListener != null) {
                        surahListener.onSurahListener(getAdapterPosition());
                    }
                }
            });
        }
    }
}