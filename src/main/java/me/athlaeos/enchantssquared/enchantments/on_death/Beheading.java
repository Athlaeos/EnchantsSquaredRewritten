package me.athlaeos.enchantssquared.enchantments.on_death;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.HashSet;

public class Beheading extends CustomEnchant implements TriggerOnDeathEnchantment {
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
    public Beheading(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.beheading.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.beheading.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.beheading.incompatible_custom_enchantments"));

        this.beheadingBase = config.getDouble("enchantment_configuration.beheading.beheading_base");
        this.beheadingLv = config.getDouble("enchantment_configuration.beheading.beheading_lv");
        this.axeMultiplier = config.getDouble("enchantment_configuration.beheading.axe_buff");
        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.beheading.icon", new ItemStack(Material.IRON_AXE));
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.beheading.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.beheading.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.beheading.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.beheading";
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
        return config.getInt("enchantment_configuration.beheading.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.beheading.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.beheading.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.beheading.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.beheading.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.beheading.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.beheading.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.beheading.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.beheading.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.beheading.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-beheading";
    }

    private final double beheadingBase;
    private final double beheadingLv;
    private final double axeMultiplier;

    @Override
    public void onEntityDeath(EntityDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onEntityKilled(EntityDeathEvent e, int level) {
        if (e.getEntity().getKiller() == null || shouldEnchantmentCancel(level, e.getEntity().getKiller(), e.getEntity().getLocation())) return;
        double dropChance = beheadingBase + ((level - 1) * beheadingLv);
        if (e.getEntity().getKiller().getInventory().getItemInMainHand().getType().toString().contains("_AXE"))
            dropChance *= axeMultiplier;
        if (Utils.getRandom().nextDouble() <= dropChance){
            if (e.getEntity() instanceof Zombie){
                e.getDrops().add(new ItemStack(Material.ZOMBIE_HEAD));
            } else if (e.getEntity() instanceof WitherSkeleton){
                e.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
            } else if (e.getEntity() instanceof Skeleton){
                e.getDrops().add(new ItemStack(Material.SKELETON_SKULL));
            } else if (e.getEntity() instanceof Creeper){
                e.getDrops().add(new ItemStack(Material.CREEPER_HEAD));
            } else if (e.getEntity() instanceof HumanEntity){
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta == null) return;
                meta.setOwningPlayer((Player) e.getEntity());
                head.setItemMeta(meta);
                e.getDrops().add(head);
            }
        }
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerKilled(PlayerDeathEvent e, int level) {
        // do nothing
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent e, int level) {
        // do nothing
    }
}
