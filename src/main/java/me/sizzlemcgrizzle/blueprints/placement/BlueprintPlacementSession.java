package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import de.craftlancer.clclans.CLClans;
import de.craftlancer.clclans.Clan;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPrePasteEvent;
import me.sizzlemcgrizzle.blueprints.conversation.ConfirmationPrompt;
import me.sizzlemcgrizzle.blueprints.conversation.FormattedConversable;
import me.sizzlemcgrizzle.blueprints.settings.Logs;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.boss.BossBar;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompSound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlueprintPlacementSession {
    private CLClans clans = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
    private WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    private BlueprintsPlugin blueprintsPlugin = BlueprintsPlugin.getInstance();
    
    private static final String BLOCKS_IN_WAY = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_TRUE_MESSAGE;
    private static final String BLOCKS_IN_WAY_NO_PREVIEW = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.SHOW_ERROR_FALSE_MESSAGE;
    private static final String PLACEMENT_DENIED = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.PLACEMENT_DENIED;
    private static final String PLACEMENT_ACCEPTED = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.BUILD_SUCCESS;
    public static final String IS_CONVERSING = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.IS_CONVERSING;
    public static final String ABOVE_Y_255 = Settings.Messages.MESSAGE_PREFIX + Settings.Messages.ABOVE_Y_255;
    
    private Blueprint blueprint;
    private Player player;
    private Location location;
    private ItemStack item;
    private Block block;
    private World world;
    private ClipboardHolder holder;
    private BossBar bossBar;
    private BukkitTask runnable;
    private BukkitTask clearErrorBlocks;
    private GameMode gameMode;
    private Inventory inventory;
    private String type;
    
    private int counter;
    private int rotationsFromNorth;
    
    private Set<Location> pasteBlockSet = new HashSet<>();
    private Set<Location> errorBlockSet = new HashSet<>();
    private Set<Location> bannerSet = new HashSet<>();
    private List<Location> pasteSet = new ArrayList<>();
    
    private Conversation convo;
    
    public BlueprintPlacementSession(Blueprint blueprint, Player player, Location loc, ItemStack item, Block block, World world, GameMode gameMode, String type, BlockFace originalDirection) {
        this.blueprint = blueprint;
        this.type = type;
        this.item = item;
        this.item.setAmount(1);
        this.player = player;
        this.location = loc;
        this.block = block;
        this.world = world;
        this.inventory = player.getInventory();
        this.holder = blueprint.getHolder(player);
        this.bossBar = blueprintsPlugin.getBossBar();
        this.gameMode = gameMode;
        
        rotationsFromNorth = getRotationsFromNorth(originalDirection);
    }
    
    private int getRotationsFromNorth(BlockFace originalDirection) {
        switch (originalDirection) {
            case EAST:
            case EAST_NORTH_EAST:
            case EAST_SOUTH_EAST:
                return 1;
            case SOUTH:
            case SOUTH_SOUTH_EAST:
            case SOUTH_SOUTH_WEST:
            case SOUTH_EAST:
            case SOUTH_WEST:
                return 2;
            case WEST:
            case WEST_SOUTH_WEST:
            case WEST_NORTH_WEST:
                return 3;
            default:
                return 0;
        }
    }
    
    /**
     * Starts the process. If something goes wrong, it will cancel and return true.
     */
    public void start() {
        
        counter -= rotationsFromNorth;
        if (!(blueprint instanceof PlayerBlueprint))
            holder.setTransform(new AffineTransform().rotateY(counter * 90));
        
        getBlocksInWay();
        
        if (errorBlockSet.size() > 0) {
            if (errorBlockSet.stream().anyMatch(loc -> loc.getY() > 255)) {
                Common.tell(player, ABOVE_Y_255);
            } else {
                if (Settings.Block.SHOW_ERROR_PREVIEW)
                    Common.tell(player, BLOCKS_IN_WAY);
                else
                    Common.tell(player, BLOCKS_IN_WAY_NO_PREVIEW);
                if (Settings.PLAY_SOUNDS)
                    player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
                showErrorBlocks();
                
            }
            if (!gameMode.equals(GameMode.CREATIVE))
                player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
            return;
        }
        
        sendFakeBlocks();
        
        if (Settings.PLAY_SOUNDS)
            player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);
        
        spawnBossBar();
        beginConversation();
    }
    
    /**
     * Gets all the blocks in the way at the current paste location.
     */
    private void getBlocksInWay() {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
            Operation testOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
                @Override
                public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
                    Location pasteLocation = new Location(world, location.getX(), location.getY(), location.getZ());
                    if ((!pasteLocation.getBlock().getType().isAir() && !Settings.Block.IGNORE_BLOCKS.contains(pasteLocation.getBlock().getType()))
                            || BlueprintsPlugin.isInRegion(player, pasteLocation)
                            || !BlueprintsPlugin.isTrusted(player, pasteLocation)
                            || pasteLocation.getY() > 255)
                        errorBlockSet.add(pasteLocation);
                    return true;
                }
            })
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .copyBiomes(false)
                    .copyEntities(false)
                    .ignoreAirBlocks(true)
                    .build();
            
            Operations.complete(testOperation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends fake blocks from the given paste location.
     */
    private void sendFakeBlocks() {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
            Operation previewOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
                @Override
                public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
                    Location loc = new Location(world, location.getX(), location.getY(), location.getZ());
                    pasteBlockSet.add(loc);
                    bannerSet.add(loc);
                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                    if (!MaterialUtil.isBanner(loc.getBlock().getType()))
                        wePlayer.sendFakeBlock(location, block);
                    return true;
                }
            })
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .copyBiomes(false)
                    .copyEntities(false)
                    .ignoreAirBlocks(true)
                    .build();
            Operations.complete(previewOperation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the origin from a conversation input (+z, +y, etc.)
     */
    public boolean setOrigin(int x, int y, int z) {
        if (clearErrorBlocks != null) {
            clearErrorBlocks.cancel();
            clearErrorBlocks();
        }
        location.setX(location.getX() + x);
        location.setY(location.getY() + y);
        location.setZ(location.getZ() + z);
        clearFakeBlocks();
        getBlocksInWay();
        if (errorBlockSet.size() > 0) {
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eTranslation cancelled. The blueprint cannot be moved here!");
            showErrorBlocks();
            location.setX(location.getX() - x);
            location.setY(location.getY() - y);
            location.setZ(location.getZ() - z);
        }
        sendFakeBlocks();
        return false;
    }
    
    /**
     * Sets a rotation from conversation input.
     */
    public void transform(int times) {
        
        if (clearErrorBlocks != null) {
            clearErrorBlocks.cancel();
            clearErrorBlocks();
        }
        
        counter += times;
        holder.setTransform(new AffineTransform().rotateY(counter * 90));
        
        clearFakeBlocks();
        getBlocksInWay();
        
        if (errorBlockSet.size() > 0) {
            Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&ePlacement cancelled. The blueprint cannot be moved here!");
            showErrorBlocks();
            counter -= times;
            holder.setTransform(new AffineTransform().rotateY(counter * 90));
        }
        
        sendFakeBlocks();
    }
    
    
    /**
     * Gets the clipboard.
     */
    private Clipboard getClipboardFromSchematic() {
        Clipboard clipboard = null;
        File file = new File(worldEditPlugin.getDataFolder().getAbsolutePath() + File.separator + "/schematics" + File.separator + "/" + blueprint.getSchematic());
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
            
            if (clans != null && clans.isEnabled())
                if (clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())) != null) {
                    ChatColor color = clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId())).getColor();
                    for (BlockVector3 blockVector3 : clipboard.getRegion()) {
                        if (clipboard.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_WOOL))
                            clipboard.setBlock(blockVector3, MaterialUtil.getWoolColor(color));
                        if (clipboard.getBlock(blockVector3).getBlockType().equals(BlockTypes.WHITE_CONCRETE))
                            clipboard.setBlock(blockVector3, MaterialUtil.getConcreteColor(color));
                    }
                }
            
            
        } catch (IOException | WorldEditException e) {
            Common.log(player.getName() + " tried to place a blueprint using " + blueprint.getSchematic() + ", however this schematic isn't valid!");
        }
        return clipboard;
    }
    
    /**
     * Completes the operation.
     */
    public void complete() {
        //If there are still fake blocks, clear them.
        pasteBlockSet.forEach(loc -> player.sendBlockChange(loc, loc.getBlock().getBlockData()));
        
        if (blueprint instanceof PlayerBlueprint) {
            Optional<InventoryLink> optional = BlueprintsPlugin.getInstance().getLink(player);
            
            if (!optional.isPresent()) {
                if (!gameMode.equals(GameMode.CREATIVE))
                    player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
                Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&cYou must link barrels or shulker boxes to draw materials from! &4/playerblueprint link create");
                if (Settings.PLAY_SOUNDS)
                    player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
                return;
            }
            
            if (!((PlayerBlueprint) blueprint).getMaterialMap().entrySet().stream().allMatch(entry -> optional.get().contains(entry.getKey(), entry.getValue()))) {
                if (!gameMode.equals(GameMode.CREATIVE))
                    player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
                Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&cThe barrels/shulker boxes you have linked do not contain the materials needed!");
                if (Settings.PLAY_SOUNDS)
                    player.playSound(player.getLocation(), CompSound.ANVIL_LAND.getSound(), 1F, 0.5F);
                return;
            }
            
            ((PlayerBlueprint) blueprint).getMaterialMap().forEach((k, v) -> optional.get().take(k, v));
        }
        //If cancelled, the placement will be cancelled.
        BlueprintPrePasteEvent event = new BlueprintPrePasteEvent(type, player, blueprint.getSchematic(), gameMode, item, location, blueprint instanceof PlayerBlueprint);
        Common.callEvent(event);
        
        if (event.isCancelled()) {
            if (!gameMode.equals(GameMode.CREATIVE))
                player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
            Logs.addToLogs(player, location, blueprint.getSchematic(), "cancelled by event");
        } else {
            
            pasteAndGetPasteBlocks();
            
            Logs.addToLogs(player, location, blueprint.getSchematic(), "confirmed");
            
            if (Settings.PLAY_SOUNDS)
                player.playSound(player.getLocation(), CompSound.ANVIL_USE.getSound(), 1F, 0.7F);
            
            if (clans != null && clans.isEnabled())
                new AdjustBannerRunnable(player, clans, bannerSet).runTaskLater(SimplePlugin.getInstance(), 5);
            
            Common.tell(player, PLACEMENT_ACCEPTED);
            Common.callEvent(new BlueprintPostPasteEvent(type, player, blueprint.getSchematic(), gameMode, item, location, pasteSet, blueprint instanceof PlayerBlueprint));
        }
    }
    
    /**
     * Clears ALL fake blocks that have been sent by this blueprint.
     */
    private void clearFakeBlocks() {
        pasteBlockSet.forEach(loc -> player.sendBlockChange(loc, loc.getBlock().getBlockData()));
        pasteBlockSet.clear();
    }
    
    /**
     * Clears all the red concrete/error blocks sent by this blueprint.
     */
    private void clearErrorBlocks() {
        errorBlockSet.forEach(loc -> player.sendBlockChange(loc, loc.getBlock().getBlockData()));
        errorBlockSet.clear();
    }
    
    /**
     * Show all error blocks as the set error block.
     */
    private void showErrorBlocks() {
        
        this.clearErrorBlocks = new BukkitRunnable() {
            @Override
            public void run() {
                clearErrorBlocks();
            }
        }.runTaskLater(blueprintsPlugin, Settings.Block.BLOCK_TIMEOUT * 20);
        
        if (Settings.Block.SHOW_ERROR_PREVIEW)
            errorBlockSet.forEach(loc -> player.sendBlockChange(loc, Settings.Block.ERROR_BLOCK.createBlockData()));
    }
    
    private void spawnBossBar() {
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        
        DecimalFormat format = new DecimalFormat("##.00");
        this.runnable = new BukkitRunnable() {
            double duration = (double) Settings.Block.BOSSBAR_DURATION;
            double secondsLeft = duration;
            
            @Override
            public void run() {
                secondsLeft = Math.round(secondsLeft * 100.0) / 100.0;
                if (secondsLeft == 0.0) {
                    convo.abandon();
                    cancel();
                }
                if (secondsLeft % 2.5 == 0 && secondsLeft != duration && Settings.PLAY_SOUNDS)
                    player.playSound(player.getLocation(), CompSound.NOTE_BASS.getSound(), 2.0F, 0.8F);
                bossBar.setTitle(ChatColor.GREEN + "Confirm Placement: " + ChatColor.RED + format.format(secondsLeft) + ChatColor.GREEN + " seconds left");
                bossBar.setProgress(secondsLeft / duration);
                secondsLeft -= 0.05;
            }
        }.runTaskTimer(blueprintsPlugin, 0, 1);
    }
    
    private void beginConversation() {
        ConversationFactory conversation = new ConversationFactory(blueprintsPlugin)
                .withLocalEcho(false)
                .withModality(false)
                .withTimeout(Settings.Block.BOSSBAR_DURATION)
                .withFirstPrompt(new ConfirmationPrompt(this))
                .addConversationAbandonedListener(conversationAbandonedEvent -> {
                    if (!conversationAbandonedEvent.gracefulExit()) {
                        
                        Common.tell(player, Settings.Messages.MESSAGE_PREFIX + "&eYour placement has been cancelled.");
                        Logs.addToLogs(player, block.getLocation(), blueprint.getSchematic(), "abandoned");
                        
                        if (inventory.firstEmpty() == -1)
                            world.dropItemNaturally(player.getLocation(), item);
                        else if (!gameMode.equals(GameMode.CREATIVE))
                            player.getInventory().addItem(item);
                        
                        if (Settings.PLAY_SOUNDS)
                            player.playSound(player.getLocation(), CompSound.LEVEL_UP.getSound(), 1F, 1F);
                    }
                    makeBossBarInvisible();
                    clearErrorBlocks();
                    clearFakeBlocks();
                    runnable.cancel();
                });
        convo = conversation.buildConversation(new FormattedConversable(player));
        convo.begin();
    }
    
    private void makeBossBarInvisible() {
        bossBar.removePlayer(player);
    }
    
    public void cancel() {
        if (Settings.PLAY_SOUNDS)
            player.playSound(player.getLocation(), CompSound.LEVEL_UP.getSound(), 1F, 1F);
        Common.tell(player, PLACEMENT_DENIED);
        makeBossBarInvisible();
        if (!gameMode.equals(GameMode.CREATIVE))
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
        
    }
    
    private static class AdjustBannerRunnable extends BukkitRunnable {
        private Set<Location> bannerSet;
        private CLClans clans;
        private Player player;
        
        
        AdjustBannerRunnable(Player player, CLClans clans, Set<Location> bannerSet) {
            this.player = player;
            this.clans = clans;
            this.bannerSet = bannerSet;
        }
        
        @Override
        public void run() {
            for (Location bannerLocation : bannerSet) {
                Clan clan = clans.getClan(Bukkit.getOfflinePlayer(player.getUniqueId()));
                if (clan == null || clan.getBanner() == null)
                    break;
                if (!MaterialUtil.isBanner(bannerLocation.getBlock().getType())) {
                    continue;
                }
                //If the bannerLocation is a wall banner, set the type to the wall banner variant of the clan banner
                if (bannerLocation.getBlock().getType().toString().contains("_WALL_BANNER")) {
                    Directional directional = (Directional) bannerLocation.getBlock().getBlockData();
                    BlockFace face = directional.getFacing();
                    
                    bannerLocation.getBlock().setType(Material.getMaterial(clan.getBanner().getType().toString().replace("_BANNER", "_WALL_BANNER")));
                    //To apply direction
                    
                    //Set face to what it was before.
                    Directional newFace = (Directional) bannerLocation.getBlock().getBlockData();
                    newFace.setFacing(face);
                    bannerLocation.getBlock().setBlockData(newFace);
                } else {
                    Rotatable rotatable = (Rotatable) bannerLocation.getBlock().getBlockData();
                    BlockFace face = rotatable.getRotation();
                    
                    bannerLocation.getBlock().setType(clan.getBanner().getType());
                    
                    Rotatable newRotatable = (Rotatable) bannerLocation.getBlock().getBlockData();
                    newRotatable.setRotation(face);
                    bannerLocation.getBlock().setBlockData(newRotatable);
                }
                
                BannerMeta clanBanner = (BannerMeta) clan.getBanner().getItemMeta();
                Banner banner = (Banner) bannerLocation.getBlock().getState();
                
                if (clanBanner != null) {
                    banner.setPatterns(clanBanner.getPatterns());
                    banner.update();
                }
            }
        }
    }
    
    private void pasteAndGetPasteBlocks() {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
            Operation previewOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
                @Override
                public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) {
                    pasteSet.add(new Location(world, location.getX(), location.getY(), location.getZ()));
                    return true;
                }
            })
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .copyBiomes(false)
                    .copyEntities(false)
                    .ignoreAirBlocks(true)
                    .build();
            
            if (blueprint instanceof PlayerBlueprint)
                editSession.getSurvivalExtent().setStripNbt(true);
            
            Operations.complete(previewOperation);
            Operation operation = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .ignoreAirBlocks(true)
                    .copyEntities(false)
                    .copyBiomes(false)
                    .build();
            
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Blueprint getBlueprint() {
        return blueprint;
    }
}
