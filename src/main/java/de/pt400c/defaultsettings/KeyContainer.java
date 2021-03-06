package de.pt400c.defaultsettings;

import net.minecraft.client.util.InputUtil;

public class KeyContainer {
	
	public final InputUtil.Key input;
	public final String modifier;
	
	public KeyContainer(final InputUtil.Key input, final String modifier) {
		this.input = input;
		this.modifier = modifier;
	}
	
}
