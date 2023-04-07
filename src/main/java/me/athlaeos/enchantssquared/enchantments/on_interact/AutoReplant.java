package me.athlaeos.enchantssquared.enchantments.on_interact;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandOnly;
import me.athlaeos.enchantssquared.enchantments.on_block_break.TriggerOnBlockBreakEnchantment;
import me.athlaeos.enchantssquared.utility.BlockUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AutoReplant extends CustomEnchant implements TriggerOnInteractEnchantment, TriggerOnBlockBreakEnchantment {
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
    public AutoReplant(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.auto_replant.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.auto_replant.incompatible_custom_enchantments"));

        this.ignoreBreakPermissions = config.getBoolean("enchantment_configuration.auto_replant.ignore_break_permission");
        this.icon = ItemUtils.getIconFromConfig(config, "enchantment_configuration.auto_replant.icon", createIcon(Material.DIAMOND_HOE));
    }

    private final LevelService levelService = new LevelsFromMainHandOnly(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.auto_replant.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.auto_replant.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.auto_replant.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.auto_replant";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    @Override
    public boolean isNaturallyCompatible(Material material) {
        return material.toString().contains("_HOE");
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return material.toString().contains("_HOE");
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.auto_replant.weight");
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
        return config.getBoolean("enchantment_configuration.auto_replant.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.auto_replant.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.auto_replant.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.auto_replant.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.auto_replant.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.auto_replant.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.auto_replant.trade_cost_base_upper");
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
        return Collections.singletonList("HOES");
    }

    private final boolean ignoreBreakPermissions;

    private final Collection<Material> legalCrops = ItemUtils.getMaterialList(Arrays.asList(
            "CARROTS", "POTATOES", "WHEAT", "BEETROOTS", "NETHER_WART", "COCOA"
    ));

    @Override
    public void onInteract(PlayerInteractEvent e, int level) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getHand() != EquipmentSlot.HAND ||
                clickedBlock == null ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !legalCrops.contains(clickedBlock.getType()) ||
                shouldEnchantmentCancel(level, e.getPlayer(), clickedBlock.getLocation())) return;
        Ageable crop = (Ageable) clickedBlock.getBlockData();
        if (crop.getAge() >= crop.getMaximumAge()){
            if (ignoreBreakPermissions) {
                BlockBreakEvent breakEvent = new BlockBreakEvent(clickedBlock, e.getPlayer());
                EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(breakEvent);
                clickedBlock.breakNaturally(e.getPlayer().getInventory().getItemInMainHand());
            }
            else BlockUtils.breakBlock(e.getPlayer(), clickedBlock);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        Block brokenBlock = e.getBlock();
        if (!legalCrops.contains(brokenBlock.getType()) ||
                shouldEnchantmentCancel(level, e.getPlayer(), brokenBlock.getLocation())) return;
        Ageable crop = (Ageable) brokenBlock.getBlockData();
        if (crop.getAge() >= crop.getMaximumAge()){
            Material originalCrop = brokenBlock.getType();
            EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () ->
                    brokenBlock.getLocation().getBlock().setType(originalCrop),2L);
        } else {
            e.setCancelled(true);
        }
    }

    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        // do nothing
    }
}
