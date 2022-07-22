package net.jomcraft.defaultsettings;

import com.mojang.blaze3d.platform.InputUtil;

public class KeyContainer {

	public final InputUtil.Key input;
	public final String modifier;

	public KeyContainer(final InputUtil.Key input, final String modifier) {
		this.input = input;
		this.modifier = modifier;
	}

}
