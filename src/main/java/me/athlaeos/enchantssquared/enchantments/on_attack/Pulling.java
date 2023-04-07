package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;

public class Pulling extends CustomEnchant implements TriggerOnAttackEnchantment {
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Pulling(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.pull_chance_base = config.getDouble("enchantment_configuration.pulling.pull_chance_base");
        this.pull_chance_lv = config.getDouble("enchantment_configuration.pulling.pull_chance_lv");
        this.pull_strength_base = config.getDouble("enchantment_configuration.pulling.pull_strength_base");
        this.pull_strength_lv = config.getDouble("enchantment_configuration.pulling.pull_strength_lv");
        this.pull_strength_max_base = config.getDouble("enchantment_configuration.pulling.pull_strength_max_base");
        this.pull_strength_max_lv = config.getDouble("enchantment_configuration.pulling.pull_strength_max_lv");
        this.proc_sound = Utils.soundFromString(config.getString("enchantment_configuration.pulling.proc_sound"), Sound.ITEM_TRIDENT_HIT);
        this.pull_sound = Utils.soundFromString(config.getString("enchantment_configuration.pulling.pull_sound"), Sound.ENTITY_FISHING_BOBBER_RETRIEVE);
        this.pull_delay = config.getInt("enchantment_configuration.pulling.pull_delay");

        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.pulling.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.pulling.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.pulling.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.pulling.icon", createIcon(Material.STRING));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    private final double pull_chance_base;
    private final double pull_chance_lv;
    private final double pull_strength_base;
    private final double pull_strength_lv;
    private final double pull_strength_max_base;
    private final double pull_strength_max_lv;
    private final Sound proc_sound;
    private final Sound pull_sound;
    private final int pull_delay;

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        double chance = pull_chance_base + ((level - 1) * pull_chance_lv);
        if (Utils.getRandom().nextDouble() <= chance){
            if (victim instanceof Player) ((Player) victim).playSound(victim, proc_sound, 1F, 1F);
            if (realAttacker instanceof Player) ((Player) realAttacker).playSound(realAttacker, proc_sound, 1F, 1F);

            double strength = pull_strength_base + ((level - 1) * pull_strength_lv);
            double maxStrength = pull_strength_max_base + ((level - 1) * pull_strength_max_lv);

            EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
                Location attackerLocation = realAttacker.getLocation();
                Location victimLocation = victim.getLocation();
                if (!realAttacker.getWorld().equals(victim.getWorld())) return;

                if (victim instanceof Player) ((Player) victim).playSound(victim, pull_sound, 1F, 1F);
                if (realAttacker instanceof Player) ((Player) realAttacker).playSound(realAttacker, pull_sound, 1F, 1F);
                victim.setVelocity(victim.getVelocity().add(new Vector(
                        Math.min(maxStrength, (attackerLocation.getX() - victimLocation.getX()) * strength),
                        Math.min(maxStrength, (attackerLocation.getY() - victimLocation.getY()) * strength),
                        Math.min(maxStrength, (attackerLocation.getZ() - victimLocation.getZ()) * strength))));
            }, pull_delay);
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.pulling.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.pulling.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.pulling.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.pulling";
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
        return config.getInt("enchantment_configuration.pulling.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.pulling.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.pulling.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.pulling.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.pulling.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.pulling.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.pulling.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.pulling.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.pulling.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.pulling.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-pulling";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
