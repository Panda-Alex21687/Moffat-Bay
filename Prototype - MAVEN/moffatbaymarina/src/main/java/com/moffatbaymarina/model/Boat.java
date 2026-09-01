package com.moffatbaymarina.model;

/**
 * Represents a boat registered to a Moffat Bay Marina customer.
 *
 * This class maps to the boats table in the database.
 */
public class Boat {

    private long boatId;
    private long customerId;
    private String boatName;
    private double boatLength;
    private String boatType;
    private String registrationNumber;

    /**
     * Required by JavaBean/Jackson conventions.
     */
    public Boat() {
    }

    /**
     * Constructor used before the boat has been inserted into the database.
     */
    public Boat(long customerId,
            String boatName,
            double boatLength,
            String boatType,
            String registrationNumber) {
        this.customerId = customerId;
        this.boatName = boatName;
        this.boatLength = boatLength;
        this.boatType = boatType;
        this.registrationNumber = registrationNumber;
    }

    /**
     * Constructor used when loading a complete boat from the database.
     */
    public Boat(long boatId,
            long customerId,
            String boatName,
            double boatLength,
            String boatType,
            String registrationNumber) {
        this.boatId = boatId;
        this.customerId = customerId;
        this.boatName = boatName;
        this.boatLength = boatLength;
        this.boatType = boatType;
        this.registrationNumber = registrationNumber;
    }

    public long getBoatId() {
        return boatId;
    }

    public void setBoatId(long boatId) {
        this.boatId = boatId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getBoatName() {
        return boatName;
    }

    public void setBoatName(String boatName) {
        this.boatName = boatName;
    }

    public double getBoatLength() {
        return boatLength;
    }

    public void setBoatLength(double boatLength) {
        this.boatLength = boatLength;
    }

    public String getBoatType() {
        return boatType;
    }

    public void setBoatType(String boatType) {
        this.boatType = boatType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @Override
    public String toString() {
        return "Boat{" +
                "boatId=" + boatId +
                ", customerId=" + customerId +
                ", boatName='" + boatName + '\'' +
                ", boatLength=" + boatLength +
                ", boatType='" + boatType + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                '}';
    }
}
