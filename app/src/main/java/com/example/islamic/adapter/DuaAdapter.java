package com.example.islamic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.islamic.R;
import com.example.islamic.model.Dua;
import java.util.ArrayList;
import java.util.List;

public class DuaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_DUA = 1;

    private List<Object> items = new ArrayList<>();

    public void setData(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_CATEGORY;
        } else if (items.get(position) instanceof Dua) {
            return TYPE_DUA;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_header_layout, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dua_item_layout, parent, false);
            return new DuaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CATEGORY) {
            CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
            categoryViewHolder.categoryTitle.setText((String) items.get(position));
        } else {
            DuaViewHolder duaViewHolder = (DuaViewHolder) holder;
            Dua dua = (Dua) items.get(position);
            duaViewHolder.duaTitle.setText(dua.getTitle());
            duaViewHolder.duaText.setText(dua.getDuaText());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder للدعاء
    static class DuaViewHolder extends RecyclerView.ViewHolder {
        TextView duaTitle, duaText;
        DuaViewHolder(@NonNull View itemView) {
            super(itemView);
            duaTitle = itemView.findViewById(R.id.tv_dua_title);
            duaText = itemView.findViewById(R.id.tv_dua_text);
        }
    }

    // ViewHolder لعنوان الفئة
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.tv_category_title);
        }
    }
}