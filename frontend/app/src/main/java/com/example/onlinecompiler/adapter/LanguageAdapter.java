package com.example.onlinecompiler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onlinecompiler.R;
import com.example.onlinecompiler.models.Language;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languages;
    private OnItemClickListener onItemClickListener;

    // Constructor to pass data (languages) to the adapter
    public LanguageAdapter(List<Language> languages) {
        this.languages = languages;
    }

    @Override
    public LanguageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LanguageViewHolder holder, int position) {
        Language language = languages.get(position);
        holder.languageNameTextView.setText(language.getName());
        holder.languageVersionTextView.setText(language.getCompilerVersion());
        holder.languageImageView.setImageResource(language.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(language);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView languageNameTextView;
        TextView languageVersionTextView;
        ImageView languageImageView;

        public LanguageViewHolder(View itemView) {
            super(itemView);
            languageNameTextView = itemView.findViewById(R.id.language_name);
            languageVersionTextView = itemView.findViewById(R.id.language_version);
            languageImageView = itemView.findViewById(R.id.language_image);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Language language);
    }
}
