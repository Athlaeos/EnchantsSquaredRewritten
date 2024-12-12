package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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
        ItemMeta meta = i.getItemMeta();
        if (meta instanceof Damageable && i.getType().getMaxDurability() > 0){
            Damageable toolMeta = (Damageable) meta;
            if (damage > 0 && i.getItemMeta().isUnbreakable()) return false;
            boolean cancelled = false;
            if (damage > 0){
                PlayerItemDamageEvent event = new PlayerItemDamageEvent(damager, i, damage);
                EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                damage = event.getDamage();
            }

            if (EnchantsSquared.isValhallaHooked()) {
                toolMeta = (Damageable) me.athlaeos.valhallammo.utility.ItemUtils.getItemMeta(i);
                // if ValhallaMMO is active, it handles custom durability itself
                if (CustomDurabilityManager.hasCustomDurability(toolMeta)){
                    if (damage > 0) return false;
                    else if (damage < 0) {
                        CustomDurabilityManager.damage(toolMeta, damage);
                        me.athlaeos.valhallammo.utility.ItemUtils.setItemMeta(i, toolMeta);
                        return false;
                    }
                }
            }
            if (!cancelled){
                toolMeta.setDamage(Math.max(0, toolMeta.getDamage() + damage));
                if (slot != null && toolMeta.getDamage() >= i.getType().getMaxDurability() && i.getType() != Material.ELYTRA) {
                    switch(slot){
                        case HAND: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                            damager.getInventory().setItemInMainHand(null);
                            break;
                        }
                        case OFF_HAND: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_OFF_HAND);
                            damager.getInventory().setItemInOffHand(null);
                            break;
                        }
                        case FEET: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_BOOTS);
                            damager.getInventory().setBoots(null);
                            break;
                        }
                        case LEGS: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_LEGGINGS);
                            damager.getInventory().setLeggings(null);
                            break;
                        }
                        case CHEST: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_CHESTPLATE);
                            damager.getInventory().setChestplate(null);
                            break;
                        }
                        case HEAD: {
                            damager.playEffect(EntityEffect.BREAK_EQUIPMENT_HELMET);
                            damager.getInventory().setHelmet(null);
                            break;
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
