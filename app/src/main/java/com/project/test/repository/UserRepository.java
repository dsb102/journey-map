package com.project.test.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.project.test.model.Journey;
import com.project.test.model.Tag;
import com.project.test.model.UserLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();;
    }

    public void updateLocation(String username, double v, double v1) {
        db.collection("users").document(username)
                .update("latitude", v, "longitude", v1)
                .addOnSuccessListener(aVoid -> {
                    Log.d("updateLocation", "Đã cập nhật vị trí thành công");
                })
                .addOnFailureListener(aVoid -> {
                    Log.d("updateLocation", "Cập nhật vị trí thất bại error=" + aVoid.getMessage());
                });
    }

    public interface UserLocationCallback {
        void onCallback(List<UserLocation> userLocations);
    }

    public void getLocationUsers(String username, UserLocationCallback listener) {
        List<UserLocation> userLocations = new ArrayList<>();
        db.collection("users").document(username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> mapUser  = task.getResult().getData();
                        Log.d("getLocationUsers", new Gson().toJson(mapUser));
                        List<String> friendNames = (List<String>) mapUser.get("friends");
                        Log.d("getLocationUsers", "friendNames: " + new Gson().toJson(friendNames));
                        if (friendNames == null || friendNames.isEmpty()) {
                            listener.onCallback(userLocations);
                            return;
                        }
                        final int totalRequests = friendNames.size();
                        final AtomicInteger completedRequests = new AtomicInteger(0);
                        for (String friendName : friendNames) {
                            db.collection("users").document(friendName)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful() && task1.getResult() != null) {
                                            Map<String, Object> mapFriend = task1.getResult().getData();
                                            Log.d("getLocationUsers", "mapFriend: " +  new Gson().toJson(mapFriend));
                                            UserLocation userLocation = new UserLocation();
                                            userLocation.setName((String) mapFriend.getOrDefault("name", friendName));
                                            userLocation.setLatitude((double) mapFriend.getOrDefault("latitude", 0.0));
                                            userLocation.setLongitude((double) mapFriend.getOrDefault("longitude", 0.0));
                                            if (userLocation.getLatitude() != 0 && userLocation.getLongitude() != 0) {
                                                userLocations.add(userLocation);
                                            }
                                        }
                                        if (completedRequests.incrementAndGet() == totalRequests) {
                                            // Nếu tất cả yêu cầu đã hoàn thành, gọi callback
                                            Log.d("getLocationUsers", "userLocations: " + new Gson().toJson(userLocations));
                                            listener.onCallback(userLocations);
                                        }
                                    });
                        }
                        Log.d("getLocationUsers", "userLocations: " + new Gson().toJson(userLocations));
                    }
                });
    }

    public void fetchJourneys(String userId, JourneyRepository.OnJourneyFetchListener listener) {
        db.collection("journeys")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Journey> journeyList = task.getResult().toObjects(Journey.class);
                        listener.onSuccess(journeyList);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void saveJourney(Journey journey, JourneyRepository.OnJourneySaveListener listener) {
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

    public void updateTagsOrder(String journeyId, List<Tag> updatedTags) {
        // Lấy ID của journey tương ứng với tags
//        String journeyId = getJourneyId(); // Lấy journeyId từ repository

        // Cập nhật Firestore
        for (Tag tag : updatedTags) {
            db.collection("journeys")
                    .document(journeyId)
                    .collection("tags")
                    .document(tag.getTagId()) // Lấy tag ID
                    .update("order", tag.getOrder()) // Cập nhật trường order
                    .addOnSuccessListener(aVoid -> {
                        // Xử lý khi cập nhật thành công
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi xảy ra lỗi
                    });
        }
    }

    public interface OnJourneyFetchListener {
        void onSuccess(List<Journey> journeys);
        void onFailure(Exception e);
    }

    public interface OnJourneySaveListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
