package io.github.anjoismysign.permissions.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PermissionsAssignEvent extends Event {
    private final Player player;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PermissionsAssignEvent(Player player) {
        super(false);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }
}
