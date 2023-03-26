package me.athlaeos.enchantssquared.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerOptionsManager {
    private final Map<Player, Boolean> wantsCustomEnchants = new HashMap<>();
    private static PlayerOptionsManager manager = null;

    public PlayerOptionsManager(){

    }

    public static PlayerOptionsManager getManager(){
        if (manager == null){
            manager = new PlayerOptionsManager();
        }
        return manager;
    }

    public void togglePlayerWantsEnchants(Player p){
        if (!wantsCustomEnchants.containsKey(p)){
            wantsCustomEnchants.put(p, false);
        } else {
            if (wantsCustomEnchants.get(p)){
                wantsCustomEnchants.put(p, false);
            } else {
                wantsCustomEnchants.put(p, true);
            }
        }
    }

    public boolean doesPlayerWantEnchants(Player p){
        return wantsCustomEnchants.getOrDefault(p, true);
    }
}
