package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.PlayerOptionsManager;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantListener implements Listener {
    private final int min_enchant_level_needed;
    private final int level_minimum;
    private final boolean consume_all_levels;

    public EnchantListener(){
        level_minimum = ConfigManager.getInstance().getConfig("config.yml").get().getInt("level_minimum");
        min_enchant_level_needed = ConfigManager.getInstance().getConfig("config.yml").get().getInt("custom_enchant_rate");
        consume_all_levels = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("consume_all_levels");
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e){
        if (e.isCancelled()) return;
        Player p = e.getEnchanter();
        if (!PlayerOptionsManager.getManager().doesPlayerWantEnchants(p)) return;
        if (e.getExpLevelCost() >= level_minimum){
            int randomEnchantNumber = Utils.getRandom().nextInt(100) + 1;
            if (randomEnchantNumber <= min_enchant_level_needed){
                EnchantsSquared.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(EnchantsSquared.getPlugin(), () -> {
                    ItemStack item = e.getInventory().getItem(0);
                    CustomEnchantManager.getInstance().enchantForPlayer(item, e.getEnchanter());
                }, 1L);
            }
        }
        if (consume_all_levels){
            int levelsToRemove = e.getExpLevelCost() - (e.whichButton() + 1);
            EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () ->
                            e.getEnchanter().setLevel(e.getEnchanter().getLevel() - levelsToRemove),
                    1L);
        }
    }
}
