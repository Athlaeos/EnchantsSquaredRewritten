package me.athlaeos.enchantssquared.utility;

import org.bukkit.ChatColor;
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
        } catch (IllegalArgumentException ignored){
            return def;
        }
    }

    /**
     * Custom SC2 method that translates roman numerals into a numerical value.
     * @param i The string that represents the roman numeral value
     * @return The integer value of the roman numeral or 0 if the roman numeral is not in the range 1 <= x <= 20.
     */
    public static int translateRomanToLevel(String i){
        switch(i){
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            case "VI": return 6;
            case "VII": return 7;
            case "VIII": return 8;
            case "IX": return 9;
            case "X": return 10;
            case "XI": return 11;
            case "XII": return 12;
            case "XIII": return 13;
            case "XIV": return 14;
            case "XV": return 15;
            case "XVI": return 16;
            case "XVII": return 17;
            case "XVIII": return 18;
            case "XIX": return 19;
            case "XX": return 20;
            default: {
                try{
                    String s = i.replace("es.level.", "");
                    return Integer.parseInt(ChatColor.stripColor(s));
                } catch (IllegalArgumentException e){
                    return 0;
                }
            }
        }
    }

}
