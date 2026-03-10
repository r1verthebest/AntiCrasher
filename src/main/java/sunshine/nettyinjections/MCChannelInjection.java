package core.sunshine.nettyinjections;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.injector.netty.BootstrapList;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import core.sunshine.handler.ChannelClosedHandler;
import core.sunshine.impl.DosCheck;
import io.netty.channel.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MCChannelInjection {

	private final List<VolatileField> bootstrapFields = new ArrayList<>();
	private List<Object> networkManagers;

	public MCChannelInjection(final DosCheck dosCheck) throws ReflectiveOperationException {
		final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				if (dosCheck.isAttack && channel.remoteAddress() != null) {
					channel.pipeline().addLast(new ChannelClosedHandler());
					return;
				}

				channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
						if (dosCheck.initChannel(ctx)) {
							super.channelRegistered(ctx);
						} else {
							ctx.close();
						}
					}

					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						if (dosCheck.handleObject(ctx, msg)) {
							super.channelRead(ctx, msg);
						} else {
							ctx.close();
						}
					}
				});
			}
		};

		final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				if (msg instanceof Channel) {
					((Channel) msg).pipeline().addFirst(beginInitProtocol);
				}
				super.channelRead(ctx, msg);
			}
		};

		inject(connectionHandler);
	}

	@SuppressWarnings("unchecked")
	private void inject(ChannelHandler connectionHandler) throws ReflectiveOperationException {
		FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
		Object server = fuzzyServer.getSingleton();
		Object serverConnection = null;
		for (Method method : fuzzyServer.getMethodListByParameters(MinecraftReflection.getServerConnectionClass(),
				new Class[0])) {
			try {
				serverConnection = method.invoke(server);
				if (serverConnection != null)
					break;
			} catch (Exception ignored) {
			}
		}

		if (serverConnection == null)
			throw new ReflectiveOperationException("ServerConnection não encontrado.");

		FuzzyReflection fuzzy = FuzzyReflection.fromObject(serverConnection, true);
		try {
			Field field = fuzzy.getParameterizedField(List.class, MinecraftReflection.getNetworkManagerClass());
			field.setAccessible(true);
			this.networkManagers = (List<Object>) field.get(serverConnection);
		} catch (Exception e) {
			ProtocolLogger.debug("Falha ao obter networkManagers via campo, tentando via método.", e);
			Method method = fuzzy.getMethodByParameters("getNetworkManagers", List.class, serverConnection.getClass());
			this.networkManagers = (List<Object>) method.invoke(null, serverConnection);
		}

		if (this.networkManagers == null)
			throw new ReflectiveOperationException("Lista de NetworkManagers nula.");

		for (Field field : fuzzy.getFieldListByType(List.class)) {
			VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();
			List<?> list = (List<?>) volatileField.getValue();

			if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
				this.bootstrapFields.add(volatileField);
				volatileField.setValue(new BootstrapList((List<Object>) list, connectionHandler));
			}
		}
	}

	public synchronized void close() {
		for (VolatileField field : bootstrapFields) {
			try {
				Object value = field.getValue();
				if (value instanceof BootstrapList) {
					((BootstrapList) value).close();
				}
				field.revertValue();
			} catch (Exception e) {
				ProtocolLogger.debug("Erro ao reverter campo de Bootstrap.", e);
			}
		}
		bootstrapFields.clear();
	}
}