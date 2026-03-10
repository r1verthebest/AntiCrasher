package core.sunshine.nettyinjections;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class PacketInjection extends MessageToMessageDecoder<Packet<?>> {
   private final Player player;
   private ArrayList<PacketInjection.PacketListener> packetListeners = Lists.newArrayList();

   public PacketInjection(Player player) {
      this.player = player;
   }

   public void addPacketListener(PacketInjection.PacketListener listeners) {
      this.packetListeners.add(listeners);
   }

   protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
      boolean handle = true;

      try {
         Iterator var5 = this.packetListeners.iterator();

         while(var5.hasNext()) {
            PacketInjection.PacketListener packetListener = (PacketInjection.PacketListener)var5.next();
            if (!packetListener.handlePacket(this.player, packet)) {
               handle = false;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      if (handle) {
         list.add(packet);
      }

   }

   public void inject() {
      CraftPlayer player = (CraftPlayer)this.player;
      player.getHandle().playerConnection.networkManager.channel.pipeline().addAfter("decoder", "ac_packet_decoder", this);
   }

   public void remove() {
      CraftPlayer player = (CraftPlayer)this.player;
      Channel channel = player.getHandle().playerConnection.networkManager.channel;
      if (channel.pipeline().get("ac_packet_decoder") != null) {
         channel.pipeline().remove("ac_packet_decoder");
      }

   }

   public interface PacketListener {
      boolean handlePacket(Player var1, Packet<?> var2);
   }
}
