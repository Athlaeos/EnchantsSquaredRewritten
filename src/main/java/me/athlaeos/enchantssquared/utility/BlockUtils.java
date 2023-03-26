package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.domain.Offset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.function.Predicate;

public class BlockUtils {
    public static Collection<Location> getBlocksInArea(Location loc1, Location loc2){
        Collection<Location> blocks = new HashSet<>();
        if (loc1.getWorld() == null) return blocks;

        int topBlockX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int bottomBlockX = Math.min(loc1.getBlockX(), loc2.getBlockX());

        int topBlockY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int bottomBlockY = Math.min(loc1.getBlockY(), loc2.getBlockY());

        int topBlockZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int bottomBlockZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        for(int x = bottomBlockX; x <= topBlockX; x++) {
            for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                for(int y = bottomBlockY; y <= topBlockY; y++) {
                    Location l = new Location(loc1.getWorld(), x, y, z);
                    if (!loc1.getWorld().getBlockAt(l).getType().toString().contains("AIR")){
                        blocks.add(l);
                    }
                }
            }
        }
        return blocks;
    }

    public static List<Block> getBlockVein(Location origin, HashSet<Material> filter, int limit, Predicate<Block> predicate, Offset... scanArea){
        HashSet<Block> vein = new HashSet<>();
        List<Block> orderedVein = new ArrayList<>();
        if (limit == 0 || filter.isEmpty() || scanArea.length == 0) return orderedVein;
        vein.add(origin.getBlock());
        orderedVein.add(origin.getBlock());
        HashSet<Block> scanBlocks = new HashSet<>();
        scanBlocks.add(origin.getBlock());

        getSurroundingBlocks(orderedVein, scanBlocks, vein, limit, filter, predicate, scanArea);

        return orderedVein;
    }

    public static List<Block> getBlockVein(Location origin, HashSet<Material> filter, int limit, Offset... scanArea){
        Predicate<Block> p = o -> true;
        return getBlockVein(origin, filter, limit, p, scanArea);
    }

    private static void getSurroundingBlocks(List<Block> orderedVein, HashSet<Block> scanBlocks, HashSet<Block> currentVein, int limit, HashSet<Material> filter, Predicate<Block> predicate, Offset... scanArea){
        HashSet<Block> newScanBlocks = new HashSet<>();
        if (currentVein.size() >= limit) return;

        for (Block b : scanBlocks){
            for (Offset o : scanArea){
                Location offset = b.getLocation().clone().add(o.getOffX(), o.getOffY(), o.getOffZ());
                if (!predicate.test(offset.getBlock())) continue;
                if (filter.contains(offset.getBlock().getType())){
                    if (currentVein.contains(offset.getBlock())) continue;
                    currentVein.add(offset.getBlock());
                    orderedVein.add(offset.getBlock());
                    if (currentVein.size() >= limit) return;
                    newScanBlocks.add(offset.getBlock());
                }
            }
        }

        if (newScanBlocks.isEmpty()) return;
        getSurroundingBlocks(orderedVein, newScanBlocks, currentVein, limit, filter, predicate, scanArea);
    }
}
