package me.sizzlemcgrizzle.blueprints;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryLink {
    
    private Player owner;
    private List<Inventory> inventories = new ArrayList<>();
    
    public InventoryLink(Player owner) {
        
        this.owner = owner;
    }
    
    public void take(Material material, int amount) {
        for (Inventory inventory : inventories) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() == material) {
                    int a = item.getAmount();
                    
                    if (a > amount) {
                        item.setAmount(a - amount);
                        return;
                    } else {
                        item.setAmount(0);
                        amount -= a;
                    }
                }
            }
        }
    }
    
    public boolean contains(Material material, int amount) {
        for (Inventory inventory : inventories) {
            for (ItemStack item : inventory.getContents()) {
                if (item == null)
                    continue;
                if (item.getType() == material)
                    amount -= item.getAmount();
                if (amount <= 0)
                    return true;
            }
        }
        return false;
    }
    
    /**
     * @param inventory the inventory being added
     * @return true if the inventory isn't present and is added
     */
    public boolean add(Inventory inventory) {
        if (inventories.stream().noneMatch(i -> getChestLocations(i.getHolder()).contains(inventory.getLocation()))) {
            inventories.add(inventory);
            return true;
        } else
            return false;
    }
    
    /**
     * @param inventory the inventory being removed
     * @return true if the inventory is present and removed
     */
    public boolean remove(Inventory inventory) {
        if (inventories.stream().anyMatch(i -> getChestLocations(i.getHolder()).contains(inventory.getLocation()))) {
            inventories.remove(getInventory(inventory));
            return true;
        } else
            return false;
    }
    
    public List<Inventory> getInventories() {
        return inventories;
    }
    
    public Player getOwner() {
        return owner;
    }
    
    private Inventory getInventory(Inventory inventory) {
        return inventories.stream().filter(inventory1 -> getChestLocations(inventory1.getHolder()).contains(inventory.getLocation())).findFirst().get();
    }
    
    private List<Location> getChestLocations(InventoryHolder holder) {
        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = ((DoubleChest) holder);
            return Arrays.asList(doubleChest.getLeftSide().getInventory().getLocation(), doubleChest.getRightSide().getInventory().getLocation());
        } else
            return Collections.singletonList(holder.getInventory().getLocation());
    }
}
