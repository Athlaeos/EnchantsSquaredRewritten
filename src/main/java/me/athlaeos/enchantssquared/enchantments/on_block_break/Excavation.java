package me.athlaeos.enchantssquared.enchantments.on_block_break;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.utility.BlockUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Excavation extends CustomEnchant implements TriggerOnBlockBreakEnchantment, Listener {
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
    public Excavation(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.excavation.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.excavation.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.excavation.incompatible_custom_enchantments"));

        this.durabilityMultiplier = config.getDouble("enchantment_configuration.excavation.durability_decay");
        this.nerfExcavationSpeed = config.getBoolean("enchantment_configuration.excavation.nerf_excavation_speed");
        this.fatigueAmplifier = config.getInt("enchantment_configuration.excavation.fatigue_amplifier");
        this.fatigueDuration = config.getInt("enchantment_configuration.excavation.fatigue_duration");
        this.areaLeveling = config.getBoolean("enchantment_configuration.excavation.area_leveling");
        this.durabilityLeveling = config.getBoolean("enchantment_configuration.excavation.durability_leveling");
        this.durabilityMultiplierLv = config.getDouble("enchantment_configuration.excavation.durability_decay_lv");
        this.allowSneakDisable = config.getBoolean("enchantment_configuration.excavation.sneak_disable", true);
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.excavation.icon", createIcon(Material.TNT));

        YamlConfiguration excavationConfig = ConfigManager.getInstance().getConfig("excavationblocks.yml").get();
        excavationBreakables.put(MaterialClassType.PICKAXES, ItemUtils.getMaterialList(excavationConfig.getStringList("excavation_pickaxe_blocks")));
        excavationBreakables.put(MaterialClassType.AXES, ItemUtils.getMaterialList(excavationConfig.getStringList("excavation_axe_blocks")));
        excavationBreakables.put(MaterialClassType.SHOVELS, ItemUtils.getMaterialList(excavationConfig.getStringList("excavation_shovel_blocks")));
        excavationBreakables.put(MaterialClassType.HOES, ItemUtils.getMaterialList(excavationConfig.getStringList("excavation_hoe_blocks")));
        for (MaterialClassType m : MaterialClassType.values()){
            Collection<Material> existingEntries = excavationBreakables.getOrDefault(m, new HashSet<>());
            existingEntries.addAll(ItemUtils.getMaterialList(excavationConfig.getStringList(m.toString())));
            excavationBreakables.put(m, existingEntries);
        }
    }

    private Collection<Location> getExcavationPattern(Location originalSpot, BlockFace face, int type){
        Collection<Location> pattern = new HashSet<>();
        switch(type){
            case 1: {
                pattern.add(originalSpot);
                pattern.add(originalSpot.clone().add(0, -1, 0));
                break;
            }
            case 2: {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    pattern.add(originalSpot.clone().add(0, 1, 0));
                    pattern.add(originalSpot.clone().add(0, -1, 0));
                    pattern.add(originalSpot.clone().add(1, 0, 0));
                    pattern.add(originalSpot.clone().add(-1, 0, 0));
                } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                    pattern.add(originalSpot.clone().add(0, 1, 0));
                    pattern.add(originalSpot.clone().add(0, -1, 0));
                    pattern.add(originalSpot.clone().add(0, 0, 1));
                    pattern.add(originalSpot.clone().add(0, 0, -1));
                } else if (face == BlockFace.UP || face == BlockFace.DOWN) {
                    pattern.add(originalSpot.clone().add(0, 0, 1));
                    pattern.add(originalSpot.clone().add(0, 0, -1));
                    pattern.add(originalSpot.clone().add(1, 0, 0));
                    pattern.add(originalSpot.clone().add(-1, 0, 0));
                }
                pattern.add(originalSpot);
                break;
            }
            default: {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    pattern.addAll(BlockUtils.getBlocksInArea(originalSpot.clone().add(type - 2, type - 2, 0), originalSpot.clone().add(-(type - 2), -(type - 2), 0)));
                } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                    pattern.addAll(BlockUtils.getBlocksInArea(originalSpot.clone().add(0, type - 2, type - 2), originalSpot.clone().add(0, -(type - 2), -(type - 2))));
                } else if (face == BlockFace.UP || face == BlockFace.DOWN) {
                    pattern.addAll(BlockUtils.getBlocksInArea(originalSpot.clone().add(type - 2, 0, type - 2), originalSpot.clone().add(-(type - 2), 0, -(type - 2))));
                }
            }
        }
        return pattern;
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.excavation.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.excavation.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.excavation.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.excavation";
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
        return config.getInt("enchantment_configuration.excavation.weight");
    }

    @Override
    public int getMaxLevel() {
        return areaLeveling ? config.getInt("enchantment_configuration.excavation.max_level") : 1;
    }

    @Override
    public int getMaxTableLevel() {
        return areaLeveling ? config.getInt("enchantment_configuration.excavation.max_level_table") : 1;
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.excavation.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.excavation.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.excavation.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.excavation.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.excavation.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.excavation.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.excavation.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-excavation";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double durabilityMultiplier;
    private final boolean nerfExcavationSpeed;
    private final int fatigueAmplifier;
    private final int fatigueDuration;
    private final boolean allowSneakDisable;
    private final boolean areaLeveling;
    private final boolean durabilityLeveling;
    private final double durabilityMultiplierLv;
    private final Map<MaterialClassType, Collection<Material>> excavationBreakables = new HashMap<>();
    private final Map<UUID, BlockFace> blockFaceMap = new HashMap<>();
    private final Map<UUID, Double> excavatingPlayers = new HashMap<>(); // this map is used to track when players are using
    // excavation, and should take the double value as fraction item durability damage while doing so
    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;
        if (allowSneakDisable && e.getPlayer().isSneaking()) return;
        ItemStack heldTool = e.getPlayer().getInventory().getItemInMainHand();
        Collection<Material> breakableBlocks = excavationBreakables.getOrDefault(MaterialClassType.getClass(heldTool.getType()), new HashSet<>());
        if (!breakableBlocks.contains(e.getBlock().getType()) || excavatingPlayers.containsKey(e.getPlayer().getUniqueId())) return; // not a breakable block, do nothing
        BlockFace face = blockFaceMap.get(e.getPlayer().getUniqueId());
        if (face != null) {
            Collection<Location> blocksToBreak = getExcavationPattern(e.getBlock().getLocation(), face, areaLeveling ? level : 3);
            double durabilityFraction = durabilityLeveling ? (durabilityMultiplier + ((level - 1) * durabilityMultiplierLv)) : durabilityMultiplier;

            if (!blocksToBreak.isEmpty()){
                blockFaceMap.remove(e.getPlayer().getUniqueId());
                excavatingPlayers.put(e.getPlayer().getUniqueId(), durabilityFraction);
                e.setCancelled(true);
                for (Location l : blocksToBreak){
                    if (!EnchantsSquared.isWorldGuardAllowed(e.getPlayer(), l, getWorldGuardFlagName())) continue;
                    if (breakableBlocks.contains(e.getPlayer().getWorld().getBlockAt(l).getType())){
                        if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
                            e.getPlayer().getWorld().getBlockAt(l).setType(Material.AIR);
                        else
                            BlockUtils.breakBlock(e.getPlayer(), e.getPlayer().getWorld().getBlockAt(l));
                    }
                }
                excavatingPlayers.remove(e.getPlayer().getUniqueId());

                if (nerfExcavationSpeed){
                    PotionEffect existingEffect = e.getPlayer().getPotionEffect(PotionEffectType.SLOW_DIGGING);
                    if (existingEffect != null){
                        if (existingEffect.getAmplifier() <= fatigueAmplifier){
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, fatigueDuration, fatigueAmplifier, true, false, false));
                        }
                    } else {
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, fatigueDuration, fatigueAmplifier, true, false, false));
                    }
                }
            }
        }
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        // do nothing
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockFaceClick(PlayerInteractEvent e){
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if (e.getClickedBlock() != null){
                blockFaceMap.put(e.getPlayer().getUniqueId(), e.getBlockFace());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemTakeDamage(PlayerItemDamageEvent e){
        if (e.isCancelled()) return;
        if (e.getDamage() > 0 && excavatingPlayers.containsKey(e.getPlayer().getUniqueId())){
            double multiplier = excavatingPlayers.getOrDefault(e.getPlayer().getUniqueId(), durabilityMultiplier);
            if (Utils.getRandom().nextDouble() > multiplier) {
                e.setCancelled(true);
            }
        }
    }
}
