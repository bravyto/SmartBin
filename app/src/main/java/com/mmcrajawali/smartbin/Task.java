package com.mmcrajawali.smartbin;

/**
 * Created by Bambang on 6/25/2016.
 */
public class Task {
    private String driver_id;
    private String location_name;
    private String location_id;
    private String location_type;
    private Double location_latitude;
    private Double location_longitude;

    public Task() {
    }

    public Task(String driver_id, String id, String name, String type, Double latitude, Double longitude) {
        this.driver_id = driver_id;
        location_id = id;
        location_name = name;
        location_type = type;
        location_latitude = latitude;
        location_longitude = longitude;
    }

    public String getLocation_type() { return location_type; }

    public String getDriver_id() { return driver_id; }

    public String getLocationName() {
        return location_name;
    }

    public String getLocationId() { return location_id; }

    public Double getLocation_latitude() { return location_latitude; }

    public Double getLocation_longitude() { return location_longitude; }

    public void setLocationName(String name) {
        location_name = name;
    }

}
