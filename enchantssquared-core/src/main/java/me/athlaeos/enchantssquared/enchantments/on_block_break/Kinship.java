package me.athlaeos.enchantssquared.enchantments.on_block_break;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Kinship extends CustomEnchant implements TriggerOnBlockBreakEnchantment {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    /**
     * Constructor for a Custom Enchant. The type and id must be unique and the type will automatically be uppercased
     * by convention.
     * The id will be used on the item to store the enchantment and thus must be consistent, or it will risk
     * changing existing enchantments on item or simply invalidate the enchantment entirely.
     *
     * @param id   the identifying id of this custom enchant.
     * @param type the identifying type of this custom enchant.
     */
    public Kinship(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.kinship.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.kinship.incompatible_custom_enchantments"));

        this.fractionRegen = config.getBoolean("enchantment_configuration.kinship.fraction_regen");
        this.durabilityRegenNetherite = config.getDouble("enchantment_configuration.kinship.durability_regen_netherite");
        this.durabilityRegenDiamond = config.getDouble("enchantment_configuration.kinship.durability_regen_diamond");
        this.durabilityRegenIron = config.getDouble("enchantment_configuration.kinship.durability_regen_iron");
        this.durabilityRegenStone = config.getDouble("enchantment_configuration.kinship.durability_regen_stone");
        this.durabilityRegenGold = config.getDouble("enchantment_configuration.kinship.durability_regen_gold");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.kinship.icon", createIcon(Material.ANVIL));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.kinship.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.kinship.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.kinship.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.kinship";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material.toString().contains("_PICKAXE");
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material.toString().contains("_PICKAXE");
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.kinship.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.kinship.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.kinship.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.kinship.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.kinship.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.kinship.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.kinship.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.kinship.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.kinship.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.kinship.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-kinship";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return Collections.singletonList("PICKAXES");
    }

    private final double durabilityRegenNetherite;
    private final double durabilityRegenDiamond;
    private final double durabilityRegenIron;
    private final double durabilityRegenStone;
    private final double durabilityRegenGold;
    private final boolean fractionRegen;

    private double getDurabilityRegeneration(Material pickMaterial, Material blockMaterial){
        switch (blockMaterial.toString()) {
            case "STONE":
            case "COBBLESTONE":
            case "BLACKSTONE":
            case "DEEPSLATE": return pickMaterial == Material.STONE_PICKAXE ? durabilityRegenStone : 0;
            case "IRON_ORE":
            case "DEEPSLATE_IRON_ORE": return pickMaterial == Material.IRON_PICKAXE ? durabilityRegenIron : 0;
            case "DIAMOND_ORE":
            case "DEEPSLATE_DIAMOND_ORE": return pickMaterial == Material.DIAMOND_PICKAXE ? durabilityRegenDiamond : 0;
            case "GOLD_ORE":
            case "NETHER_GOLD_ORE":
            case "DEEPSLATE_GOLD_ORE": return pickMaterial == Material.GOLDEN_PICKAXE ? durabilityRegenGold : 0;
            case "NETHERITE_ORE": return pickMaterial.toString().equals("NETHERITE_PICKAXE") ? durabilityRegenNetherite : 0;
        }
        return 0;
    }
    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;
        ItemStack pickaxe = e.getPlayer().getInventory().getItemInMainHand();
        int durabilityToRepair = Utils.excessChance(getDurabilityRegeneration(pickaxe.getType(), e.getBlock().getType())
        * (fractionRegen ? pickaxe.getType().getMaxDurability() : 1));
        ItemUtils.damageItem((Player) e, pickaxe, -durabilityToRepair);
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        // do nothing
    }
}
