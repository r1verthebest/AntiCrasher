package core.sunshine.events;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PacketDecodeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final ByteBuf byteBuf;
	private boolean cancelled;

	public PacketDecodeEvent(boolean isAsync, Player player, ByteBuf byteBuf) {
		super(isAsync);
		this.player = player;
		this.byteBuf = byteBuf;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Player getPlayer() {
		return this.player;
	}

	public ByteBuf getByteBuf() {
		return this.byteBuf;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}