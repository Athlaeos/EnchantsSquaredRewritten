package me.athlaeos.enchantssquared.enchantments.regular_interval;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromAllEquipment;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashSet;

public class ReinforcedPlating extends CustomEnchant implements TriggerOnRegularIntervalsEnchantment, Listener {
    private final boolean cleanup;
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
    public ReinforcedPlating(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.reinforced_plating.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.reinforced_plating.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.reinforced_plating.incompatible_custom_enchantments"));

        this.armorBase = config.getDouble("enchantment_configuration.reinforced_plating.armor_base");
        this.armorLv = config.getDouble("enchantment_configuration.reinforced_plating.armor_lv");

        this.cleanup = config.getBoolean("clean_old_enchantments");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.reinforced_plating.icon", new ItemStack(Material.IRON_BLOCK));

        EnchantsSquared.getPlugin().getServer().getPluginManager().registerEvents(this, EnchantsSquared.getPlugin());
    }

    private final LevelService levelService = new LevelsFromAllEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.reinforced_plating.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.reinforced_plating.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.reinforced_plating.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.reinforced_plating";
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
        return config.getInt("enchantment_configuration.reinforced_plating.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.reinforced_plating.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.reinforced_plating.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.reinforced_plating.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.reinforced_plating.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.reinforced_plating.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.reinforced_plating.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.reinforced_plating.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.reinforced_plating.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.reinforced_plating.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-reinforced-plating";
    }

    private final double armorBase;
    private final double armorLv;

    @Override
    public long getInterval() {
        return 20;
    }

    @Override
    public void execute(Entity e, int level) {
        if (!(e instanceof LivingEntity)) return;
        if (shouldEnchantmentCancel(level, (LivingEntity) e, e.getLocation())) {
            EntityUtils.removeUniqueAttribute((LivingEntity) e, "es_reinforced_plating", Attribute.GENERIC_ARMOR);
            return;
        }

        double armorBoost = armorBase + ((level - 1) * armorLv);

        EntityUtils.addUniqueAttribute((LivingEntity) e, "es_reinforced_plating", Attribute.GENERIC_ARMOR, armorBoost,
                AttributeModifier.Operation.ADD_NUMBER);

        if (cleanup && e instanceof Player) stripEnchantmentAttributesFromAllEquipment((Player) e);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "es_reinforced_plating", Attribute.GENERIC_ARMOR);
    }

    private void stripEnchantmentAttributesFromAllEquipment(Player p){
        EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(p);
        if (equipment.getHelmetEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getHelmet());
        if (equipment.getChestplateEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getChestplate());
        if (equipment.getLeggingsEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getLeggings());
        if (equipment.getBootsEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getBoots());
        if (equipment.getMainHandEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getMainHand());
        if (equipment.getOffHandEnchantments().containsKey(this)) stripEnchantmentAttributesFromItem(equipment.getOffHand());
    }

    private void stripEnchantmentAttributesFromItem(ItemStack i){
        if (ItemUtils.isAirOrNull(i)) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        if (meta.getAttributeModifiers(Attribute.GENERIC_ARMOR) == null) return;
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
        i.setItemMeta(meta);
    }
}