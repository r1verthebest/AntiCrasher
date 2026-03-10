package core.sunshine.anticrash.utils;

import core.sunshine.Core;
import org.bukkit.Bukkit;

public final class TPSCalculator {

	private final int[] tpsHistory = new int[60];
	private int historyIndex = 0;

	private long lastTickTime = System.currentTimeMillis();
	private int tickCount = 0;
	private int currentTps = 20;

	public TPSCalculator() {
		Bukkit.getScheduler().runTaskTimer(Core.getPlugin(), () -> {
			long now = System.currentTimeMillis();

			if (now - lastTickTime >= 1000L) {
				currentTps = tickCount;
				tpsHistory[historyIndex] = currentTps;
				historyIndex = (historyIndex + 1) % tpsHistory.length;

				tickCount = 0;
				lastTickTime = now;
			}

			tickCount++;
		}, 0L, 1L);
	}

	public int getCurrentTps() {
		return Math.min(currentTps, 20);
	}

	public double getAverageTps(int seconds) {
		int duration = Math.min(seconds, tpsHistory.length);
		int sum = 0;
		int count = 0;

		for (int i = 0; i < duration; i++) {
			int index = (historyIndex - 1 - i + tpsHistory.length) % tpsHistory.length;
			int val = tpsHistory[index];

			if (val > 0) {
				sum += val;
				count++;
			}
		}

		return count == 0 ? 20.0 : (double) sum / count;
	}
}