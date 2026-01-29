package fr.maxlego08.shop.api.event;

import fr.maxlego08.shop.api.buttons.ItemButton;
import org.bukkit.inventory.ItemStack;

public class ShopAction {

    private final ItemStack itemStack;
    private final ItemButton itemButton;
    private double price;
    private int totalAmount;

    public ShopAction(ItemStack itemStack, ItemButton itemButton, double price) {
        this.itemStack = itemStack;
        this.itemButton = itemButton;
        this.price = price;
        this.totalAmount = itemStack.getAmount();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemButton getItemButton() {
        return itemButton;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void addAmount(int amount) {
        this.totalAmount += amount;
    }
}
