package me.athlaeos.enchantssquared.enchantments.on_potion_effect;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_item_damage.TriggerOnItemDamageEnchantment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class IncreasePotionPotency extends CustomEnchant implements TriggerOnPotionEffectEnchantment {
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
    public IncreasePotionPotency(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.potion_potency_buff.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.potion_potency_buff.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.potion_potency_buff.incompatible_custom_enchantments"));

        this.amplifierBase = config.getDouble("enchantment_configuration.potion_potency_buff.amplifier_buff_base");
        this.amplifierLv = config.getDouble("enchantment_configuration.potion_potency_buff.amplifier_buff_lv");
        this.durationBase = config.getDouble("enchantment_configuration.potion_potency_buff.duration_buff_base");
        this.durationLv = config.getDouble("enchantment_configuration.potion_potency_buff.duration_buff_lv");
        this.durationMinimum = config.getDouble("enchantment_configuration.potion_potency_buff.duration_minimum");
        this.exceptions.addAll(config.getStringList("enchantment_configuration.potion_potency_buff.exceptions"));

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.potion_potency_buff.icon", new ItemStack(Material.GLOWSTONE_DUST));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.potion_potency_buff.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.potion_potency_buff.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.potion_potency_buff.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.potion_potency_buff";
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
        return config.getInt("enchantment_configuration.potion_potency_buff.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.potion_potency_buff.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.potion_potency_buff.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.potion_potency_buff.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.potion_potency_buff.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.potion_potency_buff.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.potion_potency_buff.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.potion_potency_buff.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.potion_potency_buff.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.potion_potency_buff.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-potion-potency-buff";
    }

    private final double amplifierBase;
    private final double amplifierLv;
    private final double durationBase;
    private final double durationLv;
    private final double durationMinimum;
    private final Collection<String> exceptions = new HashSet<>();

    @Override
    public void onPotionEffect(EntityPotionEffectEvent e, int level) {
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) e.getEntity();
        if (excludedPlayers.contains(entity.getUniqueId())) {
            // this is to prevent the event from calling itself and infinitely amplifying the potion effect
            excludedPlayers.remove(entity.getUniqueId());
            return;
        }
        if (shouldEnchantmentCancel(level, entity, entity.getLocation())) return;

        if (e.getCause() == EntityPotionEffectEvent.Cause.POTION_DRINK && e.getNewEffect() != null){
            if (exceptions.contains(e.getNewEffect().getType().toString())) return;

            PotionEffect newEffect = e.getNewEffect();
            int originalAmplifier = newEffect.getAmplifier() + 1;
            int originalDuration = newEffect.getDuration();
            double amplifierMultiplier = this.amplifierBase + ((level - 1) * amplifierLv);
            double durationMultiplier = Math.max(durationMinimum, durationBase + ((level - 1) * durationLv));
            originalAmplifier = (int) Math.floor(originalAmplifier * amplifierMultiplier) - 1;
            originalDuration = (int) Math.floor(originalDuration * durationMultiplier);

            PotionEffect enhancedEffect = new PotionEffect(newEffect.getType(), originalDuration, originalAmplifier, false, true, true);
            EntityPotionEffectEvent event = new EntityPotionEffectEvent(
                    entity,
                    e.getOldEffect(),
                    enhancedEffect,
                    EntityPotionEffectEvent.Cause.POTION_DRINK,
                    e.getOldEffect() == null ? EntityPotionEffectEvent.Action.ADDED : EntityPotionEffectEvent.Action.CHANGED,
                    e.getOldEffect() != null);
            excludedPlayers.add(entity.getUniqueId());
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                e.setCancelled(true);
                entity.addPotionEffect(new PotionEffect(e.getModifiedType(), originalDuration, originalAmplifier));
            }
        }
    }

    private final Collection<UUID> excludedPlayers = new HashSet<>();
}
