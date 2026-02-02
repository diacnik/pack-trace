package com.packtrace.model;

public class ClosetGear {
    private Long closetId;
    private Long gearId;
    private int quantity;

    public ClosetGear() {
    }

    public ClosetGear(Long closetId, Long gearId, int quantity) {
        this.closetId = closetId;
        this.gearId = gearId;
        this.quantity = quantity;
    }

    public Long getClosetId() {
        return closetId;
    }

    public void setClosetId(Long closetId) {
        this.closetId = closetId;
    }

    public Long getGearId() {
        return gearId;
    }

    public void setGearId(Long gearId) {
        this.gearId = gearId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
