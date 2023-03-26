package me.athlaeos.enchantssquared.menus;

import org.bukkit.entity.Player;

public class PlayerMenuUtility {
    private Player owner;
    private Player selectedPlayer;
    private int pageNumber = 0;
    private Menu previousMenu = null;

    public PlayerMenuUtility(Player owner) {
        this.owner = owner;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public void setSelectedPlayer(Player selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    public void setPreviousMenu(Menu previousMenu) {
        this.previousMenu = previousMenu;
    }

    public Menu getPreviousMenu() {
        return previousMenu;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void incrementPageNumber(){
        pageNumber++;
    }

    public void decrementPageNumber(){
        pageNumber--;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
