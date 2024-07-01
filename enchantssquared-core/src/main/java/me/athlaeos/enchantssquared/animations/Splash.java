package me.athlaeos.enchantssquared.animations;

import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.Effect;
import org.bukkit.Location;

public class Splash extends Animation{
    private final int color;

    public Splash(String argument) {
        super(argument);
        try {
            color = ChatUtils.hexToDec(argument.replaceAll("#", ""));
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("invalid hex given (" + argument + ") for animation type " + this.getClass().getSimpleName());
        }
    }

    @Override
    public Animation getNew(String argument) {
        return new Splash(argument);
    }

    @Override
    public void play(Location l) {
        if (l.getWorld() == null) return;
        l.getWorld().playEffect(l, Effect.POTION_BREAK, color);
    }
}
