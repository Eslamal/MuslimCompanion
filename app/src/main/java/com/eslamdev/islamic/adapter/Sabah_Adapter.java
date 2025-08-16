package com.eslamdev.islamic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.islamic.R;

import java.util.ArrayList;
import java.util.Locale;

public class Sabah_Adapter extends RecyclerView.Adapter<Sabah_Adapter.ViewHolder> {
    ArrayList<Integer> repeat;
    ArrayList<String> zekr;
    Context context;
    private Locale arabicLocale;

    public Sabah_Adapter(ArrayList<Integer> repeat, ArrayList<String> zekr, Context context) {
        this.repeat = repeat;
        this.zekr = zekr;
        this.context = context;
        this.arabicLocale = new Locale("ar", "EG");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zekr, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (zekr != null && repeat != null) {
            holder.zekrTextView.setText(zekr.get(position));
            String formattedRepeat = context.getString(R.string.repeat_count_format, convertToEasternArabic(String.valueOf(repeat.get(position))));
            holder.repeatCountTextView.setText(formattedRepeat);
        }
    }

    @Override
    public int getItemCount() {
        if (zekr != null) {
            return zekr.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView zekrTextView, repeatCountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            zekrTextView = itemView.findViewById(R.id.zekr_text);
            repeatCountTextView = itemView.findViewById(R.id.repeat_count);
        }
    }
    private String convertToEasternArabic(String numberString) {
        char[] arabicNumbers = {'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        StringBuilder builder = new StringBuilder();
        for (char ch : numberString.toCharArray()) {
            if (Character.isDigit(ch)) {
                builder.append(arabicNumbers[Character.getNumericValue(ch)]);
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}