package com.project.test.model;

import java.util.List;

public class Journey {
    private String journeyID;
    private String journeyName;
    private String createdBy;
    private List<Tag> tags;

    public Journey() {}

    public Journey(String journeyID, String journeyName, String createdBy, List<Tag> tags) {
        this.journeyID = journeyID;
        this.journeyName = journeyName;
        this.createdBy = createdBy;
        this.tags = tags;
    }

    public String getJourneyName() {
        return journeyName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getJourneyID() {
        return journeyID;
    }

    public void setJourneyID(String journeyID) {
        this.journeyID = journeyID;
    }

    public void setJourneyName(String journeyName) {
        this.journeyName = journeyName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
