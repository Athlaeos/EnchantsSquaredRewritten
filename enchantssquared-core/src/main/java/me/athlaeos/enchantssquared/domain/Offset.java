package me.athlaeos.enchantssquared.domain;

public class Offset {
    private final int offX;
    private final int offY;
    private final int offZ;

    public Offset(int offX, int offY, int offZ){
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
    }

    public int getOffX() {
        return offX;
    }

    public int getOffY() {
        return offY;
    }

    public int getOffZ() {
        return offZ;
    }
}
