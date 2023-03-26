package me.athlaeos.enchantssquared.domain;

public enum ExecutionPriority {
    HIGHEST(1),
    HIGH(2),
    NORMAL(3),
    LATER(4),
    LAST(5);

    private final int priority;
    ExecutionPriority(int priority){
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
