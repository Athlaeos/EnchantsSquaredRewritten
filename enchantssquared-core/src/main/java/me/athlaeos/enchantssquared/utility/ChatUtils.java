package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.domain.Version;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    public static String chat (String s) {
        if (s == null) return "";
        if (Version.currentVersionOrNewerThan(Version.MINECRAFT_1_16)){
            return newChat(s);
        } else {
            return oldChat(s);
        }
    }

    public static String oldChat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message + "");
    }
    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String newChat(String message) {
        char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return oldChat(matcher.appendTail(buffer).toString());
    }

    public static String toRoman(int number) {
        if (number == 0) return "0";
        if (number == 1) return "I";
        if (romanNumeralMap.isEmpty()) {
            populateRomanMap();
        }
        int l =  romanNumeralMap.floorKey(number);
        if ( number == l ) {
            return romanNumeralMap.get(number);
        }
        return romanNumeralMap.get(l) + toRoman(number-l);
    }

    private final static TreeMap<Integer, String> romanNumeralMap = new TreeMap<>();

    private static void populateRomanMap(){
        romanNumeralMap.put(1000, "M");
        romanNumeralMap.put(900, "CM");
        romanNumeralMap.put(500, "D");
        romanNumeralMap.put(400, "CD");
        romanNumeralMap.put(100, "C");
        romanNumeralMap.put(90, "XC");
        romanNumeralMap.put(50, "L");
        romanNumeralMap.put(40, "XL");
        romanNumeralMap.put(10, "X");
        romanNumeralMap.put(9, "IX");
        romanNumeralMap.put(5, "V");
        romanNumeralMap.put(4, "IV");
        romanNumeralMap.put(1, "I");
    }

    public static Map<Integer, ArrayList<String>> paginateTextList(int pageSize, List<String> allEntries) {
        Map<Integer, ArrayList<String>> pages = new HashMap<>();
        int stepper = 0;

        for (int pageNumber = 0; pageNumber < Math.ceil((double)allEntries.size()/(double)pageSize); pageNumber++) {
            ArrayList<String> pageEntries = new ArrayList<>();
            for (int pageEntry = 0; pageEntry < pageSize && stepper < allEntries.size(); pageEntry++, stepper++) {
                pageEntries.add(allEntries.get(stepper));
            }
            pages.put(pageNumber, pageEntries);
        }
        return pages;
    }

    public static List<String> seperateStringIntoLines(String string, int maxLength){
        List<String> lines = new ArrayList<>();
        String[] words = string.split(" ");
        if (words.length == 0) return lines;
        StringBuilder sentence = new StringBuilder();
        for (String s : words){
            if (sentence.length() + s.length() > maxLength || s.contains("\n")){
                lines.add(sentence.toString());
                String previousSentence = sentence.toString();
                sentence = new StringBuilder();
                sentence.append(chat(org.bukkit.ChatColor.getLastColors(chat(previousSentence)))).append(s);
            } else if (words[0].equals(s)){
                sentence.append(s);
            } else {
                sentence.append(" ").append(s);
            }
            if (words[words.length - 1].equals(s)){
                lines.add(sentence.toString());
            }
        }
        return lines;
    }

    public static Color hexToRgb(String colorStr) {
        return Color.fromRGB(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 )
        );
    }

    public static int hexToDec(String hex){
        return Integer.parseInt(hex, 16);
    }
}
