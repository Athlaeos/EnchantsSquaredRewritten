package me.athlaeos.enchantssquared.enchantments.on_potion_effect;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class SplashPotionBlock extends CustomEnchant implements TriggerOnPotionEffectEnchantment {
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
    public SplashPotionBlock(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.chemical_shield.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.chemical_shield.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.chemical_shield.icon", new ItemStack(Material.SPLASH_POTION));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        if (entity.getEquipment() != null){
            offHand = entity.getEquipment().getItemInMainHand().getType() != Material.SHIELD;
        }
        return offHand ? offHandLevels : mainHandLevels;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.chemical_shield.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.chemical_shield.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.chemical_shield.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.chemical_shield";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material == Material.SHIELD;
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material == Material.SHIELD;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.chemical_shield.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.chemical_shield.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.chemical_shield.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.chemical_shield.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.chemical_shield.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.chemical_shield.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.chemical_shield.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.chemical_shield.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.chemical_shield.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.chemical_shield.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-chemical-shield";
    }

    @Override
    public void onPotionEffect(EntityPotionEffectEvent e, int level) {
        if (!(e.getEntity() instanceof Player)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e.getEntity(), e.getEntity().getLocation())) return;

        if (e.getCause() == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD || e.getCause() == EntityPotionEffectEvent.Cause.POTION_SPLASH){
            if (((Player) e.getEntity()).isBlocking()){
                e.setCancelled(true);
            }
        }
    }
}
