package me.athlaeos.enchantssquared.managers;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.regular_interval.TriggerOnRegularIntervalsEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegularIntervalEnchantmentClockManager {

    private static Map<Long, Collection<CustomEnchant>> enchantmentsPerClock;
    private static final Collection<BukkitTask> runningTasks = new HashSet<>();

    public static void startClock(){
        enchantmentsPerClock = new HashMap<>();
        runningTasks.forEach(BukkitTask::cancel);
        runningTasks.clear();
        Collection<CustomEnchant> triggerOnRegularIntervalEnchantments = CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnRegularIntervalsEnchantment);
        for (CustomEnchant e : triggerOnRegularIntervalEnchantments){
            TriggerOnRegularIntervalsEnchantment t = (TriggerOnRegularIntervalsEnchantment) e;
            Collection<CustomEnchant> currentCollection = enchantmentsPerClock.getOrDefault(t.getInterval(), new HashSet<>());
            currentCollection.add(e);
            enchantmentsPerClock.put(t.getInterval(), currentCollection);
        }

        for (Long l : enchantmentsPerClock.keySet()){
            runningTasks.add(EnchantsSquared.getPlugin().getServer().getScheduler().runTaskTimer(EnchantsSquared.getPlugin(), () -> {
                for (Player p : EnchantsSquared.getPlugin().getServer().getOnlinePlayers()){
                    EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p);
                    enchantmentsPerClock.get(l).forEach(e -> ((TriggerOnRegularIntervalsEnchantment) e).execute(p, e.getLevelService(false, p).getLevel(cachedEquipment)));
                }
            }, 0L, l));
        }
    }
}
