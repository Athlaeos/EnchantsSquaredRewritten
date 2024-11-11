package me.athlaeos.enchantssquared.enchantments.on_block_break;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.ExecutionPriority;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.Levels1IfPresent;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.valhallammo.listeners.LootListener;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class Telekinesis extends CustomEnchant implements TriggerOnBlockBreakEnchantment {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;
    private final boolean preventItemOwnership;

    public Telekinesis(int id, String type) {
        super(id, type);
        this.priority = ExecutionPriority.LAST;
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.preventItemOwnership = config.getBoolean("enchantment_configuration.telekinesis.prevent_item_ownership", false);
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.telekinesis.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.telekinesis.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.telekinesis.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.telekinesis.icon", createIcon(Material.ENDER_PEARL));
    }

    private final LevelService levelService = new Levels1IfPresent(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.telekinesis.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.telekinesis.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.telekinesis.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.telekinesis";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    private final Collection<String> naturallyCompatibleWith;
    @Override
    public boolean isNaturallyCompatible(Material material) {
        return MaterialClassType.isMatchingClass(material, naturallyCompatibleWith);
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return true;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.telekinesis.weight");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMaxTableLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.telekinesis.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.telekinesis.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.telekinesis.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.telekinesis.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.telekinesis.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return 0;
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return 0;
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-telekinesis";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        if (!EnchantsSquared.isValhallaHooked()) return; // let valhalla handle pickups in case it's installed
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;
        LootListener.setInstantPickup(e.getBlock(), e.getPlayer());
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        if (EnchantsSquared.isValhallaHooked()) return;
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;

        for (Item i : e.getItems()){
            ItemUtils.addItem(e.getPlayer(), i.getItemStack(), !preventItemOwnership);
        }

        e.getItems().clear();
    }
}
