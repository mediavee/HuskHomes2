package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpDeleteEvent extends Event implements IWarpDeleteEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @NotNull
    private final Warp warp;

    private boolean cancelled;

    public WarpDeleteEvent(@NotNull Warp warp) {
        this.warp = warp;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    @NotNull
    public Warp getWarp() {
        return warp;
    }
}
