package me.athlaeos.enchantssquared.version;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public interface NMS {
    void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation);
    boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type);
    double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type);
    void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type);
}
