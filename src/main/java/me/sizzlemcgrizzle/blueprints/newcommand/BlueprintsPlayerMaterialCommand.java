package me.sizzlemcgrizzle.blueprints.newcommand;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class BlueprintsPlayerMaterialCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsPlayerMaterialCommand(BlueprintsPlugin plugin) {
        super("", plugin, false);
        
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
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must hold a blueprint!");
            return null;
        }
        
        Optional<PlayerBlueprint> optional = plugin.getPlayerBlueprints().stream().filter(blueprint -> blueprint.compareItem(item)).findFirst();
        
        if (!optional.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.WARNING, "This item is not a player blueprint!");
            return null;
        }
        
        optional.get().getMaterialContainer().display(player);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
