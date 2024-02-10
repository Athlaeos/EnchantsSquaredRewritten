package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorSwitchListener implements Listener {
    private static final Map<UUID, DelayedArmorUpdate> taskLimiters = new HashMap<>();

    private void updateArmor(Player who){
        DelayedArmorUpdate update = taskLimiters.get(who.getUniqueId());
        if (update != null) update.refresh();
        else {
            update = new DelayedArmorUpdate(who);
            update.runTaskTimer(EnchantsSquared.getPlugin(), 1L, 1L);
        }
        taskLimiters.put(who.getUniqueId(), update);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryEquip(InventoryClickEvent e){
        if (e.isCancelled() || e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER || e.getClickedInventory().getType() == InventoryType.CREATIVE){
            if (e.getSlotType() == InventoryType.SlotType.ARMOR){
                if (ItemUtils.isAirOrNull(e.getCurrentItem()) && ItemUtils.isAirOrNull(e.getCursor())) return; // both clicked slot and cursor are empty, no need to do anything
                // cursor or clicked item are empty, but not both. items are switched and therefore armor may be updated
                updateArmor((Player) e.getWhoClicked());
            } else if (e.getClick().isShiftClick() && !ItemUtils.isAirOrNull(e.getCurrentItem())){
                ItemStack clicked = e.getCurrentItem();
                ItemStack armor;
                if (clicked.getType().toString().contains("_HELMET")) armor = e.getWhoClicked().getInventory().getItem(EquipmentSlot.HEAD);
                else if (clicked.getType().toString().contains("_CHESTPLATE")) armor = e.getWhoClicked().getInventory().getItem(EquipmentSlot.CHEST);
                else if (clicked.getType().toString().contains("_LEGGINGS")) armor = e.getWhoClicked().getInventory().getItem(EquipmentSlot.LEGS);
                else if (clicked.getType().toString().contains("_BOOTS")) armor = e.getWhoClicked().getInventory().getItem(EquipmentSlot.FEET);
                else armor = null;
                if (ItemUtils.isAirOrNull(armor)){
                    // armor slot fitting for the clicked item is empty, update equipment
                    updateArmor((Player) e.getWhoClicked());
                } // armor slot is occupied, do not update
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDispenserEquip(BlockDispenseArmorEvent e){
        if (e.isCancelled()) return;
        if (e.getTargetEntity() instanceof Player){
            // armor equipped through dispenser, update equipment
            updateArmor((Player) e.getTargetEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandEquip(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY || ItemUtils.isAirOrNull(e.getItem()) || !e.getItem().getType().isItem()) return;
        ItemStack clicked = e.getItem();
        if (clicked.getType().toString().contains("_HELMET") || clicked.getType().toString().contains("_CHESTPLATE") ||
                clicked.getType().toString().contains("_LEGGINGS") || clicked.getType().toString().contains("_BOOTS")) {
            // clicked item isn't armor, and so it can be assumed no equipment is equipped
            // armor was clicked and might be swapped out, update equipment
            updateArmor(e.getPlayer());
        }
    }

    private static class DelayedArmorUpdate extends BukkitRunnable {
        private int timer = 30; // after 1.5 seconds update equipment
        private final Player who;

        public DelayedArmorUpdate(Player who){
            this.who = who;
        }

        @Override
        public void run() {
            if (timer <= 0){
                EntityEquipmentCacheManager.getInstance().resetEquipment(who);
                taskLimiters.remove(who.getUniqueId());
                cancel();
            } else {
                timer--;
            }
        }

        public void refresh(){
            timer = 30;
        }
    }
}