package net.jomcraft.defaultsettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

import com.mojang.brigadier.arguments.ArgumentType;
import net.jomcraft.defaultsettings.commands.CommandDefaultSettings;
import net.jomcraft.defaultsettings.commands.ConfigArguments;
import net.jomcraft.defaultsettings.commands.OperationArguments;
import net.jomcraft.defaultsettings.commands.TypeArguments;
import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.command.argument.ArgumentTypeInfos;

public class DefaultSettings implements ModInitializer {

	public static final String MODID = "defaultsettings";
	public static final Logger log = LogManager.getLogger(DefaultSettings.MODID);
	public static final String VERSION = "3.5.1";
	public static Map<String, KeyContainer> keyRebinds = new HashMap<String, KeyContainer>();
	public static DefaultSettings instance;

	public static synchronized <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> I registerByClass(Class<A> infoClass, I argumentTypeInfo) {
		ArgumentTypeInfos.BY_CLASS.put(infoClass, argumentTypeInfo);
		return argumentTypeInfo;
	}

	@Override
	public void onInitialize(ModContainer mod) {
		instance = this;

		registerByClass(ConfigArguments.class, new ConfigArguments.Serializer());
		registerByClass(OperationArguments.class, new OperationArguments.Serializer());
		registerByClass(TypeArguments.class, new TypeArguments.Serializer());

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (!environment.dedicated) {
				CommandDefaultSettings.register(dispatcher);
			}
		});
		
		ClientLifecycleEvents.READY.register((test) -> {
			try {

				FileUtil.restoreKeys(true, FileUtil.firstBootUp);
			} catch (IOException e) {
				DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
			} catch (NullPointerException e) {
				DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
			}
		}

		);

	}

	public static DefaultSettings getInstance() {
		return instance;
	}
}