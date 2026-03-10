package core.sunshine.nettyinjections;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.entity.Player;
import core.sunshine.anticrash.utils.NMSUtils;
import core.sunshine.handler.ByteBufDecoderHandler;

import java.util.NoSuchElementException;

public class NettyDecodeInjection {

	private final Player player;
	private Channel channel;
	private static final String HANDLER_NAME = "ac-decoder";

	public NettyDecodeInjection(Player player) {
		this.player = player;
		try {
			this.channel = NMSUtils.getChannel(player);
			if (this.channel != null) {
				this.inject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void inject() {
		channel.eventLoop().execute(() -> {
			try {
				ChannelPipeline pipeline = channel.pipeline();
				if (pipeline.get(HANDLER_NAME) != null)
					return;

				ByteBufDecoderHandler handler = new ByteBufDecoderHandler(this.player);
				if (pipeline.get("decompress") != null) {
					pipeline.addAfter("decompress", HANDLER_NAME, handler);
				} else if (pipeline.get("splitter") != null) {
					pipeline.addAfter("splitter", HANDLER_NAME, handler);
				} else {
					pipeline.addFirst(HANDLER_NAME, handler);
				}
			} catch (NoSuchElementException | IllegalArgumentException ignored) {
			}
		});
	}

	public void unInject() {
		if (channel == null || !channel.isOpen())
			return;

		channel.eventLoop().execute(() -> {
			try {
				if (channel.pipeline().get(HANDLER_NAME) != null) {
					channel.pipeline().remove(HANDLER_NAME);
				}
			} catch (NoSuchElementException ignored) {
			}
		});
	}
}