package me.athlaeos.enchantssquared.hooks.valhallammo;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.valhallammo.statsources.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EnchantmentStatSource extends AccumulativeStatSource {
    private final double base;
    private final double lv;

    public EnchantmentStatSource(CustomEnchant customEnchant, double base, double lv){
        this.base = base;
        this.lv = lv;
        this.levelService = new LevelsFromAllEquipment(customEnchant);
    }

    private final LevelService levelService;

    @Override
    public double add(Entity entity, boolean b) {
        if (entity instanceof LivingEntity){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment((LivingEntity) entity);
            int level = levelService.getLevel(equipment);
            if (level <= 0) return 0;
            return base + ((level - 1) * lv);
        }
        return 0;
    }
}
