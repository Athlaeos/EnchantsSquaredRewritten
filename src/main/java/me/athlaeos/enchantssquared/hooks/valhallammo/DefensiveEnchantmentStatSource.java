package me.athlaeos.enchantssquared.hooks.valhallammo;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class DefensiveEnchantmentStatSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final double base;
    private final double lv;

    public DefensiveEnchantmentStatSource(CustomEnchant customEnchant, double base, double lv){
        this.base = base;
        this.lv = lv;
        this.levelService = new LevelsFromAllEquipment(customEnchant);
    }

    private final LevelService levelService;

    @Override
    public double fetch(Entity entity, boolean b) {
        if (entity instanceof LivingEntity){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment((LivingEntity) entity);
            int level = levelService.getLevel(equipment);
            if (level <= 0) return 0;
            return base + ((level - 1) * lv);
        }
        return 0;
    }

    @Override
    public double fetch(Entity entity, Entity entity1, boolean b) {
        if (entity instanceof LivingEntity){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment((LivingEntity) entity);
            int level = levelService.getLevel(equipment);
            if (level <= 0) return 0;
            return base + ((level - 1) * lv);
        }
        return 0;
    }
}
