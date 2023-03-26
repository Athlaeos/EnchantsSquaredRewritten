package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class GrindstoneListener implements Listener {

    private final Collection<HumanEntity> notifiedPlayers = new HashSet<>();
    private final String message;

    public GrindstoneListener(){
        message = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_grindstone_clear_all");
    }

    @EventHandler
    public void onGrindstoneUse(InventoryClickEvent e){
        if (e.getClickedInventory() instanceof GrindstoneInventory){
            if (!notifiedPlayers.contains(e.getWhoClicked())){
                e.getWhoClicked().sendMessage(ChatUtils.chat(message));
                notifiedPlayers.add(e.getWhoClicked());
            }

            if (e.getSlotType() == InventoryType.SlotType.RESULT){
                ItemStack item = e.getCurrentItem();
                if (item != null){
                    ItemStack result = CustomEnchantManager.getInstance().removeAllEnchants(item);
                    if (result != null) {
                        e.setCurrentItem(result);
                    }
                }
            }
        }
    }
}
