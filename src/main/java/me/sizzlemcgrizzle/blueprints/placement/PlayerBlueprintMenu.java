package me.sizzlemcgrizzle.blueprints.placement;

import de.craftlancer.core.menu.ConditionalPagedMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import de.craftlancer.core.util.Tuple;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerBlueprintMenu {
    
    private UUID owner;
    private BlueprintsPlugin plugin;
    private ConditionalPagedMenu menu;
    
    public PlayerBlueprintMenu(BlueprintsPlugin plugin, UUID owner) {
        this.owner = owner;
        this.plugin = plugin;
    }
    
    public void display(Player player) {
        if (menu == null)
            createMenu();
        
        menu.display(player, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    private void createMenu() {
        menu = new ConditionalPagedMenu(plugin, 6, getPageItems(), true, true, Arrays.asList(
                new Tuple<>("default", "Player Blueprint List"),
                new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE300" + TranslateSpaceFont.getSpecificAmount(-165) + "§8Player Blueprint List")
        ));
        
        menu.setInventoryUpdateHandler(c -> c.fillBorders(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), false, "default"));
        menu.setInfoItem(getInfoItem());
    }
    
    public void update() {
        if (menu == null)
            createMenu();
        else
            menu.setPageItems(getPageItems());
    }
    
    private List<MenuItem> getPageItems() {
        return plugin.getPlayerBlueprints().stream().filter(b -> b.getOwner().equals(owner))
                .map(playerBlueprint -> {
                    
                    ItemStack item = new ItemBuilder(playerBlueprint.getItem()).addLore(
                            "",
                            ChatColor.GOLD + "→ Left click" + ChatColor.GRAY + " to " + ChatColor.GREEN + "buy" + ChatColor.GRAY + " item for " + ChatColor.GREEN + "$" + playerBlueprint.getCost() + ChatColor.GRAY + ".",
                            ChatColor.GOLD + "→ Right click" + ChatColor.GRAY + " to " + ChatColor.LIGHT_PURPLE + "open" + ChatColor.GRAY + " material list.",
                            ChatColor.GOLD + "→ Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.").build();
                    
                    return new MenuItem(item)
                            .addClickAction(click -> playerBlueprint.getMaterialContainer().display(click.getPlayer()), ClickType.RIGHT)
                            .addClickAction(click -> {
                                Player p = click.getPlayer();
                                if (!playerBlueprint.charge(p))
                                    return;
                                p.getInventory().addItem(playerBlueprint.getItem());
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5F, 2F);
                            }, ClickType.LEFT)
                            .addClickAction(p -> {
                                plugin.getBlueprints().remove(playerBlueprint);
                                menu.setPageItems(getPageItems());
                                display(p.getPlayer());
                                MessageUtil.sendMessage(plugin, p.getPlayer(), MessageLevel.SUCCESS, "Player blueprint removed.");
                            }, ClickType.SHIFT_LEFT);
                }).collect(Collectors.toList());
        
    }
    
    private MenuItem getInfoItem() {
        return new MenuItem(new ItemBuilder(Material.STONE).setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "What is this?")
                .setLore("",
                        ChatColor.GRAY + "This page shows all the player",
                        ChatColor.GRAY + "blueprints you own. If you don't",
                        ChatColor.GRAY + "have any, you won't see any!",
                        "",
                        ChatColor.GOLD + "→ Left click" + ChatColor.GRAY + " to " + ChatColor.GREEN + "buy" + ChatColor.GRAY + " item.",
                        ChatColor.GOLD + "→ Right click" + ChatColor.GRAY + " to " + ChatColor.LIGHT_PURPLE + "open" + ChatColor.GRAY + " material list.",
                        ChatColor.GOLD + "→ Shift left click" + ChatColor.GRAY + " to " + ChatColor.RED + "remove" + ChatColor.GRAY + " blueprint.")
                .setCustomModelData(5)
                .build());
    }
    
    public UUID getOwner() {
        return owner;
    }
}
