package com.nothing.firestoreup.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DetectionResponse {

    @SerializedName("inference_id")
    private String inferenceId;

    @SerializedName("predictions")
    private List<Prediction> predictions;

    public String getInferenceId() {
        return inferenceId;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public static class Prediction {
        @SerializedName("class")
        private String className;

        public String getClassName() {
            return className;
        }
    }
}