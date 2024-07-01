package me.athlaeos;

import me.athlaeos.enchantssquared.version.NMS;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class NMS_default implements NMS {
    @Override
    public void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null){
            instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier)).forEach(instance::removeModifier);
            if (amount != 0) instance.addModifier(new AttributeModifier(uuid, identifier, amount, operation));
        }
    }

    @Override
    public boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        AttributeInstance instance = e.getAttribute(type);
        return instance != null && instance.getModifiers().stream().anyMatch(m -> m != null && m.getName().equals(identifier));
    }

    @Override
    public double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type) {
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) return instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier) && m.getUniqueId().equals(uuid)).map(AttributeModifier::getAmount).findFirst().orElse(0D);
        return 0;
    }

    @Override
    public void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type) {
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) instance.getModifiers().stream().filter(m -> m != null && m.getName().equals(identifier)).forEach(instance::removeModifier);
    }
}
