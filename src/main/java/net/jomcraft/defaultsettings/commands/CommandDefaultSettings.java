package net.jomcraft.defaultsettings.commands;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.jomcraft.defaultsettings.DefaultSettings;
import net.jomcraft.defaultsettings.FileUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CommandDefaultSettings {

	private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "Please wait until the last request has finished"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = CommandManager.literal("defaultsettings");

		literalargumentbuilder.then(CommandManager.literal("save").executes((command) -> {
			return saveProcess(command.getSource(), null, null);
		}).then(CommandManager.argument("operation", OperationArguments.operationArguments(false)).executes((command) -> {
			return saveProcess(command.getSource(), OperationArguments.getString(command, "operation"), null);
		}).then(CommandManager.argument("type", TypeArguments.typeArguments()).executes((command) -> {
			return saveProcess(command.getSource(), OperationArguments.getString(command, "operation"), TypeArguments.getString(command, "type"));
		})))).then(CommandManager.literal("saveconfigs").executes((command) -> {
			return saveProcessConfigs(command.getSource(), null, null);
		}).then(CommandManager.argument("operation", OperationArguments.operationArguments(true)).executes((command) -> {
			return saveProcessConfigs(command.getSource(), OperationArguments.getString(command, "operation"), null);
		}).then(CommandManager.argument("config", ConfigArguments.configArguments()).executes((command) -> {
			return saveProcessConfigs(command.getSource(), OperationArguments.getString(command, "operation"), ConfigArguments.getString(command, "config"));
		}))));

		LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literalargumentbuilder);

		dispatcher.register(CommandManager.literal("ds").redirect(node));
	}

	private static int saveProcessConfigs(ServerCommandSource source, String argument, String argument2) throws CommandSyntaxException {

		if (tpe.getQueue().size() > 0)
			throw FAILED_EXCEPTION.create();

		MutableBoolean issue = new MutableBoolean(false);

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					boolean somethingChanged = FileUtil.checkChangedConfig();

					if (somethingChanged && (argument == null || !argument.equals("forceOverride"))) {
						source.sendFeedback(new LiteralText(Formatting.GOLD + "\n\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "You seem to have updated certain config files!"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "Users who already play your pack won't (!) receive those changes.\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "If you want to ship the new configs to those players too,"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "append the 'forceOverride' argument"), true);
					}
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the server list:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the config files!"), true);
					issue.setBoolean(true);
				}

				if (issue.getBoolean())
					source.sendFeedback(new LiteralText(Formatting.YELLOW + "Please inspect the log files for further information!"), true);
				else
					try {
						boolean updateExisting = argument != null && argument.equals("forceOverride");
						FileUtil.checkMD5(updateExisting, true, argument2 == null ? null : argument2);
						FileUtil.copyAndHashPrivate(false, true);
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved your mod configuration files" + (argument2 == null ? "" : " (single entry)")), true);
						boolean noFiles = FileUtil.checkForConfigFiles();
						if (noFiles)
							source.sendFeedback(new LiteralText(Formatting.YELLOW + "Warning: No config files will be shipped as the folder is still empty!"), true);

					} catch (UncheckedIOException | NullPointerException | IOException e) {
						source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the config files!"), true);
						if (e instanceof UncheckedIOException && e.getCause() instanceof NoSuchFileException)
							source.sendFeedback(new LiteralText(Formatting.RED + "It seems, no file or folder by that name exists"), true);
						DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving your configuration:", e);
					}
			}
		});

		return 0;
	}

	private static int saveProcess(ServerCommandSource source, String argument, String argument2) throws CommandSyntaxException {
		if (tpe.getQueue().size() > 0)
			throw FAILED_EXCEPTION.create();

		if ((FileUtil.keysFileExist() || FileUtil.optionsFilesExist() || FileUtil.serversFileExists()) && (argument == null || (!argument.equals("override") && !argument.equals("forceOverride")))) {
			source.sendFeedback(new LiteralText(Formatting.GOLD + "These files already exist! If you want to overwrite"), true);
			source.sendFeedback(new LiteralText(Formatting.GOLD + "them, add the 'override' argument"), true);
			return 0;
		}

		MutableBoolean issue = new MutableBoolean(false);

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					boolean somethingChanged = FileUtil.checkChanged();

					if (somethingChanged && !argument.equals("forceOverride")) {
						source.sendFeedback(new LiteralText(Formatting.GOLD + "\n\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "You seem to have updated certain config files!"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "Users who already play your pack won't (!) receive those changes.\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "If you want to ship the new configs to those players too,"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "append the 'forceOverride' argument instead of 'override'"), true);
					}
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the key configuration:", e);
				}
			}
		});

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					if (argument2 == null || argument2.equals("keybinds")) {
						FileUtil.saveKeys();
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the key configuration"), true);
						FileUtil.restoreKeys(true, false);
					}
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the key configuration:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the key configuration!"), true);
					issue.setBoolean(true);
				}
			}
		});

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					if (argument2 == null || argument2.equals("options")) {
						boolean optifine = FileUtil.saveOptions();
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the default game options" + (optifine ? " (+ Optifine)" : "")), true);
					}
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the default game options:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the default game options!"), true);
					issue.setBoolean(true);
				}
			}
		});

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					if (argument2 == null || argument2.equals("servers")) {
						FileUtil.saveServers();
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the server list"), true);
					}
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the server list:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the server list!"), true);
					issue.setBoolean(true);
				}

				if (issue.getBoolean())
					source.sendFeedback(new LiteralText(Formatting.YELLOW + "Please inspect the log files for further information!"), true);
				else
					try {
						boolean updateExisting = argument != null && argument.equals("forceOverride");
						FileUtil.checkMD5(updateExisting, false, null);
						FileUtil.copyAndHashPrivate(true, false);
					} catch (IOException e) {
						DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving your configuration:", e);
					}
			}
		});

		return 0;
	}

}

abstract class ThreadRunnable implements Runnable {

	final CommandSource supply;
	final MutableBoolean issue;

	ThreadRunnable(CommandSource supply, MutableBoolean issue) {
		this.supply = supply;
		this.issue = issue;
	}
}

class MutableBoolean {

	private boolean bool;

	public MutableBoolean(boolean bool) {
		this.bool = bool;
	}

	public boolean getBoolean() {
		return this.bool;
	}

	public void setBoolean(boolean bool) {
		this.bool = bool;
	}

}