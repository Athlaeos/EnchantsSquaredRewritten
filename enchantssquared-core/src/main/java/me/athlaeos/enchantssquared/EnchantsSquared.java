package me.athlaeos.enchantssquared;

import me.athlaeos.enchantssquared.commands.CommandManager;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.config.ConfigUpdater;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.domain.Version;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.hooks.WorldGuardHook;
import me.athlaeos.enchantssquared.hooks.valhallammo.ValhallaHook;
import me.athlaeos.enchantssquared.listeners.*;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.AnimationRegistry;
import me.athlaeos.enchantssquared.managers.RegularIntervalEnchantmentClockManager;
import me.athlaeos.enchantssquared.menus.MenuListener;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.version.NMS;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public final class EnchantsSquared extends JavaPlugin {
    private static EnchantsSquared plugin = null;
    private static boolean trinketsHooked = false;
    private static boolean valhallaHooked = false;
    private static boolean worldGuardHooked = false;
    private static boolean jobsHooked = false;
    private static NMS nms = null;

    private static boolean grindstonesEnabled = true;

    private GrindstoneListener grindstoneListener = null;
    private AnvilListener anvilListener = null;
    private EnchantListener enchantListener = null;
    private VillagerClickListener villagerClickListener = null;
    private PlayerFishingLootListener fishingLootListener = null;
    private PlayerFishingEnchantmentListener fishingEnchantListener = null;
    private ChestOpenListener chestOpenListener = null;
    private BlockBreakListener blockBreakListener = null;
    private InteractListener interactListener = null;
    private HealthRegenerationListener healthRegenerationListener = null;
    private EntityDamageListener entityDamageListener = null;
    private EntityDeathListener entityDeathListener = null;
    private EntityPotionEffectListener potionEffectListener = null;
    private ProjectileListener projectileListener = null;
    private MenuListener menuListener = null;
    private ItemDamageListener itemDamageListener = null;

    @Override
    public void onLoad() {
        plugin = this;
        super.onLoad();

        saveConfig("config.yml");

        valhallaHooked = hasPlugin("ValhallaMMO");
        if (valhallaHooked){
            this.getLogger().info("ValhallaMMO hooked! Adding a bunch of cool stuff.");
            saveConfig("config_valhallammo.yml");
        }
        worldGuardHooked = hasPlugin("WorldGuard");
        if (worldGuardHooked){
            WorldGuardHook.getHook().registerFlags();
        }
        jobsHooked = hasPlugin("Jobs");
        trinketsHooked = hasPlugin("ValhallaTrinkets");
    }

    private boolean hasPlugin(String plugin){
        return Arrays.stream(getServer().getPluginManager().getPlugins()).anyMatch(p -> p.getName().equals(plugin));
    }

    @Override
    public void onEnable() {
        plugin = this;
        if (!setupNMS()){
            logSevere("This version of Minecraft is not compatible with EnchantsSquared. Sorry!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveConfig("excavationblocks.yml");
        saveConfig("smeltblocksrecipes.yml");
        saveAndUpdateConfig("translations.yml");

        if (ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("metrics", true)){
            new Metrics(this, 10596);
        }

        grindstonesEnabled = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("allow_grindstone_removal", true);
        CommandManager.getInstance();

        if (Version.currentVersionOrNewerThan(Version.MINECRAFT_1_14) && grindstonesEnabled) grindstoneListener = registerListener(new GrindstoneListener());
        anvilListener = registerListener(new AnvilListener(), "disable_anvil");
        enchantListener = registerListener(new EnchantListener(), "disable_enchanting");
        villagerClickListener = registerListener(new VillagerClickListener(), "disable_trading");
        fishingLootListener = registerListener(new PlayerFishingLootListener(), "disable_fishing");
        chestOpenListener = registerListener(new ChestOpenListener(), "disable_dungeonlootgen");
        fishingEnchantListener = registerListener(new PlayerFishingEnchantmentListener());
        blockBreakListener = registerListener(new BlockBreakListener());
        interactListener = registerListener(new InteractListener());
        healthRegenerationListener = registerListener(new HealthRegenerationListener());
        entityDamageListener = registerListener(new EntityDamageListener());
        entityDeathListener = registerListener(new EntityDeathListener());
        potionEffectListener = registerListener(new EntityPotionEffectListener());
        menuListener = registerListener(new MenuListener());
        itemDamageListener = registerListener(new ItemDamageListener());
        projectileListener = registerListener(new ProjectileListener());
        registerListener(new HandSwitchListener());
        registerListener(new LeaveJoinListener());
        registerListener(new ArmorSwitchListener());
        registerListener(new MovementListener());

        AnimationRegistry.registerDefaults();

        // enchantment registry for the ones that are also listeners or that need special tasks registered on enable
        for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()){
            if (enchant instanceof Listener){
                getServer().getPluginManager().registerEvents((Listener) enchant, this);
            }
            enchant.onPluginEnable();
        }
        RegularIntervalEnchantmentClockManager.startClock();
        if (valhallaHooked) ValhallaHook.registerValhallaEnchantments();
    }

    public static NMS getNms() {
        return nms;
    }

    public static boolean isWorldGuardAllowed(LivingEntity p, Location l, String flag){
        if (!worldGuardHooked) return true;
        return p.hasPermission("es.noregionrestrictions") ||
                !WorldGuardHook.getHook().isLocationInFlaggedRegion(l, flag);
    }

    private <T extends Listener> T registerListener(T listener){
        this.getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    private <T extends Listener> T registerListener(T listener, String disablerKey){
        if (!ConfigManager.getInstance().getConfig("config.yml").get().getBoolean(disablerKey, false)){
            return registerListener(listener);
        }
        return null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (CustomEnchant e : CustomEnchantManager.getInstance().getAllEnchants().values()){
            if (e instanceof AttributeEnchantment){
                AttributeEnchantment attributeEnchantment = (AttributeEnchantment) e;
                for (Player p : getServer().getOnlinePlayers()){
                    attributeEnchantment.cleanAttribute(p);
                }
            }
        }
    }

    public static EnchantsSquared getPlugin(){
        return plugin;
    }

    public static boolean isValhallaHooked() {
        return valhallaHooked;
    }

    public static boolean isTrinketsHooked() {
        return trinketsHooked;
    }

    public static boolean isJobsHooked() {
        return jobsHooked;
    }

    public static boolean isWorldGuardHooked() {
        return worldGuardHooked;
    }

    private void saveAndUpdateConfig(String config){
        saveConfig(config);
        updateConfig(config);
    }


    public void saveConfig(String name){
        save(name);
        ConfigManager.getInstance().saveConfig(name);
    }

    public void save(String name){
        File file = new File(this.getDataFolder(), name);
        if (!file.exists()) this.saveResource(name, false);
    }

    private void updateConfig(String name){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(plugin, name, configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EnchantListener getEnchantListener() {
        return enchantListener;
    }

    public AnvilListener getAnvilListener() {
        return anvilListener;
    }

    public EntityDamageListener getEntityDamageListener() {
        return entityDamageListener;
    }

    public BlockBreakListener getBlockBreakListener() {
        return blockBreakListener;
    }

    public ChestOpenListener getChestOpenListener() {
        return chestOpenListener;
    }

    public EntityDeathListener getEntityDeathListener() {
        return entityDeathListener;
    }

    public EntityPotionEffectListener getPotionEffectListener() {
        return potionEffectListener;
    }

    public GrindstoneListener getGrindstoneListener() {
        return grindstoneListener;
    }

    public HealthRegenerationListener getHealthRegenerationListener() {
        return healthRegenerationListener;
    }

    public InteractListener getInteractListener() {
        return interactListener;
    }

    public MenuListener getMenuListener() {
        return menuListener;
    }

    public PlayerFishingLootListener getFishingLootListener() {
        return fishingLootListener;
    }

    public VillagerClickListener getVillagerClickListener() {
        return villagerClickListener;
    }

    public ItemDamageListener getItemDamageListener() {
        return itemDamageListener;
    }

    public PlayerFishingEnchantmentListener getFishingEnchantListener() {
        return fishingEnchantListener;
    }

    public ProjectileListener getProjectileListener() {
        return projectileListener;
    }

    public static boolean isGrindstonesEnabled() {
        return grindstonesEnabled;
    }

    public static void setGrindstonesEnabled(boolean grindstonesEnabled) {
        EnchantsSquared.grindstonesEnabled = grindstonesEnabled;
    }

    private boolean setupNMS() {
        try {
            String nmsVersion = MinecraftVersion.getServerVersion().getNmsVersion();
            if (nmsVersion == null) {
                return false;
            }
            Class<?> clazz = Class.forName("me.athlaeos.NMS_" + nmsVersion);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }
            return nms != null;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static void logInfo(String info){
        plugin.getServer().getLogger().info("[EnchantsSquared] " + info);
    }

    public static void logWarning(String warning){
        plugin.getServer().getLogger().warning("[EnchantsSquared] " + warning);
    }
    public static void logFine(String message){
        plugin.getServer().getLogger().fine("[EnchantsSquared] " + message);
        plugin.getServer().getConsoleSender().sendMessage(ChatUtils.chat("&a[EnchantsSquared] " + message));
    }

    public static void logSevere(String disaster){
        plugin.getServer().getLogger().severe("[EnchantsSquared] " + disaster);
    }
}
