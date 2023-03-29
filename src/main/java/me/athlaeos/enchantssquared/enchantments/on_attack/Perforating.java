package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.valhallammo.dom.ArmorType;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class Perforating extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double damageBase;
    private final double damageLv;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Perforating(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.damageBase = config.getDouble("enchantment_configuration.perforating.damage_base");
        this.damageLv = config.getDouble("enchantment_configuration.perforating.damage_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.perforating.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.perforating.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.perforating.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.perforating.icon", createIcon(Material.DIAMOND_SWORD));
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

        double damage = damageBase + ((level - 1) * damageLv);
        int count = 0;
        if (EnchantsSquared.isValhallaHooked()){
            count = ArmorType.getArmorTypeCount(victim, ArmorType.LIGHT);
        } else {
            for (ItemStack i : EntityUtils.getEntityEquipment(victim, false).getIterable(false)){
                if (MaterialClassType.isArmor(i)) {
                    if (i.toString().contains("LEATHER_") || i.toString().contains("CHAINMAIL_") || i.toString().contains("DIAMOND_")){
                        count++;
                    }
                }
            }
        }
        double multiplier = 1 + ((count * damage) / 100D);
        e.setDamage(e.getDamage() * multiplier);
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.perforating.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.perforating.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.perforating.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.perforating";
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
        return config.getInt("enchantment_configuration.perforating.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.perforating.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.perforating.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.perforating.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.perforating.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.perforating.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.perforating.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.perforating.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.perforating.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.perforating.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-perforating";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
