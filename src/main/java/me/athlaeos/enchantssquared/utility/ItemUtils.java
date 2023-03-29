package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.valhallammo.managers.CustomDurabilityManager;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class ItemUtils {
    public static boolean isAirOrNull(ItemStack i){
        return i == null || i.getType().toString().contains("AIR");
    }

    public static Collection<Material> getMaterialList(Collection<String> materials){
        Collection<Material> m = new HashSet<>();
        if (materials == null) return m;
        for (String s : materials){
            try {
                m.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored){
            }
        }
        return m;
    }

    public static ItemStack getIconFromConfig(YamlConfiguration config, String path, ItemStack def){
        Object rawIcon = config.get(path);
        if (rawIcon instanceof ItemStack){
            return (ItemStack) rawIcon;
        } else {
            try {
                def.setType(Material.valueOf(config.getString(path)));
                return def;
            } catch (IllegalArgumentException ignored){
                EnchantsSquared.getPlugin().getServer().getLogger().warning(
                        "ItemStack in config " + config.getName() + ", " + path + " did not lead to an itemstack or proper material type. Defaulted to " + getItemName(def)
                );
            }
        }
        return def;
    }

    public static String getItemName(ItemStack i){
        String name = "null";
        if (i.getItemMeta() != null){
            if (i.getItemMeta().hasDisplayName()){
                name = ChatUtils.chat(i.getItemMeta().getDisplayName());
            } else if (i.getItemMeta().hasLocalizedName()){
                name = ChatUtils.chat(i.getItemMeta().getLocalizedName());
            } else {
                name = i.getType().toString().toLowerCase().replace("_", " ");
            }
        }
        return name;
    }


    public static Map<Integer, ArrayList<ItemStack>> paginateItemStackList(int pageSize, List<ItemStack> allEntries) {
        Map<Integer, ArrayList<ItemStack>> pages = new HashMap<>();
        int stepper = 0;

        for (int pageNumber = 0; pageNumber < Math.ceil((double)allEntries.size()/(double)pageSize); pageNumber++) {
            ArrayList<ItemStack> pageEntries = new ArrayList<>();
            for (int pageEntry = 0; pageEntry < pageSize && stepper < allEntries.size(); pageEntry++, stepper++) {
                pageEntries.add(allEntries.get(stepper));
            }
            pages.put(pageNumber, pageEntries);
        }
        return pages;
    }

    public static boolean damageItem(Player damager, ItemStack i, int damage, EquipmentSlot slot){
        if (i.getItemMeta() instanceof Damageable && i.getType().getMaxDurability() > 0){
            if (damage > 0 && i.getItemMeta().isUnbreakable()) return false;
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(damager, i, damage);
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);

            if (EnchantsSquared.isValhallaHooked()) {
                // if ValhallaMMO is active, it handles custom durability itself
                if (CustomDurabilityManager.getInstance().hasCustomDurability(i)) return false;
            }
            if (!event.isCancelled()){
                Damageable toolMeta = (Damageable) i.getItemMeta();
                if (toolMeta == null) return false;
                toolMeta.setDamage(toolMeta.getDamage() + event.getDamage());
                if (toolMeta.getDamage() >= i.getType().getMaxDurability()) {
                    switch(slot){
                        case HAND: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            damager.getInventory().setItemInMainHand(null);
                        }
                        case OFF_HAND: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_OFF_HAND);
                            damager.getInventory().setItemInOffHand(null);
                        }
                        case FEET: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_BOOTS);
                            damager.getInventory().setBoots(null);
                        }
                        case LEGS: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_LEGGINGS);
                            damager.getInventory().setLeggings(null);
                        }
                        case CHEST: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_CHESTPLATE);
                            damager.getInventory().setChestplate(null);
                        }
                        case HEAD: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_HELMET);
                            damager.getInventory().setHelmet(null);
                        }
                    }
                    EntityEquipmentCacheManager.getInstance().unCacheEquipment(damager);
                    return true;
                }
                i.setItemMeta(toolMeta);
            }
        }
        return false;
    }

    public static void damageItem(Player damager, ItemStack i, int damage){
        damageItem(damager, i, damage, null);
    }

    public static void addItem(Player player, ItemStack i, boolean setOwnership){
        Map<Integer, ItemStack> excess = player.getInventory().addItem(i);
        if (!excess.isEmpty()){
            for (Integer slot : excess.keySet()){
                ItemStack slotItem = excess.get(slot);
                Item drop = player.getWorld().dropItem(player.getLocation(), slotItem);
                if (setOwnership) drop.setOwner(player.getUniqueId());
            }
        }
    }
}
