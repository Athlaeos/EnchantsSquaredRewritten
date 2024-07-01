package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;

public class LevelsFromOffHandOnly extends LevelService {
    public LevelsFromOffHandOnly(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        if (!compatible(equipment.getOffHand())) return 0;
        return equipment.getOffHandEnchantments().getOrDefault(customEnchant, 0);
    }
}
