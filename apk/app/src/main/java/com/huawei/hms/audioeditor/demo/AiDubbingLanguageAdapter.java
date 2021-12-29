package com.huawei.hms.audioeditor.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 适配器
 * @since 2021/8/10
 */
public class AiDubbingLanguageAdapter extends RecyclerView.Adapter<AiDubbingLanguageAdapter.LanguageViewHolder>{
    private Context context;
    private List<String> list;

    public AiDubbingLanguageAdapter(FragmentActivity aiAudioActivity, List<String> languagesList) {
        this.context = aiAudioActivity;
        this.list = languagesList;
    }

    public void setList( List<String> languagesList){
        this.list = languagesList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.ai_dubbing_language_item, parent, false);
        LanguageViewHolder languageViewHolder = new LanguageViewHolder(inflate);
        return languageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {

        holder.aiDubbingLanguage.setText(list.get(position));

        if (null != onItemClickListener) {
            // Callback click event
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Callback
                    onItemClickListener.setOnLanguageItemClick(v, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public interface OnItemClickListener {
        /**
         * Entry click event
         *
         * @param view view
         * @param position position
         */
        void setOnLanguageItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder{

        private final TextView aiDubbingLanguage;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            aiDubbingLanguage = itemView.findViewById(R.id.dubbing_language);
        }
    }
}
