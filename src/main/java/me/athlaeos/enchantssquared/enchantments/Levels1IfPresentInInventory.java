package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Levels1IfPresentInInventory extends LevelService {
    public Levels1IfPresentInInventory(CustomEnchant customEnchant) {
        super(customEnchant);
    }

    @Override
    public int getLevel(EntityEquipment equipment) {
        int level = 0;
        if (equipment.getOwner() != null && equipment.getOwner() instanceof Player){
            for (ItemStack i : ((Player) equipment.getOwner()).getInventory().getContents()){
                if (ItemUtils.isAirOrNull(i)) continue;
                if (compatible(i)) level += CustomEnchantManager.getInstance().getEnchantStrength(i, customEnchant.getType());
            }
            if (level > 0) return 1;
        }
        for (ItemStack i : equipment.getMiscEquipment()){
            if (compatible(i)) level += equipment.getMiscEquipmentEnchantments().getOrDefault(i, new HashMap<>()).getOrDefault(customEnchant, 0);
            if (level > 0) return 1;
        }
        return level;
    }
}
