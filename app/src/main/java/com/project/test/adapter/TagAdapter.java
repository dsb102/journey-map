package com.project.test.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.test.R;
import com.project.test.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private static List<Tag> tags ;
    private final OnTagClickListener listener;

    public TagAdapter(List<Tag> tags, OnTagClickListener listener) {
        this.tags = tags;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new TagViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        holder.bind(tag);
        holder.itemView.setOnClickListener(v -> listener.onTagClick(tag));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void listTags(List<Tag> newTags) {
        tags = newTags;
        notifyDataSetChanged();
    }

    public void setTags(List<Tag> newTags) {
        tags = newTags;
        notifyDataSetChanged();
    }

    public Tag getTagAtPosition(int position) {
        return tags.get(position);
    }

    public interface OnTagClickListener {
        void onTagClick(Tag tag);
    }

    public void moveTag(int fromPosition, int toPosition) {
        Tag movedTag = tags.remove(fromPosition);
        tags.add(toPosition, movedTag);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tags); // Trả về một bản sao của danh sách tags
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final TextView tagName;
        private final OnTagClickListener listener;

        public TagViewHolder(@NonNull View itemView, OnTagClickListener listener) { // Thêm listener vào constructor
            super(itemView);
            this.listener = listener; // Gán listener cho biến instance
            tagName = itemView.findViewById(R.id.locationName);
        }

        public void bind(Tag tag) {
            tagName.setText(tag.getTagName());
            itemView.setOnClickListener(v -> listener.onTagClick(tag)); // Sử dụng listener
        }
        public void moveTag(int fromPosition, int toPosition) {
            Tag tagMoved = tags.get(fromPosition);
            tags.remove(fromPosition);
            tags.add(toPosition, tagMoved);

            // Cập nhật thứ tự cho tất cả các tag
            for (int i = 0; i < tags.size(); i++) {
                tags.get(i).setOrder(i); // Cập nhật thuộc tính order
            }

            notifyItemMoved(fromPosition, toPosition);
        }
        public void updateTags(List<Tag> updatedTags) {
            tags.clear();
            tags.addAll(updatedTags);
            notifyDataSetChanged();
        }
    }
}
