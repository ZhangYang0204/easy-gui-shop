package pers.zhangyang.easyguishop.meta;

import org.jetbrains.annotations.NotNull;

public class ItemStockMeta {
    private final String playerUuid;
    private  String itemStack;
    private int amount;

    public ItemStockMeta(@NotNull String playerUuid, @NotNull String itemStack, int amount) {
        this.playerUuid = playerUuid;
        this.itemStack = itemStack;
        this.amount = amount;
    }

    public void setItemStack(String itemStack) {
        this.itemStack = itemStack;
    }

    @NotNull
    public String getPlayerUuid() {
        return playerUuid;
    }

    @NotNull
    public String getItemStack() {
        return itemStack;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
