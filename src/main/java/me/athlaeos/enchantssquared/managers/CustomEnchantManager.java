package me.athlaeos.enchantssquared.managers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.AnvilCombinationResult;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_attack.*;
import me.athlaeos.enchantssquared.enchantments.on_attacked.*;
import me.athlaeos.enchantssquared.enchantments.on_block_break.*;
import me.athlaeos.enchantssquared.enchantments.on_damaged.*;
import me.athlaeos.enchantssquared.enchantments.on_death.*;
import me.athlaeos.enchantssquared.enchantments.on_fishing.*;
import me.athlaeos.enchantssquared.enchantments.on_heal.*;
import me.athlaeos.enchantssquared.enchantments.on_interact.*;
import me.athlaeos.enchantssquared.enchantments.on_item_damage.*;
import me.athlaeos.enchantssquared.enchantments.on_potion_effect.*;
import me.athlaeos.enchantssquared.enchantments.on_shoot.RapidShot;
import me.athlaeos.enchantssquared.enchantments.regular_interval.*;
import me.athlaeos.enchantssquared.hooks.valhallammo.*;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CustomEnchantManager {
    private static CustomEnchantManager manager;

    private final BiMap<Integer, CustomEnchant> allEnchants;
    private static final NamespacedKey enchantmentsKey = new NamespacedKey(EnchantsSquared.getPlugin(), "es_enchantments");

    private final int maxEnchants;
    private final int maxEnchantsFromTable;
    private final boolean requirePermissions;
    private final int levelMinimum;
    private final boolean enableCosmeticGlint;
    private final boolean isUsingRomanNumerals;

    public CustomEnchantManager(){
        allEnchants = HashBiMap.create();
        YamlConfiguration config = ConfigManager.getInstance().getConfig("config.yml").get();

        this.requirePermissions = config.getBoolean("permission_required");
        this.maxEnchantsFromTable = config.getInt("enchantment_table_rolls");
        this.maxEnchants = config.getInt("max_enchants");
        this.levelMinimum = config.getInt("level_minimum");
        this.enableCosmeticGlint = config.getBoolean("enable_cosmetic_glint");
        this.isUsingRomanNumerals = config.getBoolean("level_as_roman");

        registerEnchants();
    }

    /**
     * Returns a map of random enchantments compatible with the given item.
     * This distribution is random and does not care for the amount of levels used. This was the method of enchanting
     * before 2.0.
     * This method does not consider a player, and so cannot check for permissions
     * @return a map of random enchantments compatible with the given item, or an empty map if the item is null or air.
     */
    public Map<CustomEnchant, Integer> getRandomEnchantments(ItemStack item, boolean treasure){
        return getRandomEnchantments(item, null, maxEnchantsFromTable, treasure, getCompatibleEnchants(item, GameMode.SURVIVAL));
    }

    public Map<CustomEnchant, Integer> getRandomEnchantments(ItemStack item, Player p, int maxRolls, boolean treasure, Collection<CustomEnchant> possibleEnchantments){
        Map<CustomEnchant, Integer> pickedEnchantments = getItemsEnchantsFromPDC(item);
        Collection<CustomEnchant> compatibleEnchantments = getCompatibleEnchants(item, GameMode.SURVIVAL);
        possibleEnchantments.removeIf(e -> !compatibleEnchantments.contains(e));
        possibleEnchantments.removeIf(e -> !treasure && e.isTreasure());

        List<Entry> entries = new ArrayList<>();
        double accumulatedWeight = 0.0;
        for (CustomEnchant c : possibleEnchantments){
            if (p != null && !c.hasPermission(p)) continue;
            accumulatedWeight += c.getWeight();
            entries.add(new Entry(c, accumulatedWeight));
        }
        int rolls = Utils.getRandom().nextInt(maxRolls) + 1;
        for (int i = 0; i < rolls; i++){
            double r = Utils.getRandom().nextDouble() * accumulatedWeight;

            for (Entry entry : entries) {
                if (!pickedEnchantments.isEmpty()){
                    // if an enchantment has been picked, skip this entry
                    if (item.getEnchantments().keySet().stream().anyMatch(en -> entry.enchantment.conflictsWithEnchantment(en.getKey().getKey())) ||
                            pickedEnchantments.keySet().stream().anyMatch(en -> entry.enchantment.conflictsWithEnchantment(en.getType()))) continue;
                }
                if (entry.weight >= r) {
                    int designatedLevel;
                    if (entry.enchantment.getMaxLevel() < entry.enchantment.getMaxTableLevel()){
                        EnchantsSquared.getPlugin().getServer().getLogger().warning(
                                "getRandomEnchantments() was called, but max_enchants_table for " +
                                        "the enchant " + entry.enchantment.getDisplayEnchantment() + " is higher than max_enchants. " +
                                        "This cannot be the case, so be sure to correct this mistake");
                    }
                    if (entry.enchantment.getMaxTableLevel() > 0){
                        designatedLevel = Utils.getRandom().nextInt(entry.enchantment.getMaxTableLevel()) + 1;
                    } else {
                        designatedLevel = 1;
                    }
                    if (pickedEnchantments.size() < maxEnchants){
                        pickedEnchantments.put(entry.enchantment, Math.min(designatedLevel, entry.enchantment.getMaxLevel()));
                    }
                    break;
                }
            }
        }
        return pickedEnchantments;
    }

    public void enchantForPlayer(ItemStack item, Player p){
        setItemEnchants(item, getRandomEnchantments(item, p, maxEnchantsFromTable, false, getCompatibleEnchants(item, p.getGameMode())));
    }

    /**
     * Returns a list of all CustomEnchants if they are compatible with the item given.<br>
     * - If the enchantment conflicts with a vanilla or custom enchantment the item has, it will be excluded.<br>
     * - If the enchantment isn't naturally compatible with the item, it will be excluded.<br>
     * - If the enchantment is for books only and the item isn't a book, it will be excluded.<br>
     * Books, enchanted books, and non-damageable items will return ALL enchantments.
     * @param item the item to check compatibilities
     * @return a set of compatible enchantments
     */
    public Collection<CustomEnchant> getCompatibleEnchants(ItemStack item, GameMode combinedIn){
        Collection<CustomEnchant> possibleEnchants = new HashSet<>();
        if (ItemUtils.isAirOrNull(item)) return possibleEnchants;
        if (item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK) return allEnchants.values();
        Map<CustomEnchant, Integer> existingCustomEnchantments = getItemsEnchantsFromPDC(item);
        for (CustomEnchant e : allEnchants.values()){
            // checks if the item has any conflicting custom enchantments. This isn't done with hasCustomEnchantment()
            // because that method would repeatedly fetch an item's enchantments for each custom enchantment which
            // is obviously not necessary
            if (item.getEnchantments().keySet().stream().anyMatch(en -> e.conflictsWithEnchantment(en.getKey().getKey()))) continue;
            if (existingCustomEnchantments.keySet().stream().anyMatch(en -> e.conflictsWithEnchantment(en.getType()))) continue;

//            if (!(item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0)){
//                possibleEnchants.add(e);
//            } else
            if (combinedIn == GameMode.CREATIVE || (e.isFunctionallyCompatible(item.getType()) && e.isNaturallyCompatible(item.getType()) && !e.isBookOnly())){
                possibleEnchants.add(e);
            }
        }
        return possibleEnchants;
    }

    public void setItemEnchants(ItemStack item, Map<CustomEnchant, Integer> enchantments){
        if (ItemUtils.isAirOrNull(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (enchantments.isEmpty()){
            if (meta.getPersistentDataContainer().has(enchantmentsKey, PersistentDataType.STRING)){
                meta.getPersistentDataContainer().remove(enchantmentsKey);
                item.setItemMeta(meta);
                meta = item.getItemMeta();
            }
        } else {
            if (item.getType() == Material.BOOK) item.setType(Material.ENCHANTED_BOOK);

            //updating PersistentDataContainer to be accurate with enchantments
            Collection<String> stringEnchants = new HashSet<>();
            enchantments.forEach((e, l) -> stringEnchants.add(allEnchants.inverse().get(e) + ":" + l));
            meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(enchantmentsKey, PersistentDataType.STRING, String.join(";", stringEnchants));
        }
        item.setItemMeta(meta);
        updateLore(item);
    }

    public void updateLore(ItemStack i){
        if (ItemUtils.isAirOrNull(i)) return;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        //updating the lore to be accurate with enchantments
        List<String> lore = meta.hasLore() && meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        List<String> finalLore = new ArrayList<>();

        //if the lore contains an enchantment it's not supposed to have, it is removed
        int firstEnchantIndex = -1; // This will track where in the lore the first enchant is located,
        // so that in case there is custom lore before or after the enchantments they will stay there
        for (String l : lore){
            String colorStrippedLine = ChatColor.stripColor(ChatUtils.chat(l));
            if (allEnchants.values().stream().map(c -> ChatColor.stripColor(ChatUtils.chat(c.getDisplayEnchantment()))).anyMatch(colorStrippedLine::contains)){
                // if this line of lore is a custom enchantment, it is not added and the location is recorded
                if (firstEnchantIndex == -1) firstEnchantIndex = lore.indexOf(l);
            } else {
                // if this line of lore is not a custom enchantment, it is kept in the final lore
                finalLore.add(l);
            }
        }
        boolean hideEnchantsFlag = meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);

        if (!hideEnchantsFlag){
            Map<CustomEnchant, Integer> enchantments = getItemsEnchantsFromPDC(i);

            if (firstEnchantIndex >= 0){
                for (CustomEnchant e : enchantments.keySet()){
                    finalLore.add(firstEnchantIndex, ChatUtils.chat(e.getDisplayEnchantment() + (e.getMaxLevel() > 1 ? " " +
                            (isUsingRomanNumerals ?
                                    ChatUtils.toRoman(enchantments.get(e)) :
                                    enchantments.get(e)) : "")));
                }
            } else {
                for (CustomEnchant e : enchantments.keySet()){
                    finalLore.add(ChatUtils.chat(e.getDisplayEnchantment() + (e.getMaxLevel() > 1 ? " " +
                            (isUsingRomanNumerals ?
                                    ChatUtils.toRoman(enchantments.get(e)) :
                                    enchantments.get(e)) : "")));
                }
            }
        }
        meta.setLore(finalLore);
        i.setItemMeta(meta);
    }

    /**
     * Gathers a map containing all custom enchantments the item has
     */
    public Map<CustomEnchant, Integer> getItemsEnchantsFromPDC(ItemStack item){
        Map<CustomEnchant, Integer> totalEnchants = new HashMap<>();
        if (ItemUtils.isAirOrNull(item)) return totalEnchants;
        if (item.getItemMeta() == null) return totalEnchants;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(enchantmentsKey, PersistentDataType.STRING)){
            String enchantmentsString = container.get(enchantmentsKey, PersistentDataType.STRING);
            assert enchantmentsString != null;
            String[] enchantments = enchantmentsString.split(";");
            for (String enchantment : enchantments){
                String[] enchantmentLevelPair = enchantment.split(":");
                if (enchantmentLevelPair.length == 2){
                    try {
                        int id = Integer.parseInt(enchantmentLevelPair[0]);
                        int level = Integer.parseInt(enchantmentLevelPair[1]);
                        if (allEnchants.containsKey(id)) totalEnchants.put(allEnchants.get(id), level);
                    } catch (IllegalArgumentException ignored){
                    }
                }
            }
        }
        return totalEnchants;
    }

    /**
     * @param enchant the type the enchantment is registered under
     * @return the CustomEnchant associated with the given type
     */
    public CustomEnchant getEnchantmentFromType(String enchant){
        for (CustomEnchant e : allEnchants.values()){
            if (e.getType().equals(enchant)){
                return e;
            }
        }
        return null;
    }

    /**
     * Removes a given enchant from the given item
     * @param item the item to remove the enchantment from
     * @param enchant the enchantment type to remove
     * @return true if the enchantment was removed from the item. False if the item is null, if the given enchant type
     * has no enchantment associated with it, or if the item didn't have the enchantment.
     */
    public boolean removeEnchant(ItemStack item, String enchant){
        if (ItemUtils.isAirOrNull(item)) return false;
        Map<CustomEnchant, Integer> itemsEnchants = getItemsEnchantsFromPDC(item);
        CustomEnchant associatedEnchant = getEnchantmentFromType(enchant);
        if (associatedEnchant == null ||
                !allEnchants.containsKey(associatedEnchant.getId()) ||
                !itemsEnchants.containsKey(associatedEnchant)) return false;

        itemsEnchants.remove(associatedEnchant);

        setItemEnchants(item, itemsEnchants);
        return true;
    }

    public ItemStack removeAllEnchants(ItemStack enchantedItem){
        if (ItemUtils.isAirOrNull(enchantedItem)) return null;
        setItemEnchants(enchantedItem, new HashMap<>());
        return enchantedItem;
    }

    /**
     * Adds an enchantment with the given level to the item
     * @param item the item to add an enchantment to
     * @param enchant the enchantment type to add
     * @param level the level the enchantment should have
     */
    public void addEnchant(ItemStack item, String enchant, int level){
        if (ItemUtils.isAirOrNull(item)) return;
        Map<CustomEnchant, Integer> itemsEnchants = getItemsEnchantsFromPDC(item);
        CustomEnchant newEnchant = getEnchantmentFromType(enchant);
        if (newEnchant != null){
            itemsEnchants.put(newEnchant, level);
            setItemEnchants(item, itemsEnchants);
        }
    }

    public int getEnchantStrength(ItemStack item, String enchant){
        if (ItemUtils.isAirOrNull(item)) return 0;
        CustomEnchant e = getEnchantmentFromType(enchant);
        if (e != null){
            return getItemsEnchantsFromPDC(item).getOrDefault(e, 0);
        }
        return 0;
    }

    public List<CustomEnchant> getTradeableEnchants(){
        return allEnchants.values().stream().filter(CustomEnchant::isTradingEnabled).collect(Collectors.toList());
    }

    public void registerEnchant(CustomEnchant enchant){
        if (enchant.isEnabled()) {
            allEnchants.put(enchant.getId(), enchant);
            if (EnchantsSquared.isValhallaHooked()) ValhallaHook.registerEnchantmentModifier(enchant);
        }
    }

    public Collection<CustomEnchant> getEnchantmentsMatchingFilter(Predicate<CustomEnchant> filter){
        return allEnchants.values().stream().filter(filter).distinct()
                .sorted(Comparator.comparingInt(c -> c.getPriority().getPriority())).collect(Collectors.toList());
    }

    public AnvilCombinationResult combineItems(ItemStack item1, ItemStack item2, ItemStack output, GameMode combinedIn){
        // item1 and item2 are both not null

        // item 1 is the same as item 2 and are therefore allowed to combine
        // OR item 2 is an enchanted book and item 1 is able to take enchantments (they are a tool or armor piece or something like that)
        if (item1.getType() == item2.getType() || (item2.getType() == Material.ENCHANTED_BOOK && MaterialClassType.getClass(item1) != null)) {
            Map<CustomEnchant, Integer> item1Enchantments = getItemsEnchantsFromPDC(item1);
            Map<CustomEnchant, Integer> item2Enchantments = getItemsEnchantsFromPDC(item2);
            // second item has no custom enchantments, so no special combination logic needs to occur
            if (item2Enchantments.isEmpty() && item1Enchantments.isEmpty()) return new AnvilCombinationResult(output, AnvilCombinationResult.AnvilCombinationResultState.ITEM_NO_CUSTOM_ENCHANTS);

            Collection<Enchantment> vanillaEnchantmentsToRemove = new HashSet<>();
            item2Enchantments.entrySet().removeIf(e -> item1Enchantments.entrySet().stream().anyMatch(i -> i.getKey().conflictsWithEnchantment(e.getKey().getType())));
            item2.getEnchantments().forEach((key, value) -> {
                if (item1Enchantments.keySet().stream().anyMatch(i -> i.conflictsWithEnchantment(key.getKey().getKey()))) {
                    vanillaEnchantmentsToRemove.add(key);
                }
            });

            // filter out any incompatible enchantments
            Collection<CustomEnchant> compatibleEnchantments = getCompatibleEnchants(item1, combinedIn);
            for (CustomEnchant e : new HashSet<>(item2Enchantments.keySet())){
                if (!compatibleEnchantments.contains(e)) item2Enchantments.remove(e);
            }
            // second item has no custom enchantments that are still compatible with the first item, no special combination logic needs to occur
            if (compatibleEnchantments.isEmpty()) return new AnvilCombinationResult(output, AnvilCombinationResult.AnvilCombinationResultState.ITEM_NO_COMPATIBLE_ENCHANTS);

            Map<CustomEnchant, Integer> resultEnchantments = new HashMap<>(item1Enchantments);
            for (CustomEnchant e : item2Enchantments.keySet()){
                int level = item2Enchantments.get(e);
                // if item 1 has an enchantment that item 2 also has, and they're the same level, increment up to max level
                if (resultEnchantments.getOrDefault(e, 0) == level) resultEnchantments.put(e, Math.min(e.getMaxLevel(), level + 1));
                // if item 1 has an enchantment of lower level than item 2, OR doesn't have it at all, insert the item 2 enchantment
                else if (resultEnchantments.getOrDefault(e, 0) < level) resultEnchantments.put(e, level);
            }

            // Max enchantments count exceeded through transaction, set result to null
            if (resultEnchantments.size() > maxEnchants) return new AnvilCombinationResult(null, AnvilCombinationResult.AnvilCombinationResultState.MAX_ENCHANTS_EXCEEDED);

            ItemStack result;
            // if the result of the anvil isn't empty by vanilla, simply take that output as result
            if (!ItemUtils.isAirOrNull(output)) result = output.clone();
            // if the result of the anvil IS empty, meaning the items could normally not be combined, take the item 1 as result
            else result = item1.clone();

            setItemEnchants(result, resultEnchantments);
            vanillaEnchantmentsToRemove.forEach(result::removeEnchantment);

            // removing repair cost data is needed in certain use cases as without it the anvil will occasionally present an item
            // being combineable even if no changes to the item were made
            if (item1.toString().equals(removeRepairCostData(result.toString()))) return new AnvilCombinationResult(null, AnvilCombinationResult.AnvilCombinationResultState.ITEMS_NOT_COMBINEABLE);
            return new AnvilCombinationResult(result, AnvilCombinationResult.AnvilCombinationResultState.SUCCESSFUL);
        }
        return new AnvilCombinationResult(output, AnvilCombinationResult.AnvilCombinationResultState.ITEMS_NOT_COMBINEABLE);
    }

    private String removeRepairCostData(String itemString){
        if (!itemString.contains("repair-cost=")) return itemString;
        // item string usually formatted as ..., repair-cost=1, ...
        // i hope this is consistent, lol
        String[] parts = itemString.split("repair-cost=");
        String number = parts[1].split(",")[0];
        return itemString.replace("repair-cost=" + number + ", ", "");
    }

    private void registerEnchants(){
        registerEnchant(new Excavation(1, "excavation"));
        registerEnchant(new Sunforged(2, "sunforged"));
        registerEnchant(new Kinship(3, "kinship"));

        registerEnchant(new Flight(4, "flight"));
        registerEnchant(new Rejuvenation(5, "rejuvenation"));
        registerEnchant(new LavaWalker(6, "lava_walker"));
        registerEnchant(new SpeedBoost(7, "speed_boost"));
        registerEnchant(new JumpBoost(8, "jump_boost"));
        registerEnchant(new NightVision(9, "night_vision"));
        registerEnchant(new WaterBreathing(10, "water_breathing"));
        registerEnchant(new Haste(11, "haste"));
        registerEnchant(new Metabolism(12, "metabolism"));
        registerEnchant(new Strength(13, "strength"));
        registerEnchant(new Vigorous(14, "vigorous"));
        registerEnchant(new Luck(15, "lucky"));
        registerEnchant(new CurseBrittle(16, "curse_durability"));
        registerEnchant(new CurseHeavy(17, "curse_heavy"));
        registerEnchant(new CurseHunger(18, "curse_hunger"));

        registerEnchant(new Withering(19, "withering"));
        registerEnchant(new Stunning(20, "stunning"));
        registerEnchant(new Slowness(21, "slowing"));
        registerEnchant(new Nausea(22, "nausea"));
        registerEnchant(new Weakening(23, "weakening"));
        registerEnchant(new Poisoning(24, "poisoning"));
        registerEnchant(new Blinding(25, "blinding"));
        registerEnchant(new Crushing(26, "crushing"));
        registerEnchant(new AOEArrows(27, "arrow_aoe"));
        registerEnchant(new Toxic(28, "toxic"));

        registerEnchant(new Shielding(29, "shielding"));
        registerEnchant(new Steady(30, "knockback_protection"));

        registerEnchant(new Vitality(31, "vitality"));

        registerEnchant(new PlaceTorch(32, "illuminated"));
        registerEnchant(new AutoReplant(33, "auto_replant"));
        registerEnchant(new Shockwave(34, "shockwave"));

        registerEnchant(new Sapping(35, "bonus_exp"));
        registerEnchant(new Vampiric(36, "vampiric"));
        registerEnchant(new Beheading(37, "beheading"));
        registerEnchant(new Soulbound(38, "soulbound"));

        registerEnchant(new SplashPotionBlock(39, "chemical_shield"));
        registerEnchant(new IncreasePotionPotency(40, "potion_potency_buff"));

        registerEnchant(new TradeoffBerserk(41, "tradeoff_berserk"));

        registerEnchant(new ReinforcedPlating(42, "plating"));

        registerEnchant(new TridentSharpness(43, "trident_sharpness"));
        registerEnchant(new Grappling(44, "grappling"));

        registerEnchant(new FireResistance(45, "lava_resistance"));
        registerEnchant(new Perforating(46, "perforating"));
        registerEnchant(new DamageReduction(47, "damage_reduction"));
        registerEnchant(new ShieldDowner(48, "shield_downer"));
        registerEnchant(new Telekinesis(49, "telekinesis"));
        registerEnchant(new Lightning(50, "lightning"));
        registerEnchant(new TreeFeller(51, "tree_feller"));
        registerEnchant(new Pulling(52, "pulling"));
        registerEnchant(new RapidShot(53, "rapid_shot"));
    }



    public boolean doesItemHaveEnchants(ItemStack item){
        if (ItemUtils.isAirOrNull(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(enchantmentsKey, PersistentDataType.STRING);
    }

    public void reload(){
        manager = new CustomEnchantManager();
    }

    public static CustomEnchantManager getInstance(){
        if (manager == null) manager = new CustomEnchantManager();
        return manager;
    }

    public int getMaxEnchants() {
        return maxEnchants;
    }

    public int getMaxEnchantsFromTable() {
        return maxEnchantsFromTable;
    }

    public boolean isRequirePermissions() {
        return requirePermissions;
    }

    public int getLevelMinimum() {
        return levelMinimum;
    }

    private static class Entry{
        double weight;
        CustomEnchant enchantment;
        public Entry(CustomEnchant enchantment, double weight){
            this.weight = weight;
            this.enchantment = enchantment;
        }
    }

    public BiMap<Integer, CustomEnchant> getAllEnchants() {
        return allEnchants;
    }
}
