package me.athlaeos.enchantssquared.utility;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.domain.Offset;
import me.athlaeos.enchantssquared.domain.Version;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockUtils {
    public static Collection<Location> getBlocksInArea(Location loc1, Location loc2){
        Collection<Location> blocks = new HashSet<>();

        int topBlockX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int bottomBlockX = Math.min(loc1.getBlockX(), loc2.getBlockX());

        int topBlockY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int bottomBlockY = Math.min(loc1.getBlockY(), loc2.getBlockY());

        int topBlockZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int bottomBlockZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        for(int x = bottomBlockX; x <= topBlockX; x++) {
            for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                for(int y = bottomBlockY; y <= topBlockY; y++) {
                    blocks.add(new Location(null, x, y, z));
                }
            }
        }
        return blocks;
    }

    public static int[][] getOffsetsBetweenPoints(int[] offset1, int[] offset2){
        int xOff = Math.abs(offset1[0] - offset2[0]) + 1;
        int yOff = Math.abs(offset1[1] - offset2[1]) + 1;
        int zOff = Math.abs(offset1[2] - offset2[2]) + 1;
        int arraySize = Math.abs(xOff) * Math.abs(yOff) * Math.abs(zOff);
        int[][] offsets = new int[arraySize][3];
        int index = 0;
        for (int x = offset1[0]; x <= offset2[0]; x++){
            for (int y = offset1[1]; y <= offset2[1]; y++){
                for (int z = offset1[2]; z <= offset2[2]; z++){
                    offsets[index] = new int[]{x, y, z};
                    index++;
                }
            }
        }

        return offsets;
    }

    public static void breakBlock(Player player, Block block){
        if (Version.currentVersionOrOlderThan(Version.MINECRAFT_1_16)){
            // try to break a block and call the appropriate events
            ItemStack tool;
            if (!ItemUtils.isAirOrNull(player.getInventory().getItemInMainHand())) {
                tool = player.getInventory().getItemInMainHand();
            } else {
                tool = player.getInventory().getItemInOffHand();
                if (ItemUtils.isAirOrNull(tool)) tool = null;
            }
            BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(breakEvent);
            if (breakEvent.isCancelled()) return;

            List<ItemStack> drops = new ArrayList<>((tool == null) ? block.getDrops() : block.getDrops(tool, player));
            List<Item> items = drops.stream().map(i -> block.getWorld().dropItemNaturally(block.getLocation(), i)).collect(Collectors.toList());
            BlockDropItemEvent event = new BlockDropItemEvent(block, block.getState(), player, new ArrayList<>(items));
            EnchantsSquared.getPlugin().getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()){
                items.forEach(Item::remove);
            } else {
                items.forEach(i -> {
                    if (!event.getItems().contains(i)){
                        // if the event drops do not contain the original item, remove it
                        i.remove();
                    }
                });
            }

            block.getWorld().spawnParticle(Particle.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "BLOCK" : "BLOCK_DUST"), block.getLocation().add(0.5, 0.5, 0.5), 16, 0.5, 0.5, 0.5, block.getBlockData());
            block.setType(Material.AIR);

            ItemUtils.damageItem(player, player.getInventory().getItemInMainHand(), 1, EquipmentSlot.HAND);
        } else {
            player.breakBlock(block);
        }
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
