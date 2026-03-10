package core.sunshine.impl;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.anticrash.utils.ExploitCheckUtils;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.checks.CheckResult;
import core.sunshine.config.ConfigCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public final class BookCheck extends AbstractCheck {

	public BookCheck() {
		super("BookCheck", Client.CUSTOM_PAYLOAD, Client.BLOCK_PLACE);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();

		if (packet.getType() == Client.CUSTOM_PAYLOAD) {
			handleCustomPayload(player, event);
		} else if (packet.getType() == Client.BLOCK_PLACE) {
			handleBookOpen(player, event);
		}
	}

	private void handleCustomPayload(Player player, PacketEvent event) {
		PacketContainer packet = event.getPacket();
		String channel = packet.getStrings().readSafely(0);

		if (channel == null
				|| (!channel.startsWith("MC|B") && !channel.equals("MC|BSign") && !channel.equals("MC|BEdit"))) {
			return;
		}

		if (ConfigCache.getInstance().getValue("bookcheck.disableBooks", false, Boolean.class)) {
			event.setCancelled(true);
			return;
		}

		ByteBuf byteBuf = packet.getSpecificModifier(ByteBuf.class).readSafely(0);
		if (byteBuf == null || byteBuf.readableBytes() == 0)
			return;

		Material itemInHand = player.getItemInHand().getType();
		if (itemInHand != Material.BOOK_AND_QUILL && itemInHand != Material.WRITTEN_BOOK) {
			sendCrashWarning(player, event, "Enviou pacote de livro sem possuir o item.");
			return;
		}

		try {
			PacketDataSerializer serializer = new PacketDataSerializer(byteBuf.slice());
			ItemStack nmsStack = serializer.i(); 

			if (nmsStack == null || nmsStack.getTag() == null)
				return;

			CheckResult result = ExploitCheckUtils.isInvalidBookTag(nmsStack.getTag());
			if (result.isFlagged()) {
				sendCrashWarning(player, event, "NBT de livro suspeito: " + result.getReason());
				return;
			}

			if (channel.equals("MC|BSign")) {
				String author = nmsStack.getTag().getString("author");
				if (!author.equals(player.getName())) {
					sendCrashWarning(player, event, "Tentativa de assinar livro como: " + author);
				}
			}

		} catch (Exception e) {
			sendCrashWarning(player, event, "Falha na deserealização NBT: " + e.getClass().getSimpleName());
		}
	}

	private void handleBookOpen(Player player, PacketEvent event) {
		org.bukkit.inventory.ItemStack bukkitStack = event.getPacket().getItemModifier().readSafely(0);
		if (bukkitStack == null || !bukkitStack.getType().name().contains("BOOK"))
			return;

		ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
		if (nmsStack == null || !nmsStack.hasTag())
			return;

		CheckResult result = ExploitCheckUtils.isInvalidBookTag(nmsStack.getTag());
		if (result.isFlagged()) {
			sendCrashWarning(player, event, "Abriu livro com tag inválida.");
		}
	}
}