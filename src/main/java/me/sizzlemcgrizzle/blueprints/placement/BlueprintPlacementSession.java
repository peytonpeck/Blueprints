package me.sizzlemcgrizzle.blueprints.placement;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import de.craftlancer.clclans.CLClans;
import de.craftlancer.clclans.Clan;
import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.Utils;
import de.craftlancer.core.conversation.FormattedConversable;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.sizzlemcgrizzle.blueprints.BlueprintsPlugin;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPrePasteEvent;
import me.sizzlemcgrizzle.blueprints.settings.Logs;
import me.sizzlemcgrizzle.blueprints.settings.Settings;
import me.sizzlemcgrizzle.blueprints.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlueprintPlacementSession implements Listener {
    private CLClans clans = (CLClans) Bukkit.getPluginManager().getPlugin("CLClans");
    
    private BlueprintsPlugin plugin;
    private Blueprint blueprint;
    private Player player;
    private Location location;
    private ItemStack item;
    private Block block;
    private World world;
    private ClipboardHolder holder;
    private BossBar bossBar;
    private BukkitTask runnable;
    private GameMode gameMode;
    private Inventory inventory;
    private String type;
    private Rotation rotation = Rotation.NONE;
    
    private int counter;
    private int rotationsFromNorth;
    
    private Set<Location> pasteBlockSet = new HashSet<>();
    private Set<Location> errorBlockSet = new HashSet<>();
    private Set<Entity> pasteEntitySet = new HashSet<>();
    private Set<Location> bannerSet = new HashSet<>();
    
    private List<Location> pastedBlocks = new ArrayList<>();
    private List<Entity> pastedEntities = new ArrayList<>();
    
    private Conversation convo;
    
    public BlueprintPlacementSession(BlueprintsPlugin plugin, Blueprint blueprint, Player player, Location loc, ItemStack item, Block block, World world, GameMode gameMode, String type, BlockFace originalDirection) {
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
        this.bossBar = Bukkit.getServer().createBossBar(ChatColor.GREEN + "Confirm Placement Timer", BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
        this.gameMode = gameMode;
        this.plugin = plugin;
        
        this.rotation = blueprint.canItemFrameRotate45Degrees() ?
                Utils.getRotationFromYaw(player.getLocation().getYaw())
                : Utils.getRotationFromBlockFace(player.getFacing());
        
        rotationsFromNorth = getRotationsFromNorth(originalDirection);
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (pasteEntitySet.contains(event.getRightClicked())) {
            event.setCancelled(true);
            event.getRightClicked().remove();
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(HangingBreakEvent event) {
        if (pasteEntitySet.contains(event.getEntity())) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
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
    
    public void start() {
        
        if (!(blueprint instanceof PlayerBlueprint) && blueprint.canRotate90Degrees()) {
            counter -= rotationsFromNorth;
            holder.setTransform(new AffineTransform().rotateY(counter * 90));
        }
        
        getBlocksInWay();
        
        if (errorBlockSet.size() > 0) {
            if (errorBlockSet.stream().anyMatch(loc -> loc.getY() > 255)) {
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.ABOVE_Y_255);
            } else {
                if (Settings.Block.SHOW_ERROR_PREVIEW)
                    MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.SHOW_ERROR_TRUE_MESSAGE);
                else
                    MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.SHOW_ERROR_FALSE_MESSAGE);
                playSound(Sound.BLOCK_ANVIL_LAND, 0.5F);
                showErrorBlocks();
                
            }
            if (!gameMode.equals(GameMode.CREATIVE))
                player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
            return;
        }
        
        sendFakeBlocks();
        
        playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 0.8F);
        spawnBossBar();
        beginConversation();
    }
    
    /**
     * Gets all the blocks in the way at the current paste location.
     */
    private void getBlocksInWay() {
        operate((location, block) -> {
            Location pasteLocation = new Location(world, location.getX(), location.getY(), location.getZ());
            if ((!pasteLocation.getBlock().getType().isAir() && !Settings.Block.IGNORE_BLOCKS.contains(pasteLocation.getBlock().getType()))
                    || Utils.isInAdminRegion(pasteLocation)
                    || !Utils.isTrusted(player.getUniqueId(), pasteLocation, ClaimPermission.Build)
                    || pasteLocation.getY() > 255)
                errorBlockSet.add(pasteLocation);
            return true;
        }, (e) -> true, e -> {
        }, false);
    }
    
    /**
     * Sends fake blocks from the given paste location.
     */
    private void sendFakeBlocks() {
        
        operate((location, block) -> {
            Location loc = new Location(world, location.getX(), location.getY(), location.getZ());
            pasteBlockSet.add(loc);
            bannerSet.add(loc);
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            if (!MaterialUtil.isBanner(loc.getBlock().getType()))
                wePlayer.sendFakeBlock(location, block);
            return true;
        }, (e -> {
            int[] array = e.getState().getNbtData().getIntArray("UUID");
            
            UUID uuid = new UUID((long) array[0] << 32 | array[1] & 0xFFFFFFFFL, (long) array[2] << 32 | array[3] & 0xFFFFFFFFL);
            
            new LambdaRunnable(() -> {
                Entity bukkitEntity = Bukkit.getEntity(uuid);
                
                if (bukkitEntity != null) {
                    pasteEntitySet.add(bukkitEntity);
                    bukkitEntity.setGravity(false);
                    bukkitEntity.setInvulnerable(true);
                    
                    if (bukkitEntity instanceof ItemFrame) {
                        if (blueprint.canItemFrameRotate45Degrees())
                            ((ItemFrame) bukkitEntity).setRotation(rotation);
                        ((ItemFrame) bukkitEntity).setItem(item.clone());
                    }
                }
            }).runTaskLater(plugin, 1);
            
            return true;
        }), (e) -> {
        }, true);
    }
    
    /**
     * Sets the origin from a conversation input (+z, +y, etc.)
     */
    public boolean setOrigin(int x, int y, int z) {
        location = location.add(x, y, z);
        clearFakeBlocks();
        getBlocksInWay();
        if (errorBlockSet.size() > 0) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.MESSAGE_PREFIX + "&eTranslation cancelled. The blueprint cannot be moved here!");
            showErrorBlocks();
            location = location.subtract(x, y, z);
        }
        sendFakeBlocks();
        return false;
    }
    
    /**
     * Sets a rotation from conversation input.
     */
    public void transform(int times) {
        
        if (!blueprint.canRotate90Degrees()) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.MESSAGE_PREFIX + "&eThis type of blueprint cannot be rotated.");
            return;
        }
        
        counter += times;
        holder.setTransform(new AffineTransform().rotateY(counter * 90));
        
        clearFakeBlocks();
        getBlocksInWay();
        
        if (errorBlockSet.size() > 0) {
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.MESSAGE_PREFIX + "&ePlacement cancelled. The blueprint cannot be moved here!");
            showErrorBlocks();
            counter -= times;
            holder.setTransform(new AffineTransform().rotateY(counter * 90));
        }
        
        sendFakeBlocks();
    }
    
    /**
     * Completes the operation.
     */
    public void complete() {
        //If there are still fake blocks, clear them.
        pasteBlockSet.forEach(loc -> player.sendBlockChange(loc, loc.getBlock().getBlockData()));
        
        if (blueprint instanceof PlayerBlueprint) {
            Optional<InventoryLink> optional = plugin.getLink(player);
            
            if (!optional.isPresent()) {
                if (!gameMode.equals(GameMode.CREATIVE))
                    player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.MESSAGE_PREFIX + "&cYou must link barrels or shulker boxes to draw materials from! &4/playerblueprint link create");
                playSound(Sound.BLOCK_ANVIL_LAND, 0.5F);
                return;
            }
            
            if (!((PlayerBlueprint) blueprint).getMaterialContainer().getMaterialMap().entrySet().stream().allMatch(entry -> optional.get().contains(entry.getKey(), entry.getValue()))) {
                if (!gameMode.equals(GameMode.CREATIVE))
                    player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.MESSAGE_PREFIX + "&cThe barrels/shulker boxes you have linked do not contain the materials needed!");
                playSound(Sound.BLOCK_ANVIL_LAND, 0.5F);
                return;
            }
            
            ((PlayerBlueprint) blueprint).getMaterialContainer().getMaterialMap().forEach((k, v) -> optional.get().take(k, v));
        }
        //If cancelled, the placement will be cancelled.
        BlueprintPrePasteEvent event = new BlueprintPrePasteEvent(type, player, blueprint.getSchematic(), gameMode, item, location, blueprint instanceof PlayerBlueprint);
        Bukkit.getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            if (!gameMode.equals(GameMode.CREATIVE))
                player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
            Logs.addToLogs(plugin, player, location, blueprint.getSchematic(), "cancelled by event");
        } else
            paste();
        
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Clears ALL fake blocks that have been sent by this blueprint.
     */
    private void clearFakeBlocks() {
        pasteBlockSet.forEach(loc -> player.sendBlockChange(loc, loc.getBlock().getBlockData()));
        pasteBlockSet.clear();
        
        pasteEntitySet.forEach(Entity::remove);
        pasteEntitySet.clear();
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
        
        new LambdaRunnable(this::clearErrorBlocks).runTaskLater(plugin, Settings.Block.BLOCK_TIMEOUT * 20);
        
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
                if (secondsLeft % 2.5 == 0 && secondsLeft != duration)
                    playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 0.8F);
                bossBar.setTitle(ChatColor.GREEN + "Confirm Placement: " + ChatColor.RED + format.format(secondsLeft) + ChatColor.GREEN + " seconds left");
                bossBar.setProgress(secondsLeft / duration);
                secondsLeft -= 0.05;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    private void beginConversation() {
        ConversationFactory conversation = new ConversationFactory(plugin)
                .withLocalEcho(false)
                .withModality(false)
                .withTimeout(Settings.Block.BOSSBAR_DURATION)
                .withFirstPrompt(new BlueprintMovePrompt(ChatColor.YELLOW + "Place blueprint?", this, blueprint.canRotate90Degrees(), blueprint.canTranslate()))
                .addConversationAbandonedListener(conversationAbandonedEvent -> {
                    if (!conversationAbandonedEvent.gracefulExit()) {
                        
                        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Your placement has been cancelled.");
                        Logs.addToLogs(plugin, player, block.getLocation(), blueprint.getSchematic(), "abandoned");
                        
                        if (inventory.firstEmpty() == -1)
                            world.dropItemNaturally(player.getLocation(), item);
                        else if (!gameMode.equals(GameMode.CREATIVE))
                            player.getInventory().addItem(item);
                        
                        playSound(Sound.ENTITY_PLAYER_LEVELUP, 1F);
                    }
                    bossBar.removePlayer(player);
                    clearErrorBlocks();
                    clearFakeBlocks();
                    runnable.cancel();
                });
        convo = conversation.buildConversation(new FormattedConversable(player));
        convo.begin();
    }
    
    public void cancel() {
        playSound(Sound.ENTITY_PLAYER_LEVELUP, 1F);
        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.PLACEMENT_DENIED);
        bossBar.removePlayer(player);
        if (!gameMode.equals(GameMode.CREATIVE))
            player.getInventory().addItem(item).forEach((a, b) -> player.getWorld().dropItem(player.getLocation(), b));
        
    }
    
    private void playSound(Sound sound, float pitch) {
        if (Settings.PLAY_SOUNDS)
            player.playSound(player.getLocation(), sound, 0.5F, pitch);
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
    
    private EditSession operate(BiPredicate<BlockVector3, BlockStateHolder> blockAction, Predicate<com.sk89q.worldedit.entity.Entity> entityAction,
                                Consumer<EditSession> editSessionConsumer, boolean copyEntities) {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
            Operation previewOperation = holder.createPaste(new AbstractDelegateExtent(editSession) {
                @Override
                public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
                    return blockAction.test(location, block) || super.setBlock(location, block);
                }
                
                @Nullable
                @Override
                public com.sk89q.worldedit.entity.Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
                    com.sk89q.worldedit.entity.Entity e = super.createEntity(location, entity);
                    return entityAction.test(e) ? e : null;
                }
            })
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    .copyBiomes(false)
                    .copyEntities(copyEntities)
                    .ignoreAirBlocks(true)
                    .build();
            
            editSessionConsumer.accept(editSession);
            
            Operations.complete(previewOperation);
            
            return editSession;
        } catch (WorldEditException e) {
            e.printStackTrace();
            
            return null;
        }
    }
    
    private void paste() {
        
        EditSession session = operate((location, block) -> {
                    pastedBlocks.add(new Location(world, location.getX(), location.getY(), location.getZ()));
                    return false;
                },
                e -> {
                    int[] array = e.getState().getNbtData().getIntArray("UUID");
                    
                    UUID uuid = new UUID((long) array[0] << 32 | array[1] & 0xFFFFFFFFL, (long) array[2] << 32 | array[3] & 0xFFFFFFFFL);
                    
                    new LambdaRunnable(() -> {
                        Entity bukkitEntity = Bukkit.getEntity(uuid);
                        
                        if (bukkitEntity != null)
                            if (bukkitEntity instanceof ItemFrame) {
                                if (blueprint.canItemFrameRotate45Degrees())
                                    ((ItemFrame) bukkitEntity).setRotation(rotation);
                                ((ItemFrame) bukkitEntity).setItem(item.clone());
                            }
                        pastedEntities.add(bukkitEntity);
                    }).runTaskLater(plugin, 1);
                    
                    return true;
                }, editSession -> editSession.getSurvivalExtent().setStripNbt(true),
                true);
        
        Operation operation = holder
                .createPaste(session)
                .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                .ignoreAirBlocks(true)
                .copyEntities(false)
                .copyBiomes(false)
                .build();
        
        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        
        Logs.addToLogs(plugin, player, location, blueprint.getSchematic(), "confirmed");
        
        playSound(Sound.BLOCK_ANVIL_USE, 0.7F);
        
        if (clans != null && clans.isEnabled())
            new AdjustBannerRunnable(player, clans, bannerSet).runTaskLater(plugin, 5);
        
        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, Settings.Messages.BUILD_SUCCESS);
        new LambdaRunnable(() -> Bukkit.getPluginManager().callEvent(new BlueprintPostPasteEvent(type, player, blueprint.getSchematic(), gameMode,
                item, location, pastedBlocks, blueprint instanceof PlayerBlueprint, pastedEntities))).
                runTaskLater(plugin, 2);
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
