package com.packtrace.model;

public class PackGear {
    private Long packId;
    private Long gearId;
    private int quantity;

    public PackGear() {
    }

    public PackGear(Long packId, Long gearId, int quantity) {
        this.packId = packId;
        this.gearId = gearId;
        this.quantity = quantity;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
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
