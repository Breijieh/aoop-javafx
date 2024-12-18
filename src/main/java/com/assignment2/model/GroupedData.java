package com.assignment2.model;

public class GroupedData {
    private final String groupKey;
    private final Long count;

    public GroupedData(String groupKey, Long count) {
        this.groupKey = groupKey;
        this.count = count;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public Long getCount() {
        return count;
    }
}
