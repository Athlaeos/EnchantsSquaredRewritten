package me.athlaeos.enchantssquared.enchantments.regular_interval;

import org.bukkit.entity.Entity;

public interface TriggerOnRegularIntervalsEnchantment {
    /**
     * @return the interval between enchantment executions
     */
    long getInterval();

    /**
     * Executes the enchantment every interval defined with getInterval(), given an entity parameter
     * @param e the entity on which the enchantment is executed
     */
    void execute(Entity e, int level);
}
