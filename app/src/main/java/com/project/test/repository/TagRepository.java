package com.project.test.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagRepository {

    private final FirebaseFirestore db;

    public TagRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getTags(String username, OnLoadSuccessListener listener) {
        db.collection("tags")
                .whereEqualTo("createdBy", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> tags = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> tag = document.getData();
                            tags.add(tag); // Thêm vào danh sách
                        }

                        listener.onSuccess(tags);
                    } else {
                        // Xử lý lỗi
                        Log.d("TagRepository", "Error getting documents: ", task.getException());
                    }
                });
    }

    public interface OnLoadSuccessListener {
        void onSuccess(List<Map<String, Object>> o);
    }

    public interface OnTagSavedListener {
        void onSuccess();
        void onError(String message);
    }

    public void saveTag(Map<String, Object> tag, OnTagSavedListener listener) {
        db.collection("tags").add(tag)
                .addOnSuccessListener(documentReference -> {
                    // Change marker color based on tag color and display tag info
                    listener.onSuccess();
                })
                .addOnFailureListener(command -> listener.onError(command.getMessage()));
    }
}
