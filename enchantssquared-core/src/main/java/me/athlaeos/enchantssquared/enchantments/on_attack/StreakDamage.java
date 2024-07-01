package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandAndEquipment;
import me.athlaeos.enchantssquared.enchantments.on_attacked.TriggerOnAttackedEnchantment;
import me.athlaeos.enchantssquared.utility.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StreakDamage extends CustomEnchant implements TriggerOnAttackEnchantment, TriggerOnAttackedEnchantment {

    private final int maxStacksBase;
    private final int maxStacksLv;
    private final double damagePerStackBase;
    private final double damagePerStackLv;
    private final double fractionStacksLostBase;
    private final double fractionStacksLostLv;
    private final double rangedDamagePenalty;
    private final double damageBonusBase;
    private final double damageBonusLv;
    private final String stackMessageFormat;
    private final String stackMessageMax;
    private final String stacksLostMessage;

    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public StreakDamage(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.maxStacksBase = config.getInt("enchantment_configuration.streak_damage.max_stacks_base");
        this.maxStacksLv = config.getInt("enchantment_configuration.streak_damage.max_stacks_lv");
        this.damagePerStackBase = config.getDouble("enchantment_configuration.streak_damage.damage_stack_base");
        this.damagePerStackLv = config.getDouble("enchantment_configuration.streak_damage.damage_stack_lv");
        this.fractionStacksLostBase = config.getDouble("enchantment_configuration.streak_damage.stacks_fraction_lost_base");
        this.fractionStacksLostLv = config.getDouble("enchantment_configuration.streak_damage.stacks_fraction_lost_lv");
        this.rangedDamagePenalty = config.getDouble("enchantment_configuration.streak_damage.ranged_damage_penalty");
        this.damageBonusBase = config.getDouble("enchantment_configuration.streak_damage.damage_bonus_base");
        this.damageBonusLv = config.getDouble("enchantment_configuration.streak_damage.damage_bonus_lv");
        this.stackMessageFormat = config.getString("enchantment_configuration.streak_damage.stacks_message_format");
        this.stackMessageMax = config.getString("enchantment_configuration.streak_damage.stacks_message_max");
        this.stacksLostMessage = config.getString("enchantment_configuration.streak_damage.stacks_lost_message");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.streak_damage.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.streak_damage.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.streak_damage.incompatible_custom_enchantments"));

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.streak_damage.icon", createIcon(Material.CHORUS_PLANT));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    private final Map<UUID, Integer> stacks = new HashMap<>();

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        // stacks are valid if the player is melee attacking with a fully charged melee attack, or in the case of a projectile it must have a velocity of at least 2.5
        // which is a bit less than a typical fully charged bow attack (velocity 3)
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation()) || (e.getDamager() instanceof Player p && p.getAttackCooldown() < 0.9F) ||
                e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || (e.getDamager() instanceof Projectile pr && pr.getVelocity().lengthSquared() < 6.25D)) return;

        int currentStacks = stacks.getOrDefault(realAttacker.getUniqueId(), 0);
        int maxStacks = maxStacksBase + ((level - 1) * maxStacksLv);
        boolean ranged = e.getDamager() instanceof Projectile;
        double damageBonus = damageBonusBase + ((level - 1) * damageBonusLv);
        if (currentStacks > 0) {
            double damagePerStack = damagePerStackBase + ((level - 1) * damagePerStackLv);
            damageBonus += ((ranged ? (1 - rangedDamagePenalty) : 1) * damagePerStack * Math.min(maxStacks, currentStacks));
        }
        e.setDamage(Math.max(0, e.getDamage() * (1 + damageBonus)));
        stacks.put(realAttacker.getUniqueId(), Math.min(maxStacks, currentStacks + 1));
        if (stackMessageFormat != null && !stackMessageFormat.isEmpty() && realAttacker instanceof Player p){
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.chat((stacks.get(realAttacker.getUniqueId()) >= maxStacks ? stackMessageMax : stackMessageFormat).replace("%stacks%", String.valueOf(stacks.get(p.getUniqueId()))))));
        }
    }

    @Override
    public void onAttacked(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        if (stacks.getOrDefault(e.getEntity().getUniqueId(), 0) > 0){
            int currentStacks = stacks.get(e.getEntity().getUniqueId());
            int stacksLost = (int) Math.ceil((maxStacksBase + ((level - 1) * maxStacksLv)) * (fractionStacksLostBase + ((level - 1D) * fractionStacksLostLv)));
            currentStacks -= stacksLost;
            if (currentStacks <= 0) stacks.remove(e.getEntity().getUniqueId());
            else stacks.put(e.getEntity().getUniqueId(), currentStacks);
            if (e.getEntity() instanceof Player p){
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.chat(stacksLostMessage.replace("%stacks%", String.valueOf(stacks.get(p.getUniqueId()))))));
            }
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.streak_damage.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.streak_damage.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.streak_damage.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.streak_damage";
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
        return config.getInt("enchantment_configuration.streak_damage.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.streak_damage.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.streak_damage.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.streak_damage.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.streak_damage.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.streak_damage.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.streak_damage.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.streak_damage.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.streak_damage.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.streak_damage.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-streak-damage";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }
}
