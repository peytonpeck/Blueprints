package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.menu.ConditionalPagedMenu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import de.craftlancer.core.util.Tuple;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintsAdminListCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    private ConditionalPagedMenu menu;
    
    public BlueprintsAdminListCommand(BlueprintsPlugin plugin) {
        super("blueprints.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return super.onTabComplete(sender, args);
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        display((Player) sender);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
    
    private void display(Player player) {
        if (menu == null)
            createMenu();
        
        menu.setPageItems(getPageItems());
        
        menu.display(player, ResourcePackManager.getInstance().isFullyAccepted(player) ? "resource" : "default");
    }
    
    private void createMenu() {
        this.menu = new ConditionalPagedMenu(plugin, 6, getPageItems(), true, true,
                Arrays.asList(new Tuple<>("default", "Blueprints List"),
                        new Tuple<>("resource", "§f" + TranslateSpaceFont.TRANSLATE_NEGATIVE_8 + "\uE300" + TranslateSpaceFont.getSpecificAmount(-165) + "§8Blueprints List")));
        
        menu.setInventoryUpdateHandler(conditionalMenu -> conditionalMenu.getMenu("default")
                .fillBorders(new MenuItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("").build()), false));
    }
    
    private List<MenuItem> getPageItems() {
        return plugin.getBlueprints().stream().filter(b -> !(b instanceof PlayerBlueprint)).map(blueprint -> new MenuItem(new ItemBuilder(blueprint.getItem().clone()).addLore("",
                "§6→ Left Click §eto receive.", "§6→ Shift Right Click §eto remove.").build())
                .addClickAction(click -> {
                    click.getPlayer().getInventory().addItem(blueprint.getItem());
                    MessageUtil.sendMessage(plugin, click.getPlayer(), MessageLevel.SUCCESS, "Blueprint has been added to your inventory.");
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    plugin.getBlueprints().remove(blueprint);
                    menu.setPageItems(getPageItems());
                    MessageUtil.sendMessage(plugin, click.getPlayer(), MessageLevel.SUCCESS, "Blueprint removed.");
                    display(click.getPlayer());
                }, ClickType.SHIFT_RIGHT)).collect(Collectors.toList());
    }
}
