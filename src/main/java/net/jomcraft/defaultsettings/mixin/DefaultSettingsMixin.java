package net.jomcraft.defaultsettings.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface DefaultSettingsMixin {

	@Accessor("defaultKey")
	@Mutable
	public void setDefaultKey(InputUtil.Key key);

}
