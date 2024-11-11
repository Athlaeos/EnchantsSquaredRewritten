package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class GrindstoneListener implements Listener {

    private final Collection<HumanEntity> notifiedPlayers = new HashSet<>();
    private final String message;

    public GrindstoneListener(){
        message = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_grindstone_clear_all");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrindstoneUse(InventoryClickEvent e){
        if (e.getClickedInventory() instanceof GrindstoneInventory && EnchantsSquared.isGrindstonesEnabled()){
            if (!notifiedPlayers.contains(e.getWhoClicked())){
                e.getWhoClicked().sendMessage(ChatUtils.chat(message));
                notifiedPlayers.add(e.getWhoClicked());
            }

            if (e.getSlotType() == InventoryType.SlotType.RESULT){
                ItemStack item = e.getCurrentItem();
                if (item != null){
                    Map<CustomEnchant, Integer> enchantments = CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(item);
                    for (CustomEnchant enchant : new HashSet<>(enchantments.keySet())){
                        enchantments.remove(enchant);
                    }
                    CustomEnchantManager.getInstance().setItemEnchants(item, enchantments);
                }
            }
        }
    }
}
