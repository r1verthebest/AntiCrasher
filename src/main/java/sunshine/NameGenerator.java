package core.sunshine;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class NameGenerator {

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	private NameGenerator() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String newName() {
		Random rand = ThreadLocalRandom.current();
		int size = 3 + rand.nextInt(4);

		StringBuilder builder = new StringBuilder(size + 6);

		for (int i = 0; i < size; i++) {
			builder.append(getRandomLetter(rand));
		}

		builder.append(rand.nextInt(999999));

		return builder.toString();
	}

	private static char getRandomLetter(Random rand) {
		return ALPHABET.charAt(rand.nextInt(ALPHABET.length()));
	}
}