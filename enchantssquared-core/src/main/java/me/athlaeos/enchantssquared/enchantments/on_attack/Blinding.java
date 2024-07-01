package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.animations.Animation;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.managers.AnimationRegistry;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;

public class Blinding extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double chanceBase;
    private final double chanceLv;
    private final int durationBase;
    private final int durationLv;
    private final String particleAnimation;
    private final Sound sound;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Blinding(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.chanceBase = config.getDouble("enchantment_configuration.blinding.apply_chance");
        this.chanceLv = config.getDouble("enchantment_configuration.blinding.apply_chance_lv");
        this.durationBase = config.getInt("enchantment_configuration.blinding.duration");
        this.durationLv = config.getInt("enchantment_configuration.blinding.duration_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.blinding.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.blinding.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.blinding.incompatible_custom_enchantments"));
        this.sound = Utils.soundFromString(config.getString("enchantment_configuration.blinding.sound"), Sound.ENTITY_BLAZE_DEATH);
        this.particleAnimation = config.getString("enchantment_configuration.blinding.animation");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.blinding.icon", createIcon(Material.INK_SAC));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        double chance = chanceBase + ((level - 1) * chanceLv);
        if (Utils.getRandom().nextDouble() < chance){
            int duration = durationBase + ((level - 1) * durationLv);

            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectType.BLINDNESS, duration, 1, true, false, true)
            );

            if (sound != null) victim.getWorld().playSound(victim.getLocation(), sound, 0.5f, 1f);
            Animation animation = AnimationRegistry.get(particleAnimation);
            if (animation != null) animation.play(victim.getEyeLocation());
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.blinding.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.blinding.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.blinding.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.blinding";
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
        return config.getInt("enchantment_configuration.blinding.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.blinding.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.blinding.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.blinding.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.blinding.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.blinding.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.blinding.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.blinding.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.blinding.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.blinding.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-blinding";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
