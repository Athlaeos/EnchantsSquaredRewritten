package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

// Exists because enchantment naming conventions were changed in 1.20.5+
public enum EnchantmentMappings {
    FLAME("flame", "flame"),
    POWER("power", "power"),
    INFINITY("infinity", "infinity"),
    PUNCH("punch", "punch"),
    CURSE_OF_BINDING("binding_curse", "binding_curse"),
    CHANNELING("channeling", "channeling"),
    SHARPNESS("sharpness", "sharpness"),
    BANE_OF_ARTHROPODS("bane_of_arthropods", "bane_of_arthropods"),
    SMITE("smite", "smite"),
    DEPTH_STRIDER("depth_strider", "depth_strider"),
    EFFICIENCY("efficiency", "efficiency"),
    UNBREAKING("unbreaking", "unbreaking"),
    FIRE_ASPECT("fire_aspect", "fire_aspect"),
    FROST_WALKER("frost_walker", "frost_walker"),
    IMPALING("impaling", "impaling"),
    KNOCKBACK("knockback", "knockback"),
    FORTUNE("fortune", "fortune"),
    LOOTING("looting", "looting"),
    LOYALTY("loyalty", "loyalty"),
    LUCK_OF_THE_SEA("luck_of_the_sea", "luck_of_the_sea"),
    LURE("lure", "lure"),
    MENDING("mending", "mending"),
    MULTISHOT("multishot", "multishot"),
    RESPIRATION("respiration", "respiration"),
    PIERCING("piercing", "piercing"),
    PROTECTION("protection", "protection"),
    BLAST_PROTECTION("blast_protection", "blast_protection"),
    FEATHER_FALLING("feather_falling", "feather_falling"),
    FIRE_PROTECTION("fire_protection", "fire_protection"),
    PROJECTILE_PROTECTION("projectile_protection", "projectile_protection"),
    QUICK_CHARGE("quick_charge", "quick_charge"),
    RIPTIDE("riptide", "riptide"),
    SILK_TOUCH("silk_touch", "silk_touch"),
    SOUL_SPEED("soul_speed", "soul_speed"),
    SWEEPING_EDGE("sweeping", "sweeping_edge"),
    THORNS("thorns", "thorns"),
    CURSE_OF_VANISHING("vanishing_curse", "vanishing_curse"),
    AQUA_AFFINITY("aqua_affinity", "aqua_affinity"),
    BREACH("breach", "breach"),
    DENSITY("density", "density"),
    WIND_BURST("wind_burst", "wind_burst");
    private final String oldKey;
    private final String newKey;
    EnchantmentMappings(String oldKey, String newKey){
        this.oldKey = oldKey;
        this.newKey = newKey;
    }

    /**
     * Returns the version-specific enchantment for the given mapping. <br>
     * May be null if enchantment doesn't exist yet
     */
    @SuppressWarnings("deprecation")
    public Enchantment getEnchantment(){
        return MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? Registry.ENCHANTMENT.get(NamespacedKey.minecraft(newKey)) : Enchantment.getByKey(NamespacedKey.minecraft(oldKey));
    }
}
