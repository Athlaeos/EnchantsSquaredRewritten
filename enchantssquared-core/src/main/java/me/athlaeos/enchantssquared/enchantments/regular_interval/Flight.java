package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Flight extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
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
    public Flight(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.flight.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.flight.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.flight.incompatible_custom_enchantments"));

        this.durabilityDecay = config.getDouble("enchantment_configuration.flight.durability_decay");
        this.durationBase = config.getInt("enchantment_configuration.flight.flight_duration_base");
        this.durationLv = config.getInt("enchantment_configuration.flight.flight_duration_lv");
        this.slowfallDuration = config.getInt("enchantment_configuration.flight.slowfall_duration");
        this.actionBarEnabled = config.getBoolean("enchantment_configuration.flight.flight_bar");
        this.actionBarTitle = config.getString("enchantment_configuration.flight.flight_bar_display", "");
        this.actionBarFuelColor = config.getString("enchantment_configuration.flight.flight_bar_present", "&a");
        this.actionBarEmptinessColor = config.getString("enchantment_configuration.flight.flight_bar_absent", "&c");
        this.fuelRegenerationBase = config.getInt("enchantment_configuration.flight.regeneration_base");
        this.fuelRegenerationLv = config.getInt("enchantment_configuration.flight.regeneration_lv");
        this.bossbarEnabled = config.getBoolean("enchantment_configuration.flight.flight_bossbar");
        this.bossBarTitle = config.getString("enchantment_configuration.flight.flight_bossbar_title", "");

        BarColor color;
        try {
            color = BarColor.valueOf(config.getString("enchantment_configuration.flight.flight_bossbar_color", "RED"));
        } catch (IllegalArgumentException ignored){
            color = BarColor.RED;
        }
        this.barColor = color;

        BarStyle style;
        try {
            style = BarStyle.valueOf(config.getString("enchantment_configuration.flight.flight_bossbar_shape", "SEGMENTED_6"));
        } catch (IllegalArgumentException ignored){
            style = BarStyle.SEGMENTED_6;
        }
        this.barStyle = style;

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.flight.icon", createIcon(Material.FEATHER));


    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.flight.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.flight.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.flight.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.flight";
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
        return config.getInt("enchantment_configuration.flight.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.flight.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.flight.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.flight.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.flight.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.flight.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.flight.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.flight.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.flight.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.flight.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-flight";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final Set<UUID> flyingPlayers = new HashSet<>();

    private final double durabilityDecay;
    private final int durationBase;
    private final int durationLv;
    private final int slowfallDuration;
    private final boolean actionBarEnabled;
    private final String actionBarTitle;
    private final String actionBarFuelColor;
    private final String actionBarEmptinessColor;
    private final int fuelRegenerationBase;
    private final int fuelRegenerationLv;
    private final boolean bossbarEnabled;
    private final String bossBarTitle;
    private final BarStyle barStyle;
    private final BarColor barColor;

    @Override
    public long getInterval() {
        return 10;
    }

    private final Set<UUID> playersGivenFlight = new HashSet<>();

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof Player)) return;
        Player p = (Player) e;
        boolean allowFlightNaturally = p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR
                || p.hasPermission("essentials.fly");
        if (!shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())){
            // player doesn't have the enchantment, or is not allowed in this area
//            if (!allowFlightNaturally && (p.isFlying() || p.getAllowFlight())){
//                // if the player isn't allowed to fly naturally, so if they're in survival/adventure mode and are also flying currently, disallow flight
//                p.setAllowFlight(false);
//                p.setFlying(false);
//            }
//        } else {
            if (!p.getAllowFlight()){
                playersGivenFlight.add(p.getUniqueId());
                p.setAllowFlight(true);
            }

            if (!allowFlightNaturally) {
                EntityUtils.SlotEquipment firstFlightItem = EntityUtils.getFirstEquipmentItemStackWithEnchantment(EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p), this);
                if (p.isFlying() && !ItemUtils.isAirOrNull(firstFlightItem.getEquipment())){
                    int damage = Utils.excessChance(durabilityDecay * (1D/(firstFlightItem.getEquipment().getEnchantmentLevel(EnchantmentMappings.UNBREAKING.getEnchantment()) + 1D)));
                    if (damage > 0 && ItemUtils.damageItem(p, firstFlightItem.getEquipment(), damage, firstFlightItem.getSlot())){
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, slowfallDuration, 0, true, false, true));
                        return;
                    }
                }

                if (durationBase > 0){
                    boolean isOnGround = !p.getLocation().add(0, -0.1, 0).getBlock().isPassable();

                    // Enchantment is configured a max level above 0, and therefore has a fuel meter
                    CooldownManager cooldownManager = CooldownManager.getInstance();
                    int maxFlightDuration = durationBase + ((level - 1) * durationLv);
                    int flightRegeneration = fuelRegenerationBase + ((level - 1) * fuelRegenerationLv);
                    if (!cooldownManager.getCounters("player_in_flight").containsKey(p.getUniqueId())) {
                        cooldownManager.setCounter(p.getUniqueId(), maxFlightDuration, "player_in_flight");
                    }
                    long currentFlight = cooldownManager.getCounterResult(p.getUniqueId(), "player_in_flight");
                    if (p.isFlying()) {
                        flyingPlayers.add(p.getUniqueId());
                        if (currentFlight <= 0) {
                            cooldownManager.setCounter(p.getUniqueId(), 0, "player_in_flight");
                            p.setAllowFlight(false);
                            p.setFlying(false);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, slowfallDuration, 0, true, false, true));
                        } else {
                            cooldownManager.incrementCounter(p.getUniqueId(), -500, "player_in_flight");
                        }
                    } else if (isOnGround) {
                        if (cooldownManager.getCounterResult(p.getUniqueId(), "player_in_flight") + flightRegeneration > maxFlightDuration) {
                            cooldownManager.setCounter(p.getUniqueId(), maxFlightDuration, "player_in_flight");
                            currentFlight = maxFlightDuration;
                        } else {
                            cooldownManager.incrementCounter(p.getUniqueId(), flightRegeneration, "player_in_flight");
                            currentFlight += flightRegeneration;
                        }
                    }

                    float fraction = (float) currentFlight / maxFlightDuration;
                    if (fraction < 0) fraction = 0F;
                    if (fraction > 1) fraction = 1F;
                    if (actionBarEnabled){
                        if (flyingPlayers.contains(p.getUniqueId())){
                            if (!actionBarTitle.equals("")){
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.chat(actionBarTitle.replace("%fuel%", fuelBarBuilder(fraction)))));
                            }
                            if (!(cooldownManager.getCounterResult(p.getUniqueId(), "player_in_flight") < maxFlightDuration)){
                                flyingPlayers.remove(p.getUniqueId());
                            }
                        }
                    }
                    if (bossbarEnabled){
                        if (flyingPlayers.contains(p.getUniqueId())){
                            if (!bossBarTitle.equals("")){
                                BossBarUtils.showBossBarToPlayer(p, ChatUtils.chat(bossBarTitle), fraction, 60, "es_flight", barColor, barStyle);
                            }
                            if (!(cooldownManager.getCounterResult(p.getUniqueId(), "player_in_flight") < maxFlightDuration)){
                                flyingPlayers.remove(p.getUniqueId());
                            }
                        }
                    }
                }
            }
        }
        // the following code runs regardless if the player has the enchantment or not, to later test if they're still supposed to fly
        if (allowFlightNaturally) return;
        if (level <= 0 && playersGivenFlight.contains(p.getUniqueId())){
            // only if the player previously was allowed to fly, check if they're still allowed to fly
            playersGivenFlight.remove(p.getUniqueId());
            p.setFlying(false);
            p.setAllowFlight(false);
        }
    }

    @Override
    public void onRemove(Entity e) {
        if (!(e instanceof Player)) return;
        Player p = (Player) e;
        EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p);
        if (getLevelService(false, p).getLevel(cachedEquipment) > 0) return;
        boolean allowFlightNaturally = p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR
                || p.hasPermission("essentials.fly");
        if (allowFlightNaturally) return;
        if (playersGivenFlight.contains(p.getUniqueId())){
            // only if the player previously was allowed to fly, check if they're still allowed to fly
            playersGivenFlight.remove(p.getUniqueId());
            p.setFlying(false);
            p.setAllowFlight(false);
        }
    }

    private String fuelBarBuilder(double fraction){
        int full = (int) Math.floor(fraction * 40D);
        int empty = 40 - full;
        return String.join("", Collections.nCopies(full,  actionBarFuelColor + "|")) +
                String.join("", Collections.nCopies(empty,  actionBarEmptinessColor + "|"));
    }
}