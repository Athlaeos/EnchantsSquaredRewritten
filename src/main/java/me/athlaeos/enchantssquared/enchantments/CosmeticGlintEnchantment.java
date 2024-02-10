package me.athlaeos.enchantssquared.enchantments;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CosmeticGlintEnchantment extends Enchantment {
    private static final NamespacedKey enchantmentKey = new NamespacedKey(EnchantsSquared.getPlugin(), "glint_enchantment");
    private static final CosmeticGlintEnchantment ENCHANTSSQUARED_GLINT = MinecraftVersion.currentVersionOlderThan(MinecraftVersion.MINECRAFT_1_19) ? new CosmeticGlintEnchantment() : null;

    public CosmeticGlintEnchantment() {
        super(enchantmentKey);
    }

    public static Enchantment getEnchantsSquaredGlint() {
        return ENCHANTSSQUARED_GLINT;
    }

    @Override
    public String getName() {
        return "Glint";
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    public static void registerEnchantment(Enchantment enchantment) {
        try {
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void register(){
        boolean registered = Arrays.stream(Enchantment.values()).collect(Collectors.toList()).contains(ENCHANTSSQUARED_GLINT);
        if (!registered) registerEnchantment(ENCHANTSSQUARED_GLINT);
    }
}
