package com.moffatbaymarina.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a Moffat Bay Marina slip reservation.
 *
 * This class maps primarily to the reservations table. The email,
 * boatName, and boatLength properties are included because reservation
 * lookup queries join the customers and boats tables and return those
 * values with a reservation.
 */
public class Reservation {

    private String reservationId;
    private long customerId;
    private long boatId;

    private String email;
    private String boatName;
    private double boatLength;

    private int slipSize;
    private String slipNumber;
    private LocalDate checkIn;
    private double monthlyCost;
    private String status;

    private Instant createdAt;
    private Instant cancelledAt;

    /**
     * Required by JavaBean/Jackson conventions.
     */
    public Reservation() {
    }

    /**
     * Constructor used when creating a new reservation.
     */
    public Reservation(String reservationId,
            long customerId,
            long boatId,
            int slipSize,
            String slipNumber,
            LocalDate checkIn,
            double monthlyCost,
            String status) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.boatId = boatId;
        this.slipSize = slipSize;
        this.slipNumber = slipNumber;
        this.checkIn = checkIn;
        this.monthlyCost = monthlyCost;
        this.status = status;
    }

    /**
     * Constructor used when loading a joined reservation result from MySQL.
     */
    public Reservation(String reservationId,
            long customerId,
            long boatId,
            String email,
            String boatName,
            double boatLength,
            int slipSize,
            String slipNumber,
            LocalDate checkIn,
            double monthlyCost,
            String status,
            Instant createdAt,
            Instant cancelledAt) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.boatId = boatId;
        this.email = email;
        this.boatName = boatName;
        this.boatLength = boatLength;
        this.slipSize = slipSize;
        this.slipNumber = slipNumber;
        this.checkIn = checkIn;
        this.monthlyCost = monthlyCost;
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getBoatId() {
        return boatId;
    }

    public void setBoatId(long boatId) {
        this.boatId = boatId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public int getSlipSize() {
        return slipSize;
    }

    public void setSlipSize(int slipSize) {
        this.slipSize = slipSize;
    }

    public String getSlipNumber() {
        return slipNumber;
    }

    public void setSlipNumber(String slipNumber) {
        this.slipNumber = slipNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(double monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isConfirmed() {
        return "Confirmed".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "Cancelled".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId='" + reservationId + '\'' +
                ", customerId=" + customerId +
                ", boatId=" + boatId +
                ", boatName='" + boatName + '\'' +
                ", boatLength=" + boatLength +
                ", slipSize=" + slipSize +
                ", slipNumber='" + slipNumber + '\'' +
                ", checkIn=" + checkIn +
                ", monthlyCost=" + monthlyCost +
                ", status='" + status + '\'' +
                '}';
    }
}
