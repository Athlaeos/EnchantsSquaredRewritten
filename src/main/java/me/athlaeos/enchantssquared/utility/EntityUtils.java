package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.regular_interval.TriggerOnRegularIntervalsEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.RegularIntervalEnchantmentClockManager;
import me.athlaeos.valhallatrinkets.TrinketsManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class EntityUtils {

    private static boolean include(UUID uuid){
        RegularIntervalEnchantmentClockManager.includePlayerIntoClock(uuid);
        return true;
    }

    public static EntityEquipment getEntityEquipment(LivingEntity e, boolean getEnchantments){
        EntityEquipment equipment = new EntityEquipment(e);
        if (e == null) return equipment;
        if (e.getEquipment() != null) {
            boolean included = false;
            equipment.setHelmet(e.getEquipment().getHelmet());
            equipment.setChestplate(e.getEquipment().getChestplate());
            equipment.setLeggings(e.getEquipment().getLeggings());
            equipment.setBoots(e.getEquipment().getBoots());
            equipment.setMainHand(e.getEquipment().getItemInMainHand());
            equipment.setOffHand(e.getEquipment().getItemInOffHand());
            if (getEnchantments){
                if (!ItemUtils.isAirOrNull(equipment.getHelmet())) equipment.setHelmetEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getHelmet()));
                if (equipment.getHelmetEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (!ItemUtils.isAirOrNull(equipment.getChestplate())) equipment.setChestplateEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getChestplate()));
                if (!included && equipment.getChestplateEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (!ItemUtils.isAirOrNull(equipment.getLeggings())) equipment.setLeggingsEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getLeggings()));
                if (!included && equipment.getLeggingsEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (!ItemUtils.isAirOrNull(equipment.getBoots())) equipment.setBootsEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getBoots()));
                if (!included && equipment.getBootsEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (
                        !ItemUtils.isAirOrNull(equipment.getMainHand()) &&
                        !MaterialClassType.isArmor(equipment.getMainHand()) &&
                        MaterialClassType.getClass(equipment.getMainHand()) != MaterialClassType.TRINKETS
                ){
                    equipment.setMainHandEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getMainHand()));
                    if (!included && equipment.getMainHandEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                }
                if (
                        !ItemUtils.isAirOrNull(equipment.getOffHand()) &&
                        !MaterialClassType.isArmor(equipment.getOffHand()) &&
                        MaterialClassType.getClass(equipment.getOffHand()) != MaterialClassType.TRINKETS
                ){
                    equipment.setOffHandEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getOffHand()));
                    if (!included && equipment.getOffHandEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                }
            }
            if (EnchantsSquared.isTrinketsHooked()){
                if (e instanceof Player){
                    Map<Integer, ItemStack> trinkets = TrinketsManager.getInstance().getTrinketInventory((Player) e);
                    equipment.getMiscEquipment().addAll(trinkets.values());
                    if (getEnchantments){
                        for (ItemStack i : trinkets.values()){
                            equipment.getMiscEquipmentEnchantments().put(i, CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(i));
                            if (!included && equipment.getMiscEquipmentEnchantments().get(i).keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                        }
                    }
                }
            }
            if (!included) {
                RegularIntervalEnchantmentClockManager.excludePlayerFromClock(e.getUniqueId());
                EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () ->
                        CustomEnchantManager.getInstance().getAllEnchants().values().stream()
                                .filter(ce -> ce instanceof TriggerOnRegularIntervalsEnchantment)
                                .forEach(ce -> ((TriggerOnRegularIntervalsEnchantment) ce).onRemove(e)), 5L);
            } else RegularIntervalEnchantmentClockManager.includePlayerIntoClock(e.getUniqueId());
        }
        return equipment;
    }

    public static EntityEquipment updateEnchantments(EntityEquipment equipment, LivingEntity e, boolean getEnchantments, boolean getEquipment, boolean getHands){
        if (e.getEquipment() != null) {
            boolean included = false;
            if (getEquipment){
                equipment.setHelmet(e.getEquipment().getHelmet());
                equipment.setChestplate(e.getEquipment().getChestplate());
                equipment.setLeggings(e.getEquipment().getLeggings());
                equipment.setBoots(e.getEquipment().getBoots());
                if (getEnchantments && equipment.getHelmet() != null) equipment.setHelmetEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getHelmet()));
                if (equipment.getHelmetEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (getEnchantments && equipment.getChestplate() != null) equipment.setChestplateEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getChestplate()));
                if (equipment.getChestplateEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (getEnchantments && equipment.getLeggings() != null) equipment.setLeggingsEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getLeggings()));
                if (equipment.getLeggingsEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (getEnchantments && equipment.getBoots() != null) equipment.setBootsEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getBoots()));
                if (equipment.getBootsEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                if (EnchantsSquared.isTrinketsHooked()){
                    if (e instanceof Player){
                        Map<Integer, ItemStack> trinkets = TrinketsManager.getInstance().getTrinketInventory((Player) e);
                        equipment.getMiscEquipment().addAll(trinkets.values());
                        for (ItemStack i : trinkets.values()){
                            if (getEnchantments) equipment.getMiscEquipmentEnchantments().put(i, CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(i));
                            if (!included && equipment.getMiscEquipmentEnchantments().get(i).keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                        }
                    }
                }
            }
            if (getHands){
                equipment.setMainHand(e.getEquipment().getItemInMainHand());
                equipment.setOffHand(e.getEquipment().getItemInOffHand());
                if (getEnchantments && equipment.getMainHand() != null &&
                        !MaterialClassType.isArmor(equipment.getMainHand()) &&
                        MaterialClassType.getClass(equipment.getMainHand()) != MaterialClassType.TRINKETS
                ) {
                    equipment.setMainHandEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getMainHand()));
                    if (!included && equipment.getMainHandEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                }

                if (getEnchantments && equipment.getOffHand() != null &&
                        !MaterialClassType.isArmor(equipment.getOffHand()) &&
                        MaterialClassType.getClass(equipment.getOffHand()) != MaterialClassType.TRINKETS
                ) {
                    equipment.setOffHandEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(equipment.getOffHand()));
                    if (!included && equipment.getOffHandEnchantments().keySet().stream().anyMatch(en -> en instanceof TriggerOnRegularIntervalsEnchantment)) included = include(e.getUniqueId());
                }
            }
            if (!included) {
                RegularIntervalEnchantmentClockManager.excludePlayerFromClock(e.getUniqueId());
                EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () ->
                        CustomEnchantManager.getInstance().getAllEnchants().values().stream()
                                .filter(ce -> ce instanceof TriggerOnRegularIntervalsEnchantment)
                                .forEach(ce -> ((TriggerOnRegularIntervalsEnchantment) ce).onRemove(e)), 5L);
            }
        }

        return equipment;
    }

    public static EntityEquipment getEntityEquipment(LivingEntity e, boolean getEnchantments, boolean getEquipment, boolean getHands){
        EntityEquipment equipment = new EntityEquipment(e);
        if (e == null) return equipment;
        return updateEnchantments(equipment, e, getEnchantments, getEquipment, getHands);
    }

    public static SlotEquipment getFirstEquipmentItemStackWithEnchantment(EntityEquipment equipment, CustomEnchant customEnchant){
        if (equipment.getHelmetEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getHelmet(), EquipmentSlot.HEAD);
        if (equipment.getChestplateEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getChestplate(), EquipmentSlot.CHEST);
        if (equipment.getLeggingsEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getLeggings(), EquipmentSlot.LEGS);
        if (equipment.getBootsEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getBoots(), EquipmentSlot.FEET);
        if (equipment.getMainHandEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getMainHand(), EquipmentSlot.HAND);
        if (equipment.getOffHandEnchantments().containsKey(customEnchant)) return new SlotEquipment(equipment.getOffHand(), EquipmentSlot.OFF_HAND);
        return new SlotEquipment(equipment.getMiscEquipmentEnchantments().entrySet().stream().filter(e -> e.getValue()
                .containsKey(customEnchant)).map(Map.Entry::getKey).findFirst().orElse(null), null);
    }

    public static class SlotEquipment{
        private final ItemStack equipment;
        private final EquipmentSlot slot;
        public SlotEquipment(ItemStack equipment, EquipmentSlot slot){
            this.equipment = equipment;
            this.slot = slot;
        }

        public ItemStack getEquipment() {
            return equipment;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public static LivingEntity getRealAttacker(Entity e){
        if (e instanceof Projectile){
            if (((Projectile) e).getShooter() instanceof LivingEntity) return (LivingEntity) ((Projectile) e).getShooter();
        }
        if (e instanceof LivingEntity) return (LivingEntity) e;
        return null;
    }


    public static void addUniqueAttribute(LivingEntity e, String identifier, Attribute type, double amount, AttributeModifier.Operation operation){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null){
            for (AttributeModifier modifier : instance.getModifiers()){
                if (modifier.getName().equals(identifier)){
                    instance.removeModifier(modifier);
                    break;
                }
            }
            instance.addModifier(
                    new AttributeModifier(identifier, amount, operation)
            );
        }
    }

    public static void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null){
            for (AttributeModifier modifier : instance.getModifiers()){
                if (modifier.getName().equals(identifier)){
                    instance.removeModifier(modifier);
                    break;
                }
            }
        }
    }

    public static void applyPotionEffectIfStronger(LivingEntity entity, PotionEffect effect){
        PotionEffect existingEffect = entity.getPotionEffect(effect.getType());
        if (existingEffect != null){
            if (existingEffect.getAmplifier() <= effect.getAmplifier()){
                entity.addPotionEffect(effect);
            }
        } else {
            entity.addPotionEffect(effect);
        }
    }

    private static String errorPlayerNotFound = null;
    private static String errorMalformedTargeter = null;

    public static Collection<Player> selectPlayers(CommandSender source, String selector){
        if (errorPlayerNotFound == null) errorPlayerNotFound = ConfigManager.getInstance().getConfig("translations.yml").get().getString("error_player_not_found", "");
        if (errorMalformedTargeter == null) errorMalformedTargeter = ConfigManager.getInstance().getConfig("translations.yml").get().getString("error_invalid_targeter", "");

        Collection<Player> targets = new HashSet<>();
        if (selector.startsWith("@")){
            try {
                for (Entity part : Bukkit.selectEntities(source, selector)){
                    if (part instanceof Player){
                        targets.add((Player) part);
                    }
                }
            } catch (IllegalArgumentException e){
                source.sendMessage(ChatUtils.chat(errorMalformedTargeter.replace("%error%", e.getMessage())));
                return targets;
            }
        } else {
            Player target = EnchantsSquared.getPlugin().getServer().getPlayer(selector);
            if (target == null){
                source.sendMessage(ChatUtils.chat(errorPlayerNotFound));
                return targets;
            }
            targets.add(target);
        }
        return targets;
    }
}
