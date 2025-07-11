package com.example.carebloom.dto;

public class UpdateMidwifeRequest {
    private String name;
    private String phone;
    private String registrationNumber;
    private String state;

    // Default constructor
    public UpdateMidwifeRequest() {
    }

    // Constructor with parameters
    public UpdateMidwifeRequest(String name, String phone, String registrationNumber, String state) {
        this.name = name;
        this.phone = phone;
        this.registrationNumber = registrationNumber;
        this.state = state;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "UpdateMidwifeRequest{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

}
