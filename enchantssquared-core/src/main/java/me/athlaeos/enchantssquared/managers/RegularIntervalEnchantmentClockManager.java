package me.athlaeos.enchantssquared.managers;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.regular_interval.TriggerOnRegularIntervalsEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RegularIntervalEnchantmentClockManager {

    private static Map<Long, Collection<CustomEnchant>> enchantmentsPerClock;
    private static final Collection<BukkitTask> runningTasks = new HashSet<>();
    private static final Collection<UUID> playersWithRunningEnchantments = new HashSet<>();

    public static void includePlayerIntoClock(UUID uuid){
        playersWithRunningEnchantments.add(uuid);
    }

    public static void excludePlayerFromClock(UUID uuid){
        playersWithRunningEnchantments.remove(uuid);
    }

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
                for (UUID uuid : new HashSet<>(playersWithRunningEnchantments)){
                    Player p = EnchantsSquared.getPlugin().getServer().getPlayer(uuid);
                    if (p == null) { // player is offline, remove from set
                        playersWithRunningEnchantments.remove(uuid);
                        continue;
                    }
                    EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p);
                    enchantmentsPerClock.get(l).forEach(e -> ((TriggerOnRegularIntervalsEnchantment) e).execute(p, e.getLevelService(false, p).getLevel(cachedEquipment)));
                }
            }, 0L, l));
        }
    }
}
