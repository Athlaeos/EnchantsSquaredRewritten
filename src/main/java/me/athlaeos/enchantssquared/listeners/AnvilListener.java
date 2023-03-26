package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.AnvilCombinationResult;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {

    private final int extra_cost;
    private final String message;

    public AnvilListener(){
        extra_cost = Math.max(0, ConfigManager.getInstance().getConfig("config.yml").get().getInt("enchantment_extra_cost"));
        message = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_allowed_enchants_exceeded");
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onAnvilUse(PrepareAnvilEvent e) {
        ItemStack item1 = e.getInventory().getItem(0);
        ItemStack item2 = e.getInventory().getItem(1);
        if (ItemUtils.isAirOrNull(item1)) return;
        if (ItemUtils.isAirOrNull(item2)) return;
        HumanEntity entity = e.getViewers().isEmpty() ? null : e.getViewers().get(0);
        if (entity == null) return;
        ItemStack result = e.getResult();
        AnvilCombinationResult output = CustomEnchantManager.getInstance().combineItems(item1, item2, result, entity.getGameMode());
        AnvilInventory inventory = e.getInventory();
        switch (output.getState()){
            case SUCCESSFUL:{
                inventory.setRepairCost(inventory.getRepairCost() + extra_cost);
                if (e.getInventory().getViewers().size() > 0){
                    ((Player) e.getInventory().getViewers().get(0)).updateInventory();
                }
                e.setResult(output.getOutput());
                break;
            }
            case MAX_ENCHANTS_EXCEEDED: {
                if (e.getInventory().getViewers().size() > 0){
                    e.getInventory().getViewers().get(0).sendMessage(ChatUtils.chat(message));
                }
                e.setResult(null);
                break;
            }
            case ITEMS_NOT_COMBINEABLE: {
                e.setResult(null);
                break;
            }
        }
    }
}
