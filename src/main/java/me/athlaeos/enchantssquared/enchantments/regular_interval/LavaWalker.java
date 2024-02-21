package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.Levels1IfPresent;
import me.athlaeos.enchantssquared.enchantments.on_block_break.TriggerOnBlockBreakEnchantment;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.BlockUtils;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class LavaWalker extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, TriggerOnBlockBreakEnchantment {
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
    public LavaWalker(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.lava_walker.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.lava_walker.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.lava_walker.incompatible_custom_enchantments"));

        this.durabilityPerBlock = config.getDouble("enchantment_configuration.lava_walker.durability_degeneration");
        this.duration = config.getLong("enchantment_configuration.lava_walker.duration", -1);
        Material material;
        try {
            material = Material.valueOf(config.getString("enchantment_configuration.lava_walker.transform_into", "MAGMA_BLOCK"));
        } catch (IllegalArgumentException ignored){
            material = Material.MAGMA_BLOCK;
        }
        this.transformInto = material;

        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.lava_walker.icon", createIcon(Material.OBSIDIAN));
    }

    @Override
    public void onPluginEnable() {
        if (duration > 0){
            EnchantsSquared.getPlugin().getServer().getScheduler().runTaskTimer(EnchantsSquared.getPlugin(), () ->
                            new HashMap<>(convertedBlocks).entrySet().stream()
                                    .filter(e -> e.getValue() + duration < System.currentTimeMillis())
                                    .forEach(e -> {
                                        e.getKey().setType(Material.LAVA);
                                        convertedBlocks.remove(e.getKey());
                                    }),
                    0L, 5L);
        }
    }

    private final LevelService levelService = new Levels1IfPresent(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.lava_walker.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.lava_walker.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.lava_walker.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.lava_walker";
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
        return config.getInt("enchantment_configuration.lava_walker.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.lava_walker.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.lava_walker.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.lava_walker.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.lava_walker.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.lava_walker.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.lava_walker.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.lava_walker.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.lava_walker.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.lava_walker.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-lava-walker";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return naturallyCompatibleWith;
    }

    private final double durabilityPerBlock;
    private final long duration;
    private final Material transformInto;

    @Override
    public long getInterval() {
        return 3;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof Player)) return;
        if (shouldEnchantmentCancel(level, (Player) e, e.getLocation())) return;

        EntityEquipment cachedEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment((Player) e);
        Map<ItemStack, Map<CustomEnchant, Integer>> iterable = cachedEquipment.getIterableWithEnchantments(true);
        EntityUtils.SlotEquipment firstLavaWalkerItem = EntityUtils.getFirstEquipmentItemStackWithEnchantment(cachedEquipment, this);
        ItemStack i = firstLavaWalkerItem.getEquipment();
        if (ItemUtils.isAirOrNull(i)) return;
        int lavaWalkerLevel = iterable.getOrDefault(i, new HashMap<>()).getOrDefault(this, 0);
        if (lavaWalkerLevel > 0){
            Collection<Block> blocksToReplace = BlockUtils.getBlocksInArea(
                    e.getLocation().add(-(lavaWalkerLevel - 1), -0.8, -(lavaWalkerLevel - 1)),
                    e.getLocation().add((lavaWalkerLevel - 1), -0.8, (lavaWalkerLevel - 1)))
                    .stream().map(Location::getBlock)
                    .filter(b -> {
                        if (b.getType() == Material.LAVA && b.getBlockData() instanceof Levelled){
                            return ((Levelled) b.getBlockData()).getLevel() == 0 &&
                                    b.getLocation().add(0, 1, 0).getBlock().toString().contains("AIR");
                        }
                        return false;
                    }).collect(Collectors.toSet());
            if (i.getType().getMaxDurability() > 0 && i.getItemMeta() instanceof Damageable){
                int unbreakingLevel = i.getEnchantmentLevel(Enchantment.DURABILITY);
                int damage = Utils.excessChance(blocksToReplace.size() * durabilityPerBlock * (1D/(unbreakingLevel + 1D)));
                ItemUtils.damageItem((Player) e, i, damage, firstLavaWalkerItem.getSlot());
            }

            for (Block b : blocksToReplace){
                b.setType(transformInto);
                convertedBlocks.put(b, System.currentTimeMillis());
            }
        }
    }

    private final Map<Block, Long> convertedBlocks = new HashMap<>();

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        if (convertedBlocks.containsKey(e.getBlock())){
            e.setCancelled(true);
            e.getBlock().setType(Material.LAVA);
            convertedBlocks.remove(e.getBlock());
        }
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {

    }
}
