package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerFishingLootListener implements Listener {
    private final double bookCustomEnchantChance;
    private final int bookCustomEnchantRolls;
    public PlayerFishingLootListener(){
        bookCustomEnchantChance = ConfigManager.getInstance().getConfig("config.yml").get().getDouble("custom_enchant_fish_rate");
        bookCustomEnchantRolls = Math.max(1, ConfigManager.getInstance().getConfig("config.yml").get().getInt("custom_enchant_fish_rolls"));
    }

    @EventHandler
    public void onFish(PlayerFishEvent e){
        Entity entity = e.getCaught();
        if (entity != null){
            if (entity instanceof Item){
                CustomEnchantManager manager = CustomEnchantManager.getInstance();
                ItemStack caughtItem = ((Item) entity).getItemStack();
                if (caughtItem.getType() == Material.ENCHANTED_BOOK){
                    if (Utils.getRandom().nextDouble() * 100 <= bookCustomEnchantChance){
                        manager.setItemEnchants(caughtItem, manager.getRandomEnchantments(caughtItem, e.getPlayer(), bookCustomEnchantRolls, true, CustomEnchantManager.getInstance().getCompatibleEnchants(caughtItem, GameMode.SURVIVAL)));
                    }
                }
            }
        }
    }
}
