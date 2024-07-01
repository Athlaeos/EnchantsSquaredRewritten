package me.athlaeos.enchantssquared.animations;

import org.bukkit.Location;

public abstract class Animation {
    protected String argument;
    public Animation(String argument){
        this.argument = argument;
    }

    public abstract Animation getNew(String argument);

    public abstract void play(Location l);
}
