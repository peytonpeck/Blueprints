package me.sizzlemcgrizzle.blueprints.placement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryLink {
    
    private Player owner;
    private List<Inventory> inventories = new ArrayList<>();
    
    public InventoryLink(Player owner) {
        this.owner = owner;
    }
    
    public void take(Material material, int amount) {
        inventories.removeIf(inventory ->
                inventory == null
                        || inventory.getLocation() == null
                        || (!inventory.getLocation().getBlock().getType().name().contains("SHULKER_BOX")
                        && inventory.getLocation().getBlock().getType() != Material.BARREL));
        List<Inventory> copy = inventories;
        copy.add(owner.getInventory());
        for (Inventory inventory : copy) {
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
        inventories.removeIf(inventory ->
                inventory == null
                        || inventory.getLocation() == null
                        || (!inventory.getLocation().getBlock().getType().name().contains("SHULKER_BOX")
                        && inventory.getLocation().getBlock().getType() != Material.BARREL));
        List<Inventory> copy = inventories;
        copy.add(owner.getInventory());
        for (Inventory inventory : copy) {
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
     * @return true if the inventory isn't present and is successfully added
     */
    public boolean add(Inventory inventory) {
        if (inventories.stream().noneMatch(i -> i.getLocation().equals(inventory.getLocation()))) {
            inventories.add(inventory);
            return true;
        } else
            return false;
    }
    
    /**
     * @param inventory the inventory being removed
     * @return true if the inventory is present and successfully removed
     */
    public boolean remove(Inventory inventory) {
        if (inventories.stream().anyMatch(i -> i.getLocation().equals(inventory.getLocation()))) {
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
        return inventories.stream().filter(inventory1 -> inventory.getLocation().equals(inventory1.getLocation())).findFirst().get();
    }
}
