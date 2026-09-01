package com.moffatbaymarina.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private long reservationId;
    private long customerId;
    private long boatId;
    private long slipId;
    private LocalDate checkInDate;
    private String expectedTerm;
    private BigDecimal monthlyCost;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public Reservation() {}

    public Reservation(long reservationId, long customerId, long boatId, long slipId,
                       LocalDate checkInDate, String expectedTerm, BigDecimal monthlyCost,
                       String status, LocalDateTime createdAt, LocalDateTime cancelledAt) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.boatId = boatId;
        this.slipId = slipId;
        this.checkInDate = checkInDate;
        this.expectedTerm = expectedTerm;
        this.monthlyCost = monthlyCost;
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public long getBoatId() { return boatId; }
    public void setBoatId(long boatId) { this.boatId = boatId; }
    public long getSlipId() { return slipId; }
    public void setSlipId(long slipId) { this.slipId = slipId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public String getExpectedTerm() { return expectedTerm; }
    public void setExpectedTerm(String expectedTerm) { this.expectedTerm = expectedTerm; }
    public BigDecimal getMonthlyCost() { return monthlyCost; }
    public void setMonthlyCost(BigDecimal monthlyCost) { this.monthlyCost = monthlyCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}
