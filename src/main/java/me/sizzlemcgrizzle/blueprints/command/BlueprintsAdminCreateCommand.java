package me.sizzlemcgrizzle.blueprints.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.placement.Blueprint;
import me.sizzlemcgrizzle.blueprints.placement.PlayerBlueprint;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class BlueprintsAdminCreateCommand extends SubCommand {
    
    private BlueprintsPlugin plugin;
    
    public BlueprintsAdminCreateCommand(BlueprintsPlugin plugin) {
        super("blueprints.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Utils.getMatches(args[2], SchematicUtil.getSchematics());
        if (args.length == 4)
            return Collections.singletonList("<type>");
        if (args.length == 5)
            return Collections.singletonList("<canRotate>");
        if (args.length == 6)
            return Collections.singletonList("<canTranslate>");
        if (args.length == 7)
            return Collections.singletonList("<canItemFrameRotate45Degrees>");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Player player = (Player) sender;
        
        final ItemStack blueprint = player.getInventory().getItemInMainHand();
        
        if (!blueprint.hasItemMeta() || !blueprint.getItemMeta().hasDisplayName()) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "You must hold an item with a display name.");
            return null;
        }
        
        if (plugin.getBlueprints().stream().filter(b -> !(b instanceof PlayerBlueprint)).anyMatch(b -> b.getSchematic().equals(args[2]))) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "A blueprint with this schematic already exists.");
            return null;
        }
        
        if (!SchematicUtil.getSchematics().contains(args[2])) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "There is no such file '" + args[2]
                    + "'. Please add the schematic to WorldEdit's schematics folder.");
            return null;
        }
        
        
        ItemMeta meta = blueprint.getItemMeta();
        meta.getPersistentDataContainer().set(Blueprint.BLUEPRINT_KEY, PersistentDataType.STRING, args[2]);
        blueprint.setItemMeta(meta);
        
        Blueprint b = new Blueprint(blueprint.clone(), args[2], args[3], Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
        
        plugin.addBlueprint(b);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added blueprint.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
