package com.eslamdev.islamic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.islamic.R;
import com.eslamdev.islamic.model.TafseerAya;
import java.util.ArrayList;

public class TafseerDetailAdapter extends RecyclerView.Adapter<TafseerDetailAdapter.ViewHolder> {

    private final ArrayList<TafseerAya> tafseerAyaList;

    public TafseerDetailAdapter(ArrayList<TafseerAya> tafseerAyaList) {
        this.tafseerAyaList = tafseerAyaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tafseer_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TafseerAya currentItem = tafseerAyaList.get(position);
        String ayaFullText = currentItem.getAyaText() + " (" + currentItem.getAyaNumber() + ")";
        holder.ayaTextView.setText(ayaFullText);
        holder.tafseerTextView.setText(currentItem.getTafseerText());
    }

    @Override
    public int getItemCount() {
        return tafseerAyaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ayaTextView;
        TextView tafseerTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ayaTextView = itemView.findViewById(R.id.aya_text);
            tafseerTextView = itemView.findViewById(R.id.tafseer_text);
        }
    }
}