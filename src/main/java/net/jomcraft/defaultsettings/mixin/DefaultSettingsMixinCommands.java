package net.jomcraft.defaultsettings.mixin;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.command.argument.ArgumentTypeInfos;

@Mixin(ArgumentTypeInfos.class)
public abstract interface DefaultSettingsMixinCommands {

	@Accessor("BY_CLASS")
	public static Map<Class<?>, ArgumentTypeInfo<?, ?>> getClassList() {
		throw new AssertionError();
	}

}
