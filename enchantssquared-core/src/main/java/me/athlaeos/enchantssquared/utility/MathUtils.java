package me.athlaeos.enchantssquared.utility;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;

public class MathUtils {
    public static Collection<Location> getRandomPointsOnCircleCircumference(Location center, double radius, int amount, boolean includeCenter) {
        World world = center.getWorld();
        Collection<Location> locations = new HashSet<>();
        for(int i = 1; i < amount + 1; i++) {
            double theta = Utils.getRandom().nextDouble() * 2 * Math.PI;
            double x = (center.getX() + (radius * Math.cos(theta)));
            double z = (center.getZ() + (radius * Math.sin(theta)));
            locations.add(new Location(world, x, center.getY(), z));
        }
        if (includeCenter) locations.add(center);
        return locations;
    }

    public static Collection<Location> getEvenCircle(Location center, double radius, int amount, double addAngle){
        World world = center.getWorld();
        Collection<Location> locations = new HashSet<>();
        for (double i = 0; i < amount; ++i) {
            double angle = Math.toRadians(((i / amount) * 360d)) + Math.toRadians((addAngle * 360d));
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;

            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}
