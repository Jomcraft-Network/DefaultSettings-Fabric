package de.pt400c.defaultsettings.mixin;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface ExampleMixin {
	
	@Accessor("defaultKey")
    public void setDefaultKey(InputUtil.Key key);
	
	
	/*
	 * try {
				FileUtil.restoreContents();
				
			} catch (Exception e) {
				DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game:", e);
			}
	 */
}
