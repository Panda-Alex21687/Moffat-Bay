package com.moffatbaymarina.model;

import java.time.LocalDateTime;

public class WaitlistEntry {
    private long waitlistId;
    private long customerId;
    private long boatId;
    private long slipTypeId;
    private LocalDateTime joinedAt;
    private String status;

    public WaitlistEntry() {}

    public WaitlistEntry(long waitlistId, long customerId, long boatId,
                         long slipTypeId, LocalDateTime joinedAt, String status) {
        this.waitlistId = waitlistId;
        this.customerId = customerId;
        this.boatId = boatId;
        this.slipTypeId = slipTypeId;
        this.joinedAt = joinedAt;
        this.status = status;
    }

    public long getWaitlistId() { return waitlistId; }
    public void setWaitlistId(long waitlistId) { this.waitlistId = waitlistId; }
    public long getCustomerId() { return customerId; }
    public void setCustomerId(long customerId) { this.customerId = customerId; }
    public long getBoatId() { return boatId; }
    public void setBoatId(long boatId) { this.boatId = boatId; }
    public long getSlipTypeId() { return slipTypeId; }
    public void setSlipTypeId(long slipTypeId) { this.slipTypeId = slipTypeId; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
