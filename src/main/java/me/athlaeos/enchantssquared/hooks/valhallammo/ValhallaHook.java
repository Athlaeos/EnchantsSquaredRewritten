package me.athlaeos.enchantssquared.hooks.valhallammo;
import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.StatCollector;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ValhallaHook {
    public static void registerValhallaEnchantments(){
        YamlConfiguration valhallaConfig = ConfigManager.getInstance().getConfig("config_valhallammo.yml").get();
        ConfigurationSection section = valhallaConfig.getConfigurationSection("enchantments");
        if (section != null){
            int registered = 0;
            for (String key : section.getKeys(false)){
                int id = valhallaConfig.getInt("enchantments." + key + ".id");
                if (CustomEnchantManager.getInstance().getAllEnchants().containsKey(id)) {
                    EnchantsSquared.getPlugin().getServer().getLogger().warning("Enchantment " + key + " has id " + id + ", but it was already registered!");
                    continue;
                }
                GenericValhallaStatEnchantment newEnchantment = new GenericValhallaStatEnchantment(id, key);

                ConfigurationSection statSection = valhallaConfig.getConfigurationSection("enchantments." + key + ".stats");
                if (statSection != null){
                    for (String stat : statSection.getKeys(false)){
                        double base = valhallaConfig.getDouble("enchantments." + key + ".stats." + stat + ".base");
                        double lv = valhallaConfig.getDouble("enchantments." + key + ".stats." + stat + ".lv");
                        StatCollector collector = AccumulativeStatManager.getSources().get(stat);
                        if (collector != null){
                            if (collector.getStatSources().stream()
                                    .anyMatch(source -> source instanceof EvEAccumulativeStatSource)){
                                if (collector.isAttackerPossessive()){
                                    AccumulativeStatManager.register(stat, new OffensiveEnchantmentStatSource(newEnchantment, base, lv));
                                } else {
                                    AccumulativeStatManager.register(stat, new DefensiveEnchantmentStatSource(newEnchantment, base, lv));
                                }
                            } else {
                                AccumulativeStatManager.register(stat, new EnchantmentStatSource(newEnchantment, base, lv));
                            }
                        }
                    }
                }

                registered++;
                CustomEnchantManager.getInstance().registerEnchant(newEnchantment);
            }

            EnchantsSquared.getPlugin().getServer().getLogger().info("ValhallaMMO enchantments loaded in! " + registered + " were registered.");
        }
    }

    public static void registerEnchantmentModifier(CustomEnchant enchant){
        ModifierRegistry.register(new CustomEnchantmentAddModifier(enchant));
    }
}
