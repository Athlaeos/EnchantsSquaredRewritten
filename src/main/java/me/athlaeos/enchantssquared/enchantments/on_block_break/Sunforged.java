package me.athlaeos.enchantssquared.enchantments.on_block_break;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.LevelService;
import me.athlaeos.enchantssquared.enchantments.LevelsFromMainHandAndEquipment;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Sunforged extends CustomEnchant implements TriggerOnBlockBreakEnchantment {
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
    public Sunforged(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.excavation.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.sunforged.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.sunforged.incompatible_custom_enchantments"));

        this.avgExpDropped = config.getDouble("enchantment_configuration.sunforged.drop_exp_chance");
        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.sunforged.icon", new ItemStack(Material.FURNACE));

        YamlConfiguration smeltConfig = ConfigManager.getInstance().getConfig("smeltblocksrecipes.yml").get();
        ConfigurationSection fortuneLessSection = smeltConfig.getConfigurationSection("fortune_ignored");
        if (fortuneLessSection != null){
            for (String s : fortuneLessSection.getKeys(false)){
                try{
                    String recipeResult = smeltConfig.getString("fortune_ignored."+s);
                    if (recipeResult == null) continue;
                    fortuneLessDrops.put(Material.matchMaterial(s), Material.matchMaterial(recipeResult));
                } catch (IllegalArgumentException | NullPointerException ignored){
                }
            }
        }
        ConfigurationSection fortuneSection = smeltConfig.getConfigurationSection("fortune_affected");
        if (fortuneSection != null){
            for (String s : fortuneSection.getKeys(false)){
                try{
                    String recipeResult = smeltConfig.getString("fortune_affected."+s);
                    if (recipeResult == null) continue;
                    fortuneDrops.put(Material.matchMaterial(s), Material.matchMaterial(recipeResult));
                } catch (IllegalArgumentException | NullPointerException ignored){
                }
            }
        }
    }

    private final LevelService levelService = new LevelsFromMainHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return levelService;
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.sunforged.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.sunforged.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.sunforged.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.sunforged";
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
        return config.getInt("enchantment_configuration.sunforged.weight");
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
        return config.getBoolean("enchantment_configuration.sunforged.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.sunforged.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.sunforged.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.sunforged.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.sunforged.trade_cost_base_upper");
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
        return "es-deny-sunforged";
    }

    private final double avgExpDropped;

    @Override
    public void onBlockBreak(BlockBreakEvent e, int level) {
        // do nothing
    }


    private final Map<Material, Material> fortuneLessDrops = new HashMap<>();
    private final Map<Material, Material> fortuneDrops = new HashMap<>();
    @Override
    public void onBlockDropItem(BlockDropItemEvent e, int level) {
        if (shouldEnchantmentCancel(level, e.getPlayer(), e.getBlock().getLocation())) return;
        ItemStack pickaxe = e.getPlayer().getInventory().getItemInMainHand();
        boolean dropExp = false;
        int fortuneLevel = pickaxe.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        for (Item i : e.getItems()){
            ItemStack drop = i.getItemStack();
            if (!drop.isSimilar(new ItemStack(drop.getType()))) continue; // if the item is not simple, don't do anything
            if (fortuneLessDrops.containsKey(drop.getType())){
                drop.setType(fortuneLessDrops.get(drop.getType()));
                i.setItemStack(drop);
                dropExp = true;
            } else if (fortuneDrops.containsKey(drop.getType())){
                int extra = (Utils.getRandom().nextInt(fortuneLevel + 1));
                drop.setType(fortuneDrops.get(drop.getType()));
                drop.setAmount(drop.getAmount() + extra);
                i.setItemStack(drop);
                dropExp = true;
            }
            if (dropExp){
                int expToDrop = Utils.excessChance(Math.min(1, fortuneLevel * avgExpDropped));
                if (expToDrop <= 0) return;
                ExperienceOrb orb = (ExperienceOrb) e.getBlock().getWorld().spawnEntity(e.getBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.EXPERIENCE_ORB);
                orb.setExperience(expToDrop);
            }
        }
    }
}
