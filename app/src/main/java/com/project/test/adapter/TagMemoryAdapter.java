package com.project.test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.test.R;
import com.project.test.model.Tag;
import com.project.test.model.TagMemory;

import java.util.List;

public class TagMemoryAdapter extends RecyclerView.Adapter<TagMemoryAdapter.TagMemViewHolder>{

    private List<TagMemory> tagMems;

    private TagMemoryAdapter.OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(TagMemory tag);
    }

    public void setTagMems(List<TagMemory> tagMems, TagMemoryAdapter.OnTagClickListener listener) {
        this.tagMems = tagMems;
        this.listener = listener;
        notifyDataSetChanged();
    }

    public void setTagMems(List<TagMemory> tagMems) {
        this.tagMems = tagMems;
        notifyDataSetChanged();
    }

    public void setListener(TagMemoryAdapter.OnTagClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagMemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memory_item, parent, false);
        return new TagMemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TagMemViewHolder holder, int position) {
        TagMemory tag = tagMems.get(position);
        if (tag == null) return;
        holder.bind(tag);
        holder.itemView.setOnClickListener(v -> listener.onTagClick(tag));
    }

    public TagMemory getTagAtPosition(int position) {
        return tagMems.get(position);
    }

    @Override
    public int getItemCount() {
        if (tagMems == null) return 0;
        return tagMems.size();
    }

    class TagMemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tagName;
        private final ImageView tagImage;
        private final Button button;
        private final CheckBox checkBox;
        private final TagMemoryAdapter.OnTagClickListener listener;

        public TagMemViewHolder(@NonNull View itemView, TagMemoryAdapter.OnTagClickListener listener) { // Thêm listener vào constructor
            super(itemView);
            this.listener = listener; // Gán listener cho biến instance
            this.tagImage = itemView.findViewById(R.id.image);
            this.button = itemView.findViewById(R.id.button);
            this.checkBox = itemView.findViewById(R.id.checkBox);
            this.tagName = itemView.findViewById(R.id.locationName);
        }

        public void bind(TagMemory tag) {
            tagName.setText(tag.getTagName());
            button.setOnClickListener(
                view -> {

                }
            );
            itemView.setOnClickListener(v -> listener.onTagClick(tag)); // Sử dụng listener
        }

        public void moveTag(int fromPosition, int toPosition) {
            TagMemory tagMoved = tagMems.get(fromPosition);
            tagMems.remove(fromPosition);
            tagMems.add(toPosition, tagMoved);

            // Cập nhật thứ tự cho tất cả các tag
            for (int i = 0; i < tagMems.size(); i++) {
                tagMems.get(i).setOrder(i); // Cập nhật thuộc tính order
            }

            notifyItemMoved(fromPosition, toPosition);
        }
        public void updateTags(List<TagMemory> updatedTags) {
            tagMems.clear();
            tagMems.addAll(updatedTags);
            notifyDataSetChanged();
        }
    }
}
