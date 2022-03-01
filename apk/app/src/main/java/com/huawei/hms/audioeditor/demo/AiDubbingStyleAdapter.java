package com.huawei.hms.audioeditor.demo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @date 2021/8/10
 * @since 2021/8/10
 */
public class AiDubbingStyleAdapter extends RecyclerView.Adapter<AiDubbingStyleAdapter.LanguageViewHolder> {
    private Context context;
    private List<String> list;
    private String name;

    public AiDubbingStyleAdapter(FragmentActivity aiDubbingAudioActivity, List<String> styleList ,String name) {
        this.context = aiDubbingAudioActivity;
        this.list = styleList;
        this.name = name;
    }

    public void setList(List<String> lists){
        this.list=lists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.ai_dubbing_style_item, parent, false);
        LanguageViewHolder languageViewHolder = new LanguageViewHolder(inflate);
        return languageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        holder.aiDubbingStyle.setText(list.get(position));
        if (list.get(position).equals(name)){
            holder.aiDubbingStyle.setTextColor(Color.parseColor("#5CACEE"));
        } else {
            holder.aiDubbingStyle.setTextColor(Color.parseColor("#626262"));
        }

        if (null != onItemClickListener) {
            // Callback click event
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Callback
                    onItemClickListener.setOnStyleItemClick(v, position);
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
        void setOnStyleItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class LanguageViewHolder extends RecyclerView.ViewHolder {

        private final TextView aiDubbingStyle;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            aiDubbingStyle = itemView.findViewById(R.id.ai_style);
        }
    }
}
