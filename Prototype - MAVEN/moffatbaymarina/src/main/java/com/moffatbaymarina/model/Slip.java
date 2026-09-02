package com.moffatbaymarina.model;

public class Slip {
    private long slipId;
    private long slipTypeId;
    private String slipNumber;
    private String status;

    public Slip() {}

    public Slip(long slipId, long slipTypeId, String slipNumber, String status) {
        this.slipId = slipId;
        this.slipTypeId = slipTypeId;
        this.slipNumber = slipNumber;
        this.status = status;
    }

    public long getSlipId() { return slipId; }
    public void setSlipId(long slipId) { this.slipId = slipId; }
    public long getSlipTypeId() { return slipTypeId; }
    public void setSlipTypeId(long slipTypeId) { this.slipTypeId = slipTypeId; }
    public String getSlipNumber() { return slipNumber; }
    public void setSlipNumber(String slipNumber) { this.slipNumber = slipNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
