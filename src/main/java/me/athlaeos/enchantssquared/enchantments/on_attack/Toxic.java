package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_heal.TriggerOnHealthRegainedEnchantment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Toxic extends CustomEnchant implements TriggerOnAttackEnchantment, TriggerOnHealthRegainedEnchantment {

    private final int durationBase;
    private final int durationLv;
    private final double amplifierBase;
    private final double amplifierLv;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Toxic(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.amplifierBase = config.getDouble("enchantment_configuration.toxic.healing_reduction_base");
        this.amplifierLv = config.getDouble("enchantment_configuration.toxic.healing_reduction_lv");
        this.durationBase = config.getInt("enchantment_configuration.toxic.duration_base");
        this.durationLv = config.getInt("enchantment_configuration.toxic.duration_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.toxic.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.toxic.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.toxic.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.toxic.icon", new ItemStack(Material.BONE));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }
    private final Map<UUID, Map<Integer, Long>> afflictedEntities = new HashMap<>();

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        int duration = durationBase + ((level - 1) * durationLv);

        afflictEntity(victim.getUniqueId(), level, 50 * duration);
    }

    @Override
    public void onHeal(EntityRegainHealthEvent e, int level) {
        int antihealLevel = getHealingReductionLevel(e.getEntity().getUniqueId());
        if (antihealLevel > 0){
            double antiheal = amplifierBase + ((antihealLevel - 1) * amplifierLv);
            e.setAmount(e.getAmount() * Math.max(0, 1 - antiheal));
        }
    }

    public void afflictEntity(UUID entity, int level, int timeMS){
        Map<Integer, Long> existingEntries = afflictedEntities.getOrDefault(entity, new HashMap<>());
        existingEntries.put(level, System.currentTimeMillis() + timeMS);
        afflictedEntities.put(entity, existingEntries);
    }

    public int getHealingReductionLevel(UUID player){
        Map<Integer, Long> toxicDetails = afflictedEntities.getOrDefault(player, new HashMap<>());
        Collection<Integer> levelCollection = toxicDetails.keySet().stream().filter(
                i -> toxicDetails.get(i) > System.currentTimeMillis()).collect(Collectors.toSet());
        return levelCollection.isEmpty() ? 0 : Collections.max(levelCollection);
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.toxic.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.toxic.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.toxic.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.toxic";
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
        return config.getInt("enchantment_configuration.toxic.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.toxic.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.toxic.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.toxic.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.toxic.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.toxic.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.toxic.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.toxic.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.toxic.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.toxic.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-slowing";
    }
}
