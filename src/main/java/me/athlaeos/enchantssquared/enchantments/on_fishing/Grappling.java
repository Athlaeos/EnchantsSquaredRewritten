package me.athlaeos.enchantssquared.enchantments.on_fishing;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandOnly;
import me.athlaeos.enchantssquared.enchantments.LevelsFromOffHandOnly;
import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;

public class Grappling extends CustomEnchant implements TriggerOnFishingEnchantment {
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
    public Grappling(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.grappling.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.grappling.incompatible_custom_enchantments"));

        this.forceBase = config.getDouble("enchantment_configuration.grappling.force_base");
        this.forceLv = config.getDouble("enchantment_configuration.grappling.force_lv");
        this.requireHooking = config.getBoolean("enchantment_configuration.grappling.require_hooking");
        this.cooldownMessage = config.getString("enchantment_configuration.grappling.cooldown_message");
        this.cooldown = config.getInt("enchantment_configuration.grappling.cooldown");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.grappling.icon", new ItemStack(Material.FISHING_ROD));
    }

    private final LevelService mainHandService = new LevelsFromMainHandOnly(this);
    private final LevelService offHandService = new LevelsFromOffHandOnly(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandService : mainHandService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.grappling.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.grappling.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.grappling.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.grappling";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material == Material.FISHING_ROD;
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material == Material.FISHING_ROD;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.grappling.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.grappling.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.grappling.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.grappling.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.grappling.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.grappling.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.grappling.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.grappling.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.grappling.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.grappling.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-grappling";
    }

    private final double forceBase;
    private final double forceLv;
    private final boolean requireHooking;
    private final String cooldownMessage;
    private final int cooldown;

    @Override
    public void onFish(PlayerFishEvent e, int level) {
        if (e.getState() == PlayerFishEvent.State.FISHING) return;
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getHook().getLocation())) return;
        if (e.getHook().getWorld().getBlockAt(e.getHook().getLocation()).getType() == Material.WATER) return;
        if (requireHooking){
            if (e.getHook().getLocation().add(0.2, 0, 0).getBlock().isPassable() &&
                    e.getHook().getLocation().add(0, 0, 0.2).getBlock().isPassable() &&
                    e.getHook().getLocation().add(-0.2, 0, 0).getBlock().isPassable() &&
                    e.getHook().getLocation().add(0, 0, -0.2).getBlock().isPassable() &&
                    e.getHook().getLocation().add(0, -0.2, 0).getBlock().isPassable()) {
                return;
            }
            //if (e.getState() != PlayerFishEvent.State.IN_GROUND) return;
        }
        if (!CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "grappling_cooldown")) {
            if (!cooldownMessage.equals("")){
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.chat(cooldownMessage
                        .replace("%cooldown%", String.format("%.1f", CooldownManager.getInstance().getCooldown(e.getPlayer().getUniqueId(), "grappling_cooldown")/1000D)))));
            }
            return;
        }
        double force =  this.forceBase + ((level - 1) * this.forceLv);
        Location hookLocation = e.getHook().getLocation();
        Location playerLocation = e.getPlayer().getLocation();

        e.getPlayer().setVelocity(e.getPlayer().getVelocity().add(new Vector(
                hookLocation.getX() - playerLocation.getX(),
                hookLocation.getY() - playerLocation.getY(),
                hookLocation.getZ() - playerLocation.getZ())
                .multiply(force)));

        CooldownManager.getInstance().setCooldownIgnoreIfPermission(e.getPlayer(), cooldown * 50, "grappling_cooldown");
    }
}
