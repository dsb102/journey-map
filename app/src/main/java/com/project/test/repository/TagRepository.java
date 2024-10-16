package com.project.test.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class TagRepository {

    private final FirebaseFirestore db;

    public TagRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getTags(String dungsobin103, Object mapsActivity) {
        db.collection("tags")
                .whereEqualTo("createdBy", dungsobin103)
                .addSnapshotListener((command, a) -> {

                });
    }

    public interface OnLoadSuccessListener {
        void onSuccess(Object o);
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
