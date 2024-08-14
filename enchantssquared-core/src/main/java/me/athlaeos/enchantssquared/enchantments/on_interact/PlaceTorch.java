package me.athlaeos.enchantssquared.enchantments.on_interact;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandOnly;
import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.utility.EnchantmentMappings;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class PlaceTorch extends CustomEnchant implements TriggerOnInteractEnchantment {
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
    public PlaceTorch(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.illuminated.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.illuminated.incompatible_custom_enchantments"));

        this.cooldown = config.getInt("enchantment_configuration.illuminated.cooldown");
        this.damagePerTorch = config.getDouble("enchantment_configuration.illuminated.durability_cost");
        this.applyUnbreaking = config.getBoolean("enchantment_configuration.illuminated.use_unbreaking");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.illuminated.icon", createIcon(Material.TORCH));
    }

    private final LevelService levelService = new LevelsFromMainHandOnly(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.illuminated.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.illuminated.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.illuminated.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.illuminated";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material.toString().contains("_PICKAXE");
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material.toString().contains("_PICKAXE");
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.illuminated.weight");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMaxTableLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.illuminated.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.illuminated.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.illuminated.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.illuminated.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.illuminated.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.illuminated.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.illuminated.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-torches";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return Collections.singleton("PICKAXES");
    }

    private final int cooldown;
    private final boolean applyUnbreaking;
    private final double damagePerTorch;

    @Override
    public void onInteract(PlayerInteractEvent e, int level) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getHand() != EquipmentSlot.HAND ||
                clickedBlock == null ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !e.getBlockFace().isCartesian() ||
                clickedBlock.isPassable() ||
                clickedBlock.getType().isInteractable() ||
                !CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "illuminated_cooldown") ||
                shouldEnchantmentCancel(level, e.getPlayer(), clickedBlock.getLocation())) return;
        // only main hand clicks are registered, clicked block should not be null or non-solid, player should be sneaking,
        // clicked block face should be cartesian
        ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isAirOrNull(heldItem) || heldItem.getType().getMaxDurability() <= 0 || !(heldItem.getItemMeta() instanceof Damageable)) return;
        Block torchBlock = clickedBlock.getRelative(e.getBlockFace());
        Block placedAgainst = e.getBlockFace() == BlockFace.DOWN ? torchBlock.getLocation().add(0, -1, 0).getBlock() : clickedBlock;
        if (placedAgainst.getType().isSolid() && placedAgainst.getType().isOccluding() && torchBlock.getType().toString().contains("AIR")){
            torchBlock.setType(Material.TORCH);
            BlockState previousBlockState = torchBlock.getState();
            BlockPlaceEvent event = new BlockPlaceEvent(
                    torchBlock,
                    previousBlockState,
                    placedAgainst,
                    e.getPlayer().getInventory().getItemInMainHand(),
                    e.getPlayer(),
                    true,
                    EquipmentSlot.HAND
            );
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled() && event.canBuild()){
                if (e.getBlockFace() == BlockFace.UP || e.getBlockFace() == BlockFace.DOWN){
                    torchBlock.setType(Material.TORCH);
                } else {
                    torchBlock.setType(Material.WALL_TORCH);
                    Directional torch = (Directional) torchBlock.getBlockData();
                    torch.setFacing(e.getBlockFace());
                    torchBlock.setBlockData(torch);
                }

                CooldownManager.getInstance().setCooldownIgnoreIfPermission(e.getPlayer(), cooldown * 50, "illuminated_cooldown");

                if (heldItem.getItemMeta().isUnbreakable()) return;
                int damage = Utils.excessChance(damagePerTorch);
                if (applyUnbreaking){
                    int unbreakingLevel = heldItem.getEnchantmentLevel(EnchantmentMappings.UNBREAKING.getEnchantment());
                    damage = Utils.excessChance(damagePerTorch * (1D/(unbreakingLevel + 1D)));
                }
                PlayerItemDamageEvent damageEvent = new PlayerItemDamageEvent(e.getPlayer(), heldItem, damage);
                EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(damageEvent);
                if (!damageEvent.isCancelled() || EnchantsSquared.isValhallaHooked()){
                    Damageable toolMeta = (Damageable) heldItem.getItemMeta();
                    toolMeta.setDamage(toolMeta.getDamage() + damage);
                    if (toolMeta.getDamage() >= heldItem.getType().getMaxDurability()) {
                        e.getPlayer().getInventory().setItemInMainHand(null);
                        e.getPlayer().playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                    } else heldItem.setItemMeta(toolMeta);
                }
            }
        }
    }
}
