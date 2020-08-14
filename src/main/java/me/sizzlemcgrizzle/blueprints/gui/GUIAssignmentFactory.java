package me.sizzlemcgrizzle.blueprints.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GUIAssignmentFactory {
    
    private List<PlayerBlueprintMenu> playerBlueprintListGUIs = new ArrayList<>();
    
    
    public List<PlayerBlueprintMenu> getPlayerBlueprintListGUIs() {
        return playerBlueprintListGUIs;
    }
    
    public Optional<PlayerBlueprintMenu> getPlayerBlueprintListGUIFor(UUID player) {
        return playerBlueprintListGUIs.stream().filter(gui -> gui.getOwner().equals(player)).findFirst();
    }
    
    public void addPlayerBlueprintListGUI(PlayerBlueprintMenu gui) {
        playerBlueprintListGUIs.add(gui);
    }
    
    public void removePlayerBlueprintListGUI(PlayerBlueprintMenu gui) {
        playerBlueprintListGUIs.remove(gui);
    }
}
