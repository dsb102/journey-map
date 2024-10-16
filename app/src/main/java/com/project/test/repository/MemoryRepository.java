package com.project.test.repository;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.project.test.model.Memory;
import com.project.test.model.Tag;
import com.project.test.model.TagMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryRepository {

    private final FirebaseFirestore db;

    public MemoryRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void fetchMemories(String userId, MemoryRepository.OnMemoryFetchListener listener) {
        db.collection("memories")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Memory> journeyList = task.getResult().toObjects(Memory.class);
                        Log.d("JourneyRepository.fetchJourneys", "journeyList: " + new Gson().toJson(journeyList));
                        listener.onSuccess(journeyList);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void saveMemory(Memory journey, OnMemorySaveListener listener) {
        db.collection("memories").document(journey.getMemoryId())
                .set(journey)
                .addOnSuccessListener(aVoid -> {
                    Log.d("saveMemory", "Đã save thành công");
                    listener.onSuccess();
                })
                .addOnFailureListener(aVoid -> {
                    Log.d("saveMemory", "Đã save thất bại error=" + aVoid.getMessage());
                    listener.onFailure(aVoid);
                });
    }

    public void removeTag(String journeyId, TagMemory tagToRemove) {
        db.collection("memories")
                .document(journeyId)
                .update("tags", FieldValue.arrayRemove(tagToRemove))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MemoryRepository.removeTag", "Xóa tag thành công tagId=" + tagToRemove.getTagId());
                    } else {
                        Log.e("MemoryRepository.removeTag", "Xóa tag thất bại", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MemoryRepository.removeTag", "Xóa tag thất bại", e);
                });
    }

    public void updateTagsOrder(String memId, List<TagMemory> updatedTags) {
        db.collection("memories")
                .document(memId)
                .update("tags", updatedTags)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MemoryRepository.removeTag", "Xóa tag thành công updatedTags=" + new Gson().toJson(updatedTags));
                    } else {
                        Log.e("MemoryRepository.removeTag", "Xóa tag thất bại", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MemoryRepository.removeTag", "Xóa tag thất bại", e);
                });
    }

    public void getTagForMemory(String memId, MemoryRepository.OnTagMemFetchListener onTagFetchListener) {
        db.collection("memories")
                .document(memId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<TagMemory> tags = new ArrayList<>();
                        List<Map<String, Object>> tagMaps = (List<Map<String, Object>>) task.getResult().get("tags");
                        if (tagMaps != null) {
                            for (Map<String, Object> tagMap : tagMaps) {
                                TagMemory tag = new Gson().fromJson(new Gson().toJson(tagMap), TagMemory.class);
                                tags.add(tag);
                            }
                        }
                        onTagFetchListener.onSuccess(tags);
                    } else {
                        onTagFetchListener.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("JourneyRepository.getTagForJourney", "Lỗi khi lấy tag: " + e.getMessage());
                    onTagFetchListener.onFailure(e);
                });
    }

    public interface OnMemoryFetchListener {
        void onSuccess(List<Memory> memories);
        void onFailure(Exception e);
    }

    public interface OnMemorySaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnTagMemFetchListener {
        void onSuccess(List<TagMemory> tags);
        void onFailure(Exception e);
    }
}
