package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.animations.Animation;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.managers.AnimationRegistry;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class ShieldDowner extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double chanceBase;
    private final double chanceLv;
    private final int durationBase;
    private final int durationLv;
    private final String particleAnimation;
    private final Sound sound;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public ShieldDowner(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.shield_downer.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.shield_downer.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.shield_downer.incompatible_custom_enchantments"));

        this.chanceBase = config.getDouble("enchantment_configuration.shield_downer.apply_chance");
        this.chanceLv = config.getDouble("enchantment_configuration.shield_downer.apply_chance_lv");
        this.durationBase = config.getInt("enchantment_configuration.shield_downer.duration");
        this.durationLv = config.getInt("enchantment_configuration.shield_downer.duration_lv");

        this.sound = Utils.soundFromString(config.getString("enchantment_configuration.shield_downer.sound"), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR);
        this.particleAnimation = config.getString("enchantment_configuration.shield_downer.animation");

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.shield_downer.icon", createIcon(Material.SHIELD));
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
        if (!(e.getEntity() instanceof Player) || shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;
        Player p = (Player) e.getEntity();

        if (!p.isBlocking() || p.getCooldown(Material.SHIELD) > 0) return;

        double chance = chanceBase + ((level - 1) * chanceLv);
        if (Utils.getRandom().nextDouble() < chance){
            int duration = durationBase + ((level - 1) * durationLv);

            p.setCooldown(Material.SHIELD, duration);

            if (sound != null) victim.getWorld().playSound(victim, sound, 0.5f, 1f);
            Animation animation = AnimationRegistry.get(particleAnimation);
            if (animation != null) animation.play(victim.getEyeLocation());
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.shield_downer.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.shield_downer.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.shield_downer.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.shield_downer";
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
        return config.getInt("enchantment_configuration.shield_downer.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.shield_downer.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.shield_downer.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.shield_downer.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.shield_downer.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.shield_downer.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.shield_downer.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.shield_downer.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.shield_downer.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.shield_downer.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-shield-downer";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
