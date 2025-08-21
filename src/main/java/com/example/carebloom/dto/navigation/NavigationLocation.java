package com.example.carebloom.dto.navigation;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationLocation {
    private double latitude;
    private double longitude;
    private String address;
    private String name; // Mother name or location identifier
    
    public NavigationLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String getLatLngString() {
        return latitude + "," + longitude;
    }
    
    public boolean hasValidCoordinates() {
        return latitude != 0.0 && longitude != 0.0 && 
               latitude >= -90 && latitude <= 90 && 
               longitude >= -180 && longitude <= 180;
    }
}
