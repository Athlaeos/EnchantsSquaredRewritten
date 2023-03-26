package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_fishing.TriggerOnFishingEnchantment;
import me.athlaeos.enchantssquared.hooks.WorldGuardHook;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerFishListener implements Listener {
    private final double bookCustomEnchantChance;
    private final int bookCustomEnchantRolls;
    public PlayerFishListener(){
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


        Player fisher = e.getPlayer();

        if (EnchantsSquared.isWorldGuardAllowed(fisher, e.getHook().getLocation(), "es-deny-all")){
            EntityEquipment equipment = EntityUtils.getEntityEquipment(fisher);

            boolean offHand = fisher.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD && fisher.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD;

            for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnFishingEnchantment)){
                ((TriggerOnFishingEnchantment) enchantment).onFish(e, enchantment.getLevelService(offHand, fisher).getLevel(equipment));
            }
        }
    }
}
