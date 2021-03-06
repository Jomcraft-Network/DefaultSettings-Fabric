package de.pt400c.defaultsettings;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CommandDefaultSettings {
	
	private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "Please wait until the last request has finished"));

	protected static void register(CommandDispatcher dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = CommandManager.literal("defaultsettings");
		
		
		literalargumentbuilder.then(CommandManager.literal("save").executes((command) -> {
	         return saveProcess(command.getSource(), null);
	      }).then(CommandManager.argument("argument", StringArgumentType.string()).executes((command) -> {
	         return saveProcess(command.getSource(), StringArgumentType.getString(command, "argument"));
	      }))).then(CommandManager.literal("export-mode").executes((command) -> {
		         return exportMode(command.getSource(), null);
		      }));
		dispatcher.register(literalargumentbuilder);
	//	event.getServer().getCommandManager().getDispatcher().register(literalargumentbuilder);
	}
	
	private static int exportMode(ServerCommandSource source, String argument) throws CommandSyntaxException {
		
		if (tpe.getQueue().size() > 0)
			throw FAILED_EXCEPTION.create();
		
		boolean exportMode = FileUtil.getMainJSON().getExportMode();
		tpe.execute(new ThreadRunnable(source, null) {

			@SuppressWarnings("static-access")
			@Override
			public void run() {
				try {
					if (exportMode) {
						FileUtil.restoreConfigs();
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully deactivated the export-mode"), true);
					} else {
						FileUtil.moveAllConfigs();
						FileUtil.checkMD5();
						source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully activated the export-mode"), true);
					}
				} catch (IOException e) {
					DefaultSettings.getInstance().log.log(Level.ERROR, "An exception occurred while trying to move the configs:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't switch the export-mode"), true);
				}
			}
		});
		return 0;

	}
	
	private static int saveProcess(ServerCommandSource source, String argument) throws CommandSyntaxException {

		if (tpe.getQueue().size() > 0)
			throw FAILED_EXCEPTION.create();
		
		if((FileUtil.keysFileExist() || FileUtil.optionsFilesExist() || FileUtil.serversFileExists()) && (argument == null || !argument.equals("-o"))) {
			source.sendFeedback(new LiteralText(Formatting.GOLD + "These files already exist! If you want to overwrite"), true);
			source.sendFeedback(new LiteralText(Formatting.GOLD + "them, add the '-o' argument"), true);
			return 0;
		}

		MutableBoolean issue = new MutableBoolean(false);

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					FileUtil.saveKeys();
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the key configuration"), true);
					FileUtil.restoreKeys(true);
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
					FileUtil.saveOptions();
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the default game options"), true);
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
					FileUtil.saveServers();
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the server list"), true);
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the server list:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the server list!"), true);
					issue.setBoolean(true);
				}

				if (issue.getBoolean())
					source.sendFeedback(new LiteralText(Formatting.YELLOW + "Please inspect the log files for further information!"), true);
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