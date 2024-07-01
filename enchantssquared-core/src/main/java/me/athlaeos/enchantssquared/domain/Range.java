package me.athlaeos.enchantssquared.domain;

public class Range {
    private final int min;
    private final int max;

    public Range(int min, int max){
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean isInRange(int i){
        return i <= max && i >= min;
    }
}
