package core.sunshine.handler;

import core.sunshine.Core;
import core.sunshine.anticrash.utils.NMSUtils;
import core.sunshine.config.ConfigCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class ByteBufDecoderHandler extends ByteToMessageDecoder {

	private final Player player;
	private long lastWarningSent = 0L;

	public ByteBufDecoderHandler(Player player) {
		this.player = player;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
		if (byteBuf.capacity() < 0 || byteBuf.refCnt() < 1) {
			sendCrashWarning("Estrutura de pacote inválida (Capacidade/RefCnt)");
			return;
		}

		int maxLength = ConfigCache.getInstance().getValue("decode.maxLength", 8000, Integer.class);
		if (byteBuf.readableBytes() > maxLength) {
			sendCrashWarning("Pacote excede o limite de bytes permitidos");
			return;
		}

		if (!player.isOnline()) {
			ctx.close();
			return;
		}

		int maxCapacity = ConfigCache.getInstance().getValue("decode.maxCapacity", 16500, Integer.class);

		if (byteBuf.readableBytes() > 0) {
			ByteBuf slice = byteBuf.slice();
			PacketDataSerializer serializer = new PacketDataSerializer(slice);

			try {
				int packetId = serializer.e();
				EnumProtocol protocol = ctx.channel().attr(NetworkManager.c).get();
				Packet<?> packet = protocol.a(EnumProtocolDirection.SERVERBOUND, packetId);

				if (packet != null) {
					String customPath = "decode.customCapacity." + packet.getClass().getSimpleName();
					maxCapacity = ConfigCache.getInstance().getValue(customPath, maxCapacity, Integer.class);
				}
			} catch (Exception ignored) {
			}
		}

		if (byteBuf.capacity() > maxCapacity) {
			sendCrashWarning("Capacidade do buffer excede o limite de segurança (" + maxCapacity + ")");
			return;
		}
		list.add(byteBuf.readBytes(byteBuf.readableBytes()));
	}

	private void sendCrashWarning(String reason) {
		long now = System.currentTimeMillis();
		if (lastWarningSent + 1500L > now)
			return;
		lastWarningSent = now;

		try {
			NMSUtils.getChannel(player).close();
		} catch (Exception ignored) {
		}

		Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
			if (player.isOnline()) {
				player.kickPlayer("§c§lSUNSHINE SHIELD\n\n§fErro crítico de decodificação de rede.");
			}

			String msg = "§c§lSHIELD §fSuspeito: §c" + player.getName() + " §f- Motivo: §7" + reason;
			Bukkit.getConsoleSender().sendMessage(msg);
			Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("anticrash.notify"))
					.forEach(p -> p.sendMessage(msg));
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		if (!(cause instanceof java.io.IOException)) {
			Core.getPlugin().getLogger()
					.warning("Erro no pipeline do jogador " + player.getName() + ": " + cause.getMessage());
		}
		ctx.close();
	}
}