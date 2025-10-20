package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "childRecord")
public class ChildRecord {
    @Id
    private String id;

    private String motherId;
    private String name;
    private String gender;
    private String dateOfBirth;
    private String bloodType;
    private String birthWeight;
    private String birthHeight;
    private String birthHeadCircumference;
    private String birthNotes;
    private Object vaccineSchedule; // keep flexible; map/array from frontend
    private Object growthRecords;

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getBirthWeight() { return birthWeight; }
    public void setBirthWeight(String birthWeight) { this.birthWeight = birthWeight; }

    public String getBirthHeight() { return birthHeight; }
    public void setBirthHeight(String birthHeight) { this.birthHeight = birthHeight; }

    public String getBirthHeadCircumference() { return birthHeadCircumference; }
    public void setBirthHeadCircumference(String birthHeadCircumference) { this.birthHeadCircumference = birthHeadCircumference; }

    public String getBirthNotes() { return birthNotes; }
    public void setBirthNotes(String birthNotes) { this.birthNotes = birthNotes; }

    public Object getVaccineSchedule() { return vaccineSchedule; }
    public void setVaccineSchedule(Object vaccineSchedule) { this.vaccineSchedule = vaccineSchedule; }

    public Object getGrowthRecords() { return growthRecords; }
    public void setGrowthRecords(Object growthRecords) { this.growthRecords = growthRecords; }
}
