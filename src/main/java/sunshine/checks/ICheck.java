package core.sunshine.checks;

import core.sunshine.anticrash.player.PlayerRegister;

public interface ICheck {

	String getName();

	void registerFACPlayer(PlayerRegister register);
}