package com.project.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.test.R;
import com.project.test.model.TagMemory;

import java.util.List;

public class TagMemoryAdapter extends RecyclerView.Adapter<TagMemoryAdapter.TagMemViewHolder>{

    private List<TagMemory> tagMems;

    private Context context;

    private TagMemoryAdapter.OnTagClickListener listener;

    private TagMemoryAdapter.OnClickButton onClickButton;

    private TagMemoryAdapter.ClickCheckBox clickCheckBox;

    private AddNoteClick addNoteClick;


    public interface OnTagClickListener {
        void onTagClick(TagMemory tag);
    }

    public interface OnClickButton {
        void onClickButton(int position);
    }

    public interface ClickCheckBox {
        void onClickCheckBox(boolean check, int position);
    }

    public interface AddNoteClick {
        void onClickAddNote(int position);
    }

    public TagMemoryAdapter() {
    }

    public void setTagMems(List<TagMemory> tagMems, TagMemoryAdapter.OnTagClickListener listener, TagMemoryAdapter.OnClickButton onClickButton, TagMemoryAdapter.ClickCheckBox onClickCheckBox, AddNoteClick addNoteClick, Context context) {
        this.tagMems = tagMems;
        this.listener = listener;
        this.onClickButton = onClickButton;
        this.clickCheckBox = onClickCheckBox;
        this.addNoteClick = addNoteClick;
        this.context = context;
        notifyDataSetChanged();
    }

    public void setTagMems(List<TagMemory> tagMems) {
        this.tagMems = tagMems;
        notifyDataSetChanged();
    }

    public void setListener(TagMemoryAdapter.OnTagClickListener listener) {
        this.listener = listener;
    }

    public List<TagMemory> getTagMems() {
        return tagMems;
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

//        holder.itemView.setOnClickListener(v -> listener.onTagClick(tag));
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
        private final Button addNote;
        private final ImageView tagImage;
        private final Button button;
        private final CheckBox checkBox;
        private final TagMemoryAdapter.OnTagClickListener listener;

        public TagMemViewHolder(@NonNull View itemView, TagMemoryAdapter.OnTagClickListener listener) { // Thêm listener vào constructor
            super(itemView);
            this.addNote = itemView.findViewById(R.id.addNote);
            this.listener = listener; // Gán listener cho biến instance
            this.tagImage = itemView.findViewById(R.id.image);
            this.button = itemView.findViewById(R.id.button);
            this.checkBox = itemView.findViewById(R.id.checkBox);
            this.tagName = itemView.findViewById(R.id.locationName);
        }

        public void bind(TagMemory tag) {
            tagName.setText(tag.getTagName());
            if (tag.getImage() != null) {
                Glide.with(context)
                        .load(tag.getImage())
                        .into(tagImage);
            }
            addNote.setOnClickListener(v -> {
                addNoteClick.onClickAddNote(getAdapterPosition());
            });
            checkBox.setChecked(tag.isCheck());
            button.setOnClickListener(v -> {
                // Gọi phương thức openImageChooser để chọn ảnh
                onClickButton.onClickButton(getAdapterPosition());
            });
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                clickCheckBox.onClickCheckBox(isChecked, getAdapterPosition());
            });
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
