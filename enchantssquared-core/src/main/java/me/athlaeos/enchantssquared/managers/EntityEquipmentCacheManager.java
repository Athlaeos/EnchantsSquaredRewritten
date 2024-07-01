package me.athlaeos.enchantssquared.managers;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class EntityEquipmentCacheManager {
    private static EntityEquipmentCacheManager manager = null;
    private final long cacheRefreshDelay;
    private final long cacheCleanupDelay;
    private final Map<UUID, EntityEquipment> cachedEquipment = new HashMap<>();
    private final Map<UUID, Long> lastCacheRefreshAt = new HashMap<>();
    private long lastCacheCleanupAt = System.currentTimeMillis();

    public EntityEquipmentCacheManager(){
        cacheRefreshDelay = ConfigManager.getInstance().getConfig("config.yml").get().getLong("cache_refresh_time", 2000);
        cacheCleanupDelay = ConfigManager.getInstance().getConfig("config.yml").get().getLong("cache_cleanup_delay", 600000);
    }

    public static EntityEquipmentCacheManager getInstance(){
        if (manager == null) manager = new EntityEquipmentCacheManager();
        return manager;
    }

    public void resetHands(LivingEntity entity){
        cachedEquipment.put(entity.getUniqueId(), EntityUtils.updateEnchantments(cachedEquipment.getOrDefault(entity.getUniqueId(), getAndCacheEquipment(entity)),
                entity, true, true, true));
    }

    public void resetEquipment(LivingEntity entity){
        cachedEquipment.put(entity.getUniqueId(), EntityUtils.updateEnchantments(cachedEquipment.getOrDefault(entity.getUniqueId(), getAndCacheEquipment(entity)),
                entity, true, true, true));
    }

    public void resetEnchantments(LivingEntity entity){
        cachedEquipment.put(entity.getUniqueId(), EntityUtils.updateEnchantments(cachedEquipment.getOrDefault(entity.getUniqueId(), getAndCacheEquipment(entity)),
                entity, true, false, false));
    }

    public void unCacheEquipment(LivingEntity entity) {
        cachedEquipment.remove(entity.getUniqueId());
    }

    public EntityEquipment getAndCacheEquipment(LivingEntity entity){
        cleanCache();
        if (!cachedEquipment.containsKey(entity.getUniqueId())) {
            lastCacheRefreshAt.put(entity.getUniqueId(), System.currentTimeMillis());
            cachedEquipment.put(entity.getUniqueId(), EntityUtils.getEntityEquipment(entity, true, true, true));
        } else if (lastCacheRefreshAt.containsKey(entity.getUniqueId())){
            if (lastCacheRefreshAt.get(entity.getUniqueId()) + cacheRefreshDelay < System.currentTimeMillis()){
                // last cache refresh was longer than cacheRefreshDelay milliseconds ago, refresh cached equipment
                lastCacheRefreshAt.put(entity.getUniqueId(), System.currentTimeMillis());
                cachedEquipment.put(entity.getUniqueId(), EntityUtils.getEntityEquipment(entity, true, true, true));
            }
        }
        return cachedEquipment.get(entity.getUniqueId());
    }

    public void cleanCache(){
        if (lastCacheCleanupAt + cacheCleanupDelay < System.currentTimeMillis()){
            Collection<UUID> uuids = new HashSet<>(cachedEquipment.keySet());
            uuids.forEach(u -> {
                Entity entity = EnchantsSquared.getPlugin().getServer().getEntity(u);
                if (entity != null){
                    if (!entity.isValid()){
                        cachedEquipment.remove(u);
                        lastCacheRefreshAt.remove(u);
                    }
                } else {
                    cachedEquipment.remove(u);
                    lastCacheRefreshAt.remove(u);
                }
            });
            lastCacheCleanupAt = System.currentTimeMillis();
        }
    }

    public Map<UUID, EntityEquipment> getCachedEquipment() {
        return cachedEquipment;
    }
}
