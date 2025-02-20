package me.athlaeos.enchantssquared.domain;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.valhallatrinkets.TrinketsManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public enum MaterialClassType {
    SWORDS(Arrays.asList("GOLDEN_SWORD", "STONE_SWORD", "WOODEN_SWORD", "NETHERITE_SWORD", "DIAMOND_SWORD", "IRON_SWORD")),
    BOWS(Collections.singletonList("BOW")),
    CROSSBOWS(Collections.singletonList("CROSSBOW")),
    TRIDENTS(Collections.singletonList("TRIDENT")),
    HELMETS(Arrays.asList("PLAYER_HEAD", "SKELETON_SKULL", "ZOMBIE_HEAD", "WITHER_SKELETON_SKULL", "CARVED_PUMPKIN", "LEATHER_HELMET", "CHAINMAIL_HELMET", "GOLDEN_HELMET", "IRON_HELMET", "DIAMOND_HELMET", "NETHERITE_HELMET", "TURTLE_HELMET")),
    CHESTPLATES(Arrays.asList("LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "GOLDEN_CHESTPLATE", "IRON_CHESTPLATE", "DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE")),
    LEGGINGS(Arrays.asList("LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS", "GOLDEN_LEGGINGS", "IRON_LEGGINGS", "DIAMOND_LEGGINGS", "NETHERITE_LEGGINGS")),
    BOOTS(Arrays.asList("LEATHER_BOOTS", "CHAINMAIL_BOOTS", "GOLDEN_BOOTS", "IRON_BOOTS", "DIAMOND_BOOTS", "NETHERITE_BOOTS")),
    SHEARS(Collections.singletonList("SHEARS")),
    FLINTANDSTEEL(Collections.singletonList("FLINT_AND_STEEL")),
    FISHINGROD(Collections.singletonList("FISHING_ROD")),
    ELYTRA(Collections.singletonList("ELYTRA")),
    PICKAXES(Arrays.asList("WOODEN_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "IRON_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE")),
    AXES(Arrays.asList("WOODEN_AXE", "STONE_AXE", "GOLDEN_AXE", "IRON_AXE", "DIAMOND_AXE", "NETHERITE_AXE")),
    SHOVELS(Arrays.asList("WOODEN_SHOVEL", "STONE_SHOVEL", "GOLDEN_SHOVEL", "IRON_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL")),
    HOES(Arrays.asList("WOODEN_HOE", "STONE_HOE", "GOLDEN_HOE", "IRON_HOE", "DIAMOND_HOE", "NETHERITE_HOE")),
    SHIELDS(Collections.singletonList("SHIELD")),
    MACES(Collections.singletonList("MACE")),
    BRUSHES(Collections.singletonList("BRUSH")),
    ALL(new ArrayList<>()),
    TRINKETS(new ArrayList<>());

    private final Collection<Material> matches = new HashSet<>();

    MaterialClassType(Collection<String> matches) {
        this.matches.addAll(ItemUtils.getMaterialList(matches));
    }
    public Collection<Material> getMatches() {
        return matches;
    }

    public static MaterialClassType getClass(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        if (EnchantsSquared.isTrinketsHooked()){
            if (TrinketsManager.getTrinketType(item.getItemMeta()) != null) return TRINKETS;
        }
        for (MaterialClassType tc : MaterialClassType.values()) {
            if (tc.getMatches().contains(item.getType())) {
                return tc;
            }
        }
        return null;
    }

    public static boolean isMatchingClass(Material material, Collection<String> compatibleClasses){
        MaterialClassType materialClassType = MaterialClassType.getClass(material);
        return materialClassType != null && (compatibleClasses.contains("ALL") ||
                compatibleClasses.contains(materialClassType.toString()) ||
                compatibleClasses.contains(material.toString())
        );
    }

    public static MaterialClassType getClass(Material item) {
        for (MaterialClassType tc : MaterialClassType.values()) {
            if (tc.getMatches().contains(item)) {
                return tc;
            }
        }
        return null;
    }

    public static boolean isArmor(ItemStack m) {
        MaterialClassType equipmentClass = getClass(m);
        return equipmentClass == HELMETS || equipmentClass == CHESTPLATES || equipmentClass == LEGGINGS || equipmentClass == BOOTS;
    }
}