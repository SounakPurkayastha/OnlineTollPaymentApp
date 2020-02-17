package com.example.myapplication;

public class Model {

    private String vehicleId, vehicleType, key;

    public Model(String vehicleId, String vehicleType, String key) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.key = key;
    }

    public Model() {
    }

    public String getKey() {
        return key;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

}
