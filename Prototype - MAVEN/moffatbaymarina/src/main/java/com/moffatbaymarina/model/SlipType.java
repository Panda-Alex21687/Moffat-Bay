package com.moffatbaymarina.model;

import java.math.BigDecimal;

public class SlipType {
    private long slipTypeId;
    private BigDecimal sizeFt;
    private int totalCapacity;
    private BigDecimal ratePerFoot;
    private BigDecimal electricFee;

    public SlipType() {}

    public SlipType(long slipTypeId, BigDecimal sizeFt, int totalCapacity,
                    BigDecimal ratePerFoot, BigDecimal electricFee) {
        this.slipTypeId = slipTypeId;
        this.sizeFt = sizeFt;
        this.totalCapacity = totalCapacity;
        this.ratePerFoot = ratePerFoot;
        this.electricFee = electricFee;
    }

    public long getSlipTypeId() { return slipTypeId; }
    public void setSlipTypeId(long slipTypeId) { this.slipTypeId = slipTypeId; }
    public BigDecimal getSizeFt() { return sizeFt; }
    public void setSizeFt(BigDecimal sizeFt) { this.sizeFt = sizeFt; }
    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public BigDecimal getRatePerFoot() { return ratePerFoot; }
    public void setRatePerFoot(BigDecimal ratePerFoot) { this.ratePerFoot = ratePerFoot; }
    public BigDecimal getElectricFee() { return electricFee; }
    public void setElectricFee(BigDecimal electricFee) { this.electricFee = electricFee; }
}
