package me.athlaeos.enchantssquared.enchantments.on_block_break;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.Offset;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.utility.BlockUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.List;

public class TreeFeller extends CustomEnchant implements TriggerOnBlockBreakEnchantment, Listener {
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
    public TreeFeller(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.tree_feller.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.tree_feller.incompatible_custom_enchantments"));

        this.durabilityMultiplier = config.getDouble("enchantment_configuration.tree_feller.damage_multiplier");
        this.logLimit = config.getInt("enchantment_configuration.tree_feller.log_limit");
        this.leafLimit = config.getInt("enchantment_configuration.tree_feller.leaf_limit");
        this.includeLeaves = config.getBoolean("enchantment_configuration.tree_feller.break_leaves");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.tree_feller.icon", createIcon(Material.DIAMOND_AXE));

        EnchantsSquared.getPlugin().getServer().getPluginManager().registerEvents(this, EnchantsSquared.getPlugin());
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.tree_feller.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.tree_feller.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.tree_feller.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.tree_feller";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material.toString().endsWith("_AXE");
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material.toString().endsWith("_AXE");
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.tree_feller.weight");
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
        return config.getBoolean("enchantment_configuration.tree_feller.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.tree_feller.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.tree_feller.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.tree_feller.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.tree_feller.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return 0;
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return 0;
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-tree-feller";
    }

    @Override
    public Collection<String> getCompatibleItems() {
        return Collections.singletonList("AXES");
    }

    private final double durabilityMultiplier;
    private final boolean includeLeaves;
    private final int logLimit;
    private final int leafLimit;
    private final Map<UUID, Double> fellingPlayers = new HashMap<>(); // this map is used to track when players are using
    // tree feller, and should take the double value as fraction item durability damage while doing so

    private final Collection<Material> logs = ItemUtils.getMaterialList(Arrays.asList("OAK_LOG", "SPRUCE_LOG",
            "BIRCH_LOG", "ACACIA_LOG", "JUNGLE_LOG", "DARK_OAK_LOG", "MANGROVE_LOG", "CHERRY_BLOSSOM_LOG",
            "OAK_WOOD", "SPRUCE_WOOD", "BIRCH_WOOD", "ACACIA_WOOD", "JUNGLE_WOOD", "DARK_OAK_WOOD", "MANGROVE_WOOD",
            "CHERRY_BLOSSOM_WOOD", "WARPED_STEM", "CRIMSON_STEM", "WARPED_HYPHAE", "CRIMSON_HYPHAE"));
    private final Collection<Material> leaves = ItemUtils.getMaterialList(Arrays.asList("OAK_LEAVES", "SPRUCE_LEAVES",
            "BIRCH_LEAVES", "ACACIA_LEAVES", "JUNGLE_LEAVES", "DARK_OAK_LEAVES", "MANGROVE_LEAVES",
            "CHERRY_BLOSSOM_LEAVES", "NETHER_WART_BLOCK", "WARPED_WART_BLOCK"));

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;
        if (!e.getPlayer().isSneaking() || fellingPlayers.containsKey(e.getPlayer().getUniqueId())) return;

        if (logs.contains(e.getBlock().getType())) {
            if (isTree(e.getBlock())){
                fellingPlayers.put(e.getPlayer().getUniqueId(), durabilityMultiplier);
                ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();
                List<Block> treeBlocks = BlockUtils.getBlockVein(
                        e.getBlock().getLocation(),
                        new HashSet<>(logs),
                        logLimit,
                        new Offset(-1, 1, -1), new Offset(-1, 1, 0), new Offset(-1, 1, 1),
                        new Offset(0, 1, -1), new Offset(0, 1, 0), new Offset(0, 1, 1),
                        new Offset(1, 1, -1), new Offset(1, 1, 0), new Offset(1, 1, 1),
                        new Offset(-1, 0, -1), new Offset(-1, 0, 0), new Offset(-1, 0, 1),
                        new Offset(0, 0, -1), new Offset(0, 0, 1),
                        new Offset(1, 0, -1), new Offset(1, 0, 0), new Offset(1, 0, 1));
                treeBlocks.forEach(b -> e.getPlayer().breakBlock(b));

                EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
                    List<Block> leaves = includeLeaves ? getTreeLeaves(e.getBlock()) : new ArrayList<>();
                    leaves.stream().filter(b -> {
                        if (b.getBlockData() instanceof Leaves){
                            return ((Leaves) b.getBlockData()).getDistance() > 2;
                        }
                        return false;
                    }).forEach(b -> b.breakNaturally(heldItem));
                    fellingPlayers.remove(e.getPlayer().getUniqueId());
                }, 5L);
            }
        }
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        // do nothing
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemTakeDamage(PlayerItemDamageEvent e){
        if (e.isCancelled()) return;
        if (e.getDamage() > 0 && fellingPlayers.containsKey(e.getPlayer().getUniqueId())){
            double multiplier = fellingPlayers.getOrDefault(e.getPlayer().getUniqueId(), durabilityMultiplier);
            if (Utils.getRandom().nextDouble() > multiplier) {
                e.setCancelled(true);
            }
        }
    }


    private boolean isTree(Block b){
        Block currentBlock;
        // check up to 48 blocks above the log mined
        for (int i = 1; i < 48; i++){
            if (b.getLocation().getY() + i >= b.getWorld().getMaxHeight()) break;
            currentBlock = b.getLocation().add(0, i, 0).getBlock();
            if (i < 4 && !logs.contains(currentBlock.getType())) return false; // at least 4 log blocks must be found before a leaf or air block
            if (leaves.contains(currentBlock.getType())) return true; // if leaf blocks are found, it is a tree
            if (!currentBlock.getType().isAir()){
                if (!logs.contains(currentBlock.getType())) break;
            }
            // from bottom to up, L = log, V = leaves, A = air
            // LLLAAVV is not considered a tree
            // LLLVVAA is considered a tree (AA not scanned)
        }
        return false;
    }

    private List<Block> getTreeLeaves(Block b){
        Block currentBlock;
        // check up to 48 blocks above the log mined
        for (int i = 1; i < 48; i++){
            if (b.getLocation().getY() + i >= b.getWorld().getMaxHeight()) break;
            currentBlock = b.getLocation().add(0, i, 0).getBlock();

            if (leaves.contains(currentBlock.getType())) {
                return BlockUtils.getBlockVein(
                        currentBlock.getLocation(),
                        new HashSet<>(this.leaves),
                        leafLimit,
                        block -> true,
                        new Offset(0, 1, 0),
                        new Offset(-1, -1, 0), new Offset(-1, 0, 0), new Offset(-1, 1, 0),
                        new Offset(0, -1, -1), new Offset(0, 0, -1), new Offset(0, 1, -1),
                        new Offset(0, -1, 1), new Offset(0, 0, 1), new Offset(0, 1, 1),
                        new Offset(1, -1, 0), new Offset(1, 0, 0), new Offset(1, 1, 0),
                        new Offset(0, -1, 0),
                        new Offset(0, -2, 0),
                        new Offset(0, -3, 0)); // if leaf blocks are found, it is a tree
            }
        }
        return new ArrayList<>();
    }
}
