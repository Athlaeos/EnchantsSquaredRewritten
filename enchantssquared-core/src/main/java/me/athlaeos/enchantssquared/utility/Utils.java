package me.athlaeos.enchantssquared.utility;

import org.bukkit.Sound;

import java.util.Random;

public class Utils {

    private static Random random = null;

    public static Random getRandom(){
        if (random == null) random = new Random();
        return random;
    }

    public static int excessChance(double chance){
        boolean negative = chance < 0;
        int atLeast = (negative) ? (int) Math.ceil(chance) : (int) Math.floor(chance);
        double remainingChance = chance - atLeast;
        if (getRandom().nextDouble() <= Math.abs(remainingChance)) {
            if(negative) {
                atLeast--;
            } else{
                atLeast++;
            }
        }
        return atLeast;
    }

    public static Sound soundFromString(String sound, Sound def){
        if (sound == null) return def;
        try {
            return Sound.valueOf(sound);
        } catch (Exception ignored){
            return def;
        }
    }
}
