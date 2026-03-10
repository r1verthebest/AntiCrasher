package core.sunshine.packetlogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketLoggerFileWriter implements Runnable {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private final File file;
	private final PacketLogger packetLogger;
	private final BlockingQueue<PacketLoggerPacket> packetQueue = new LinkedBlockingQueue<>();

	private volatile boolean running = false;
	private Thread thread;

	public PacketLoggerFileWriter(PacketLogger packetLogger) {
		this.packetLogger = packetLogger;
		File dir = packetLogger.getDIR();
		String fileName = String.format("%s_%s.txt", packetLogger.getPlayer().getName(),
				DATE_FORMAT.format(new Date()));

		this.file = new File(dir, fileName);
	}

	public void writePacket(PacketLoggerPacket packet) {
		this.packetQueue.offer(packet);
	}

	public synchronized void start() {
		if (running)
			return;
		this.running = true;
		this.thread = new Thread(this, "Sunshine-PacketLogger-" + packetLogger.getPlayer().getName());
		this.thread.start();
	}

	public synchronized void stop() {
		this.running = false;
		if (thread != null)
			thread.interrupt();
	}

	@Override
	public void run() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

			while (running || !packetQueue.isEmpty()) {
				PacketLoggerPacket packet = packetQueue.poll(500, TimeUnit.MILLISECONDS);

				if (packet != null) {
					String packetName = packet.getPacket().getClass().getSimpleName();
					String fields = packet.getFields().toString();

					writer.write("----------------------------------------");
					writer.newLine();
					writer.write("Packet: " + packetName);
					writer.newLine();
					writer.write("Data: " + fields);
					writer.newLine();
					writer.newLine();
					writer.flush();
				}
			}
		} catch (IOException | InterruptedException e) {
			if (!(e instanceof InterruptedException)) {
				e.printStackTrace();
			}
		}
	}

	public File getFile() {
		return file;
	}

	public boolean isLogging() {
		return running;
	}
}