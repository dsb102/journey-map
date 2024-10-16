package com.project.test.repository;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.project.test.model.Journey;
import com.project.test.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JourneyRepository {

    private final FirebaseFirestore db;

    public JourneyRepository() {
        this.db = FirebaseFirestore.getInstance();;
    }

    public void fetchJourneys(String userId, OnJourneyFetchListener listener) {
        db.collection("journeys")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Journey> journeyList = task.getResult().toObjects(Journey.class);
                        Log.d("JourneyRepository.fetchJourneys", "journeyList: " + new Gson().toJson(journeyList));
                        listener.onSuccess(journeyList);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void saveJourney(Journey journey, OnJourneySaveListener listener) {
        db.collection("journeys").document(journey.getJourneyID())
                .set(journey)
                .addOnSuccessListener(aVoid -> {
                    Log.d("saveJourney", "Đã save thành công");
                    listener.onSuccess();
                })
                .addOnFailureListener(aVoid -> {
                    Log.d("saveJourney", "Đã save thất bại error=" + aVoid.getMessage());
                    listener.onFailure(aVoid);
                });
    }

    public void removeTag(String journeyId, Tag tagToRemove) {
        db.collection("journeys")
                .document(journeyId)
                .update("tags", FieldValue.arrayRemove(tagToRemove))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("JourneyRepository.removeTag", "Xóa tag thành công tagId=" + tagToRemove.getTagId());
                    } else {
                        Log.e("JourneyRepository.removeTag", "Xóa tag thất bại", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("JourneyRepository.removeTag", "Xóa tag thất bại", e);
                });
    }

    public void updateTagsOrder(String journeyId, List<Tag> updatedTags) {
        db.collection("journeys")
                .document(journeyId)
                .update("tags", updatedTags)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("JourneyRepository.removeTag", "Xóa tag thành công updatedTags=" + new Gson().toJson(updatedTags));
                    } else {
                        Log.e("JourneyRepository.removeTag", "Xóa tag thất bại", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("JourneyRepository.removeTag", "Xóa tag thất bại", e);
                });
    }

//    public void updateTagsOrder(String journeyId, List<Tag> updatedTags) {
//        for (Tag tag : updatedTags) {
//            db.collection("journeys")
//                .document(journeyId)
//                .collection("tags")
//                .document(tag.getTagId()) // Lấy tag ID
//                .update("order", tag.getOrder()) // Cập nhật trường order
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("JourneyRepository.updateTagsOrder", "Cập nhật thành công");
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("JourneyRepository.updateTagsOrder", "Cập nhật thất bại", e);
//                });
//        }
//    }

    public void getTagForJourney(String journeyID, JourneyRepository.OnTagFetchListener onTagFetchListener) {
        db.collection("journeys")
                .document(journeyID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Tag> tags = new ArrayList<>();
                        List<Map<String, Object>> tagMaps = (List<Map<String, Object>>) task.getResult().get("tags");
                        if (tagMaps != null) {
                            for (Map<String, Object> tagMap : tagMaps) {
                                Tag tag = new Gson().fromJson(new Gson().toJson(tagMap), Tag.class);
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

    public interface OnJourneyFetchListener {
        void onSuccess(List<Journey> journeys);
        void onFailure(Exception e);
    }

    public interface OnJourneySaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnTagFetchListener {
        void onSuccess(List<Tag> tags);
        void onFailure(Exception e);
    }
}

