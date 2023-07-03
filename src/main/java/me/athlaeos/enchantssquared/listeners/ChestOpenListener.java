package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestOpenListener implements Listener {
    private final double chestCustomEnchantChance;
    private final int chestCustomEnchantRolls;

    public ChestOpenListener(){
        chestCustomEnchantChance = ConfigManager.getInstance().getConfig("config.yml").get().getDouble("custom_enchant_dungeon_rate");
        chestCustomEnchantRolls = Math.max(1, ConfigManager.getInstance().getConfig("config.yml").get().getInt("custom_enchant_dungeon_rolls"));
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent e){
        if (e.useInteractedBlock() != Event.Result.DENY && e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Block b = e.getClickedBlock();
            if (b != null && b.getType() == Material.CHEST && b.getState() instanceof Chest){
                Chest c = (Chest) b.getState();
                if (c.getLootTable() != null){
                    EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
                        for (ItemStack i : c.getInventory().getStorageContents()){
                            if (ItemUtils.isAirOrNull(i)) continue;
                            if (i.getType() == Material.ENCHANTED_BOOK || MaterialClassType.getClass(i.getType()) != null){
                                if (Utils.getRandom().nextDouble() * 100 <= chestCustomEnchantChance){
                                    CustomEnchantManager.getInstance().setItemEnchants(i,
                                            CustomEnchantManager.getInstance().getRandomEnchantments(i, e.getPlayer(),
                                                    chestCustomEnchantRolls, true, CustomEnchantManager.getInstance().getCompatibleEnchants(i, GameMode.SURVIVAL)));
                                }
                            }
                        }
                    }, 1L);
                }
            }
        }
    }
}
