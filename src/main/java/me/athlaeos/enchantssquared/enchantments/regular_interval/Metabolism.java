package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class Metabolism extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment {
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
    public Metabolism(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.metabolism.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.metabolism.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.metabolism.incompatible_custom_enchantments"));

        this.hungerPerSecondBase = config.getDouble("enchantment_configuration.metabolism.hunger_regeneration_base");
        this.hungerPerSecondLv = config.getDouble("enchantment_configuration.metabolism.hunger_regeneration_lv");
        this.saturationLimit = config.getInt("enchantment_configuration.metabolism.saturation_limit");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.metabolism.icon", new ItemStack(Material.GOLDEN_CARROT));
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.metabolism.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.metabolism.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.metabolism.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.metabolism";
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
        return config.getInt("enchantment_configuration.metabolism.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.metabolism.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.metabolism.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.metabolism.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.metabolism.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.metabolism.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.metabolism.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.metabolism.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.metabolism.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.metabolism.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-metabolism";
    }

    private final double hungerPerSecondBase;
    private final double hungerPerSecondLv;
    private final int saturationLimit;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof Player)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) return;
        Player p = (Player) e;
        if (p.getFoodLevel() >= 20 && p.getSaturation() >= saturationLimit) return;

        int hungerRegenerated = Utils.excessChance(hungerPerSecondBase + (level - 1) * hungerPerSecondLv);

        if (p.getFoodLevel() >= 20){
            if (p.getSaturation() < saturationLimit && p.getSaturation() + hungerRegenerated <= 20F){
                p.setSaturation(p.getSaturation() + hungerRegenerated);
            } else if (p.getSaturation() + hungerRegenerated <= 20F){
                p.setSaturation(20F);
            }
        } else {
            p.setFoodLevel(p.getFoodLevel() + hungerRegenerated);
        }
    }
}
