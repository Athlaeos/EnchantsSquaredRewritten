package me.athlaeos.enchantssquared.menus;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantmentOverviewMenu extends Menu{
    private final YamlConfiguration translationConfig = ConfigManager.getInstance().getConfig("translations.yml").get();
    private final String menuTitle = ChatUtils.chat(translationConfig.getString("enchantment_menu.title"));

    private final String nextPageName = translationConfig.getString("enchantment_menu.button_nextpage.name");
    private final List<String> nextPageLore = translationConfig.getStringList("enchantment_menu.button_nextpage.lore");
    private final ItemStack nextPageButton = createItemStack(
            Material.valueOf(translationConfig.getString("enchantment_menu.button_nextpage.icon")),
            nextPageName,
            nextPageLore
    );
    private final String prevPageName = translationConfig.getString("enchantment_menu.button_prevpage.name");
    private final List<String> prevPageLore = translationConfig.getStringList("enchantment_menu.button_prevpage.lore");
    private final ItemStack prevPageButton = createItemStack(
            Material.valueOf(translationConfig.getString("enchantment_menu.button_prevpage.icon")),
            prevPageName,
            prevPageLore
    );

    private int currentPage = 1;

    public EnchantmentOverviewMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return menuTitle;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getCurrentItem() != null){
            if (e.getCurrentItem().equals(prevPageButton)){
                currentPage--;
            } else if (e.getCurrentItem().equals(nextPageButton)){
                currentPage++;
            }
        }
        setMenuItems();
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        List<ItemStack> totalEnchantIcons = new ArrayList<>();
        for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()){
            ItemStack icon = enchant.getIcon().clone();
            totalEnchantIcons.add(icon);
        }

        Map<Integer, ArrayList<ItemStack>> pages = ItemUtils.paginateItemStackList(45, totalEnchantIcons);
        if (currentPage < 1) currentPage = 1;
        if (currentPage > pages.size()) currentPage = pages.size();
        for (ItemStack icon : pages.get(currentPage - 1)){
            inventory.addItem(icon);
        }
        updatePageButtons(prevPageButton, true);
        updatePageButtons(nextPageButton, false);
        inventory.setItem(45, prevPageButton);
        inventory.setItem(53, nextPageButton);
    }

    private ItemStack createItemStack(Material material, String displayname, List<String> lore){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatUtils.chat(displayname));
        if (lore != null){
            List<String> coloredLore = new ArrayList<>();
            for (String l : lore){
                coloredLore.add(ChatUtils.chat(l));
            }
            meta.setLore(coloredLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void updatePageButtons(ItemStack item, boolean prevButton) {
        if (ItemUtils.isAirOrNull(item)) return;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setDisplayName(ChatUtils.chat(prevButton ? prevPageName : nextPageName).replace("%page%", "" + currentPage));
        List<String> newLore = new ArrayList<>();
        for (String l : prevButton ? prevPageLore : nextPageLore){
            newLore.add(ChatUtils.chat(l.replace("%page%", "" + currentPage)));
        }
        itemMeta.setLore(newLore);
        item.setItemMeta(itemMeta);
    }
}
