package com.project.test.model;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Memory {

    private String memoryId;

    private String memoryName;

    private String createdBy;

    private List<TagMemory> tags;

    public Memory() {
    }

    public Memory(String memoryId, String memoryName, String createdBy, List<TagMemory> tags) {
        this.memoryId = memoryId;
        this.memoryName = memoryName;
        this.createdBy = createdBy;
        this.tags = tags;
    }

    public static Memory journeyToMemory(Journey journey) {
        Memory memory = new Memory();
        memory.setMemoryId(UUID.randomUUID().toString());
        memory.setMemoryName(journey.getJourneyName());
        memory.setCreatedBy(journey.getCreatedBy());
        memory.setTags(journey.getTags().stream().map(TagMemory::tagToTagMemory).collect(Collectors.toList()));
        return memory;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public String getMemoryName() {
        return memoryName;
    }

    public void setMemoryName(String memoryName) {
        this.memoryName = memoryName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<TagMemory> getTags() {
        return tags;
    }

    public void setTags(List<TagMemory> tags) {
        this.tags = tags;
    }
}
