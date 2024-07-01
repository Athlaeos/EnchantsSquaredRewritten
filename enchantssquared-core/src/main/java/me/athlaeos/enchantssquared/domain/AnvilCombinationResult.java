package me.athlaeos.enchantssquared.domain;

import org.bukkit.inventory.ItemStack;

public class AnvilCombinationResult {
    private final ItemStack output;
    private final AnvilCombinationResultState state;

    public AnvilCombinationResult(ItemStack output, AnvilCombinationResultState state){
        this.output = output;
        this.state = state;
    }

    public AnvilCombinationResultState getState() {
        return state;
    }

    public ItemStack getOutput() {
        return output;
    }

    public enum AnvilCombinationResultState {
        MAX_ENCHANTS_EXCEEDED,
        ITEM_NO_CUSTOM_ENCHANTS,
        ITEM_NO_COMPATIBLE_ENCHANTS,
        ITEMS_NOT_COMBINEABLE,
        SUCCESSFUL
    }
}
