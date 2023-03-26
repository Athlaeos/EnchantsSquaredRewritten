package me.athlaeos.enchantssquared.animations;

import org.bukkit.Location;
import org.bukkit.Particle;

public class Flash extends Animation{
    private final double size;

    public Flash(String argument) {
        super(argument);
        try {
            size = Double.parseDouble(argument);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("invalid flash size given for animation type " + this.getClass().getSimpleName());
        }
    }

    @Override
    public Animation getNew(String argument) {
        return new Flash(argument);
    }

    @Override
    public void play(Location l) {
        if (l.getWorld() == null) return;
        l.getWorld().spawnParticle(Particle.FLASH, l, 0, size);
    }
}
