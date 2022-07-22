package net.jomcraft.defaultsettings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.mojang.blaze3d.platform.InputUtil;
import net.minecraft.client.option.KeyBind;

@Mixin(KeyBind.class)
public interface DefaultSettingsMixin {

	@Accessor("defaultKey")
	@Mutable
	public void setDefaultKey(InputUtil.Key key);

}
