package com.eslamdev.islamic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.islamic.R;
import com.eslamdev.islamic.model.Hadith;
import java.util.ArrayList;

public class HadithAdapter extends RecyclerView.Adapter<HadithAdapter.ViewHolder> {

    private final ArrayList<Hadith> hadithList;
    private final OnHadithClickListener listener;

    public interface OnHadithClickListener {
        void onHadithClick(Hadith hadith);
    }

    public HadithAdapter(ArrayList<Hadith> hadithList, OnHadithClickListener listener) {
        this.hadithList = hadithList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hadith_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hadith hadith = hadithList.get(position);
        holder.hadithId.setText(String.valueOf(position + 1));
        holder.hadithTitle.setText(hadith.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onHadithClick(hadith));
    }

    @Override
    public int getItemCount() {
        return hadithList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hadithId, hadithTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hadithId = itemView.findViewById(R.id.hadith_id);
            hadithTitle = itemView.findViewById(R.id.hadith_title);
        }
    }
}