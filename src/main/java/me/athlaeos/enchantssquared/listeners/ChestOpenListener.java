package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ChestOpenListener implements Listener {
    private final double chestCustomEnchantChance;
    private final int chestCustomEnchantRolls;

    public ChestOpenListener(){
        chestCustomEnchantChance = ConfigManager.getInstance().getConfig("config.yml").get().getDouble("custom_enchant_fish_rate");
        chestCustomEnchantRolls = Math.max(1, ConfigManager.getInstance().getConfig("config.yml").get().getInt("custom_enchant_fish_rolls"));
    }

    @EventHandler
    public void onPlayerOpenChest(InventoryOpenEvent e){
        if (!e.isCancelled()) {
            if (e.getInventory().getType() == InventoryType.CHEST && e.getInventory().getHolder() instanceof Chest){
                Chest chest = (Chest) e.getInventory().getHolder();
                if (chest.getLootTable() != null){
                    EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
                        for (ItemStack i : chest.getInventory().getStorageContents()){
                            if (ItemUtils.isAirOrNull(i)) continue;
                            if (i.getType() == Material.ENCHANTED_BOOK || MaterialClassType.getClass(i.getType()) != null){
                                if (Utils.getRandom().nextDouble() * 100 <= chestCustomEnchantChance){
                                    CustomEnchantManager.getInstance().setItemEnchants(i,
                                            CustomEnchantManager.getInstance().getRandomEnchantments(i, (Player) e.getPlayer(), chestCustomEnchantRolls, true, CustomEnchantManager.getInstance().getCompatibleEnchants(i, GameMode.SURVIVAL)));
                                }
                            }
                        }
                    }, 1L);
                }
            }
        }
    }
}
