package me.athlaeos.enchantssquared.domain;


import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityEquipment{
    private final LivingEntity owner;
    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;
    private ItemStack mainHand = null;
    private ItemStack offHand = null;
    private Map<CustomEnchant, Integer> helmetEnchantments = new HashMap<>();
    private Map<CustomEnchant, Integer> chestplateEnchantments = new HashMap<>();
    private Map<CustomEnchant, Integer> leggingsEnchantments = new HashMap<>();
    private Map<CustomEnchant, Integer> bootsEnchantments = new HashMap<>();
    private Map<CustomEnchant, Integer> mainHandEnchantments = new HashMap<>();
    private Map<CustomEnchant, Integer> offHandEnchantments = new HashMap<>();
    private final List<ItemStack> miscEquipment = new ArrayList<>();
    private final Map<ItemStack, Map<CustomEnchant, Integer>> miscEquipmentEnchantments = new HashMap<>();

    public EntityEquipment(LivingEntity owner){
        this.owner = owner;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public List<ItemStack> getMiscEquipment() {
        return miscEquipment;
    }

    public Map<ItemStack, Map<CustomEnchant, Integer>> getMiscEquipmentEnchantments() {
        return miscEquipmentEnchantments;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public Map<CustomEnchant, Integer> getHelmetEnchantments() {
        return helmetEnchantments;
    }

    public void setHelmetEnchantments(Map<CustomEnchant, Integer> helmetEnchantments) {
        this.helmetEnchantments = helmetEnchantments;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public Map<CustomEnchant, Integer> getChestplateEnchantments() {
        return chestplateEnchantments;
    }

    public void setChestplateEnchantments(Map<CustomEnchant, Integer> chestplateEnchantments) {
        this.chestplateEnchantments = chestplateEnchantments;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public Map<CustomEnchant, Integer> getBootsEnchantments() {
        return bootsEnchantments;
    }

    public void setBootsEnchantments(Map<CustomEnchant, Integer> bootsEnchantments) {
        this.bootsEnchantments = bootsEnchantments;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public Map<CustomEnchant, Integer> getLeggingsEnchantments() {
        return leggingsEnchantments;
    }

    public void setLeggingsEnchantments(Map<CustomEnchant, Integer> leggingsEnchantments) {
        this.leggingsEnchantments = leggingsEnchantments;
    }

    public ItemStack getMainHand() {
        return mainHand;
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    public Map<CustomEnchant, Integer> getMainHandEnchantments() {
        return mainHandEnchantments;
    }

    public void setMainHandEnchantments(Map<CustomEnchant, Integer> mainHandEnchantments) {
        this.mainHandEnchantments = mainHandEnchantments;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public Map<CustomEnchant, Integer> getOffHandEnchantments() {
        return offHandEnchantments;
    }

    public void setOffHandEnchantments(Map<CustomEnchant, Integer> offHandEnchantments) {
        this.offHandEnchantments = offHandEnchantments;
    }

    public List<ItemStack> getIterable(boolean includeHands){
        List<ItemStack> iterable = new ArrayList<>();
        if (!ItemUtils.isAirOrNull(helmet)) iterable.add(helmet);
        if (!ItemUtils.isAirOrNull(chestplate)) iterable.add(chestplate);
        if (!ItemUtils.isAirOrNull(leggings)) iterable.add(leggings);
        if (!ItemUtils.isAirOrNull(boots)) iterable.add(boots);
        if (!miscEquipment.isEmpty()) iterable.addAll(miscEquipment);
        if (includeHands){
            if (!ItemUtils.isAirOrNull(mainHand)) iterable.add(mainHand);
            if (!ItemUtils.isAirOrNull(offHand)) iterable.add(offHand);
        }
        return iterable;
    }

    public Map<ItemStack, Map<CustomEnchant, Integer>> getIterableWithEnchantments(boolean includeHands){
        Map<ItemStack, Map<CustomEnchant, Integer>> iterable = new HashMap<>();
        if (!ItemUtils.isAirOrNull(helmet)) iterable.put(helmet, helmetEnchantments);
        if (!ItemUtils.isAirOrNull(chestplate)) iterable.put(chestplate, chestplateEnchantments);
        if (!ItemUtils.isAirOrNull(leggings)) iterable.put(leggings, leggingsEnchantments);
        if (!ItemUtils.isAirOrNull(boots)) iterable.put(boots, bootsEnchantments);
        if (!miscEquipment.isEmpty()) iterable.putAll(miscEquipmentEnchantments);
        if (includeHands){
            if (!ItemUtils.isAirOrNull(mainHand)) iterable.put(mainHand, mainHandEnchantments);
            if (!ItemUtils.isAirOrNull(offHand)) iterable.put(offHand, offHandEnchantments);
        }
        return iterable;
    }

    public List<ItemStack> getHands(){
        List<ItemStack> iterable = new ArrayList<>();
        if (!ItemUtils.isAirOrNull(mainHand)) iterable.add(mainHand);
        if (!ItemUtils.isAirOrNull(offHand)) iterable.add(offHand);
        return iterable;
    }
}
