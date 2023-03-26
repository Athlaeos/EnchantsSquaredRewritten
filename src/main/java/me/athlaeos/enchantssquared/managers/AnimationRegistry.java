package me.athlaeos.enchantssquared.managers;

import me.athlaeos.enchantssquared.animations.Animation;
import me.athlaeos.enchantssquared.animations.Flash;
import me.athlaeos.enchantssquared.animations.Splash;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
    private static final Map<String, Animation> baseAnimations = new HashMap<>();
    private static final Map<String, Animation> animations = new HashMap<>();

    public static Animation get(String type){
        // animation is already registered with the plugin
        if (animations.containsKey(type)) return animations.get(type);
        // animation is not yet registered, so its construction is attempted
        String[] parts = type.split("-");
        String baseType = parts[0];
        // if the base animation (animation without argument) does not exist, throw an exception
        if (!baseAnimations.containsKey(baseType)) {
            throw new IllegalArgumentException("Base animation type " + baseType + " does not exist");
        }
        // register the animation with the argument, or no argument if none are present
        animations.put(type, baseAnimations.get(baseType).getNew(parts.length > 1 ? parts[1] : null));
        return animations.get(type);
    }

    public static void registerDefaults(){
        baseAnimations.put("splash", new Splash("#ffffff"));
        baseAnimations.put("flash", new Flash("1.0"));
    }
}
