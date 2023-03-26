package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;

public class CurseHeavy extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
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
    public CurseHeavy(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.curse_heavy.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_heavy.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.curse_heavy.incompatible_custom_enchantments"));

        this.slowBase = config.getDouble("enchantment_configuration.curse_heavy.slow_base");
        this.slowLv = config.getDouble("enchantment_configuration.curse_heavy.slow_lv");
        this.fatigue = config.getBoolean("enchantment_configuration.curse_heavy.fatigue");
        this.fatigueAmplifierBase = config.getInt("enchantment_configuration.curse_heavy.amplifier_fatigue");
        this.fatigueAmplifierLv = config.getInt("enchantment_configuration.curse_heavy.amplifier_lv_fatigue");
        this.fatigueDuration = config.getInt("enchantment_configuration.curse_heavy.duration");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.curse_heavy.icon", new ItemStack(Material.ANVIL));

        EnchantsSquared.getPlugin().getServer().getPluginManager().registerEvents(this, EnchantsSquared.getPlugin());
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.curse_heavy.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.curse_heavy.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.curse_heavy.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.curse_heavy";
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
        return config.getInt("enchantment_configuration.curse_heavy.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.curse_heavy.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.curse_heavy.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.curse_heavy.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.curse_heavy.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.curse_heavy.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.curse_heavy.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.curse_heavy.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_heavy.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.curse_heavy.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-curse-heavy";
    }

    private final double slowBase;
    private final double slowLv;
    private final boolean fatigue;
    private final int fatigueAmplifierBase;
    private final int fatigueAmplifierLv;
    private final int fatigueDuration;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_curse_heavy", Attribute.GENERIC_MOVEMENT_SPEED);
            return;
        }

        double slow = slowBase + ((level - 1) * slowLv);
        LivingEntity entity = (LivingEntity) e;

        if (fatigue){
            int fatigueAmplifier = fatigueAmplifierBase + ((level - 1) * fatigueAmplifierLv);
            EntityUtils.applyPotionEffectIfStronger(entity,
                    new PotionEffect(PotionEffectType.SLOW_DIGGING, fatigueDuration, fatigueAmplifier, true, false, false)
            );
        }

        EntityUtils.addUniqueAttribute((LivingEntity) e, "es_curse_heavy", Attribute.GENERIC_MOVEMENT_SPEED, -slow, AttributeModifier.Operation.ADD_SCALAR);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "es_curse_heavy", Attribute.GENERIC_MOVEMENT_SPEED);
    }
}