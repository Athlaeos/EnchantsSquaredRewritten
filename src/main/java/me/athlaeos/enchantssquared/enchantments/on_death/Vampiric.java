package me.athlaeos.enchantssquared.enchantments.on_death;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class Vampiric extends CustomEnchant implements TriggerOnDeathEnchantment, TriggerOnAttackEnchantment {
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
    public Vampiric(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.vampiric.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.vampiric.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.vampiric.incompatible_custom_enchantments"));

        this.lifestealBase = config.getDouble("enchantment_configuration.vampiric.lifesteal_base");
        this.lifestealLv = config.getDouble("enchantment_configuration.vampiric.lifesteal_lv");
        this.lifestealMax = config.getDouble("enchantment_configuration.vampiric.lifesteal_max");
        this.healingBase = config.getDouble("enchantment_configuration.vampiric.healing_base");
        this.healingLv = config.getDouble("enchantment_configuration.vampiric.healing_lv");
        this.hungerBase = config.getDouble("enchantment_configuration.vampiric.hunger_base");
        this.hungerLv = config.getDouble("enchantment_configuration.vampiric.hunger_lv");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.vampiric.icon", createIcon(Material.REDSTONE));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.vampiric.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.vampiric.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.vampiric.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.vampiric";
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
        return config.getInt("enchantment_configuration.vampiric.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.vampiric.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.vampiric.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.vampiric.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.vampiric.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.vampiric.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.vampiric.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.vampiric.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.vampiric.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.vampiric.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-vampiric";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double lifestealBase;
    private final double lifestealLv;
    private final double lifestealMax;
    private final double healingBase;
    private final double healingLv;
    private final double hungerBase;
    private final double hungerLv;

    @Override
    public void onEntityDeath(EntityDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onEntityKilled(EntityDeathEvent e, int level) {
        if (e.getEntity().getKiller() == null || shouldEnchantmentCancel(level, e.getEntity().getKiller(), e.getEntity().getLocation())) return;
        Player killer = e.getEntity().getKiller();
        double killHeal = healingBase + ((level - 1) * healingLv);
        healEntity(killer, killHeal);

        int killHunger = Utils.excessChance(hungerBase + ((level - 1) * hungerLv));
        if (killer.getFoodLevel() < 20){
            // regen hunger
            killer.setFoodLevel(Math.min(20, killer.getFoodLevel() + killHunger));
        } else {
            // regen saturation
            killer.setSaturation(Math.min(20, killer.getSaturation() + killHunger));
        }
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerKilled(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent e, int level) {
        // do nothing
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        if (shouldEnchantmentCancel(level, realAttacker, e.getEntity().getLocation())) return;
        double healingFraction = lifestealBase + ((level - 1) * lifestealLv);
        double healed = Math.min(lifestealMax, e.getDamage() * healingFraction);
        healEntity(realAttacker, healed);
    }

    private void healEntity(LivingEntity e, double amount){
        AttributeInstance attribute = e.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null){
            double maxHealth = attribute.getValue();
            double currentHealth = e.getHealth();
            EntityRegainHealthEvent healEvent = new EntityRegainHealthEvent(e, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(healEvent);
            if (!healEvent.isCancelled()){
                e.setHealth(Math.max(0, Math.min(maxHealth, currentHealth + healEvent.getAmount())));
            }
        }
    }
}
