package com.mmcrajawali.smartbin;

/**
 * Created by Bambang on 6/25/2016.
 */
public class Task {
    private String location_name;

    public Task() {
    }

    public Task(String name) {
        location_name = name;
    }

    public String getLocationName() {
        return location_name;
    }

    public void setLocationName(String name) {
        location_name = name;
    }

}
