package me.athlaeos.enchantssquared.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private static WorldGuardHook hook = null;
    
    public static WorldGuardHook getHook() {
        if (hook == null) hook = new WorldGuardHook();
        return hook;
    }

    public void registerFlags(){
        registerFlag("es-deny-all");
        for (CustomEnchant e : CustomEnchantManager.getInstance().getAllEnchants().values()){
            registerFlag(e.getWorldGuardFlagName());
        }
    }

    private void registerFlag(String s){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(s, true);
            registry.register(flag);
        } catch (Exception e) {
            Flag<?> existing = registry.get(s);
            if (!(existing instanceof StateFlag)) {
                EnchantsSquared.getPlugin().getServer().getLogger().warning("Failed to register flag " + s);
                e.printStackTrace();
//                System.out.println("[EnchantsSquared] Something went wrong with WorldguardHook#setFlag for flag " + s + ", contact the plugin developer!");
                //Error message temporarily disabled until i can figure out why the fuck worldguard sometimes doesn't work
            }
        }
    }

    public boolean isLocationInFlaggedRegion(Location l, String flag){
        if (EnchantsSquared.isWorldGuardHooked()){
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            Flag<?> fuzzyFlag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flag);
            if (fuzzyFlag instanceof StateFlag){
                return !query.testState(BukkitAdapter.adapt(l), null, (StateFlag) fuzzyFlag);
            }
            return false;
        }
        return false;
    }

    public boolean isPVPDenied(Player p){
        if (!EnchantsSquared.isWorldGuardHooked()) return false;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return !query.testState(BukkitAdapter.adapt(p.getLocation()), WorldGuardPlugin.inst().wrapPlayer(p), Flags.PVP);
    }
}
