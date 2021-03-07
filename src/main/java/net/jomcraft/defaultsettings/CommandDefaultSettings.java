package net.jomcraft.defaultsettings;

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
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CommandDefaultSettings {
	
	private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "Please wait until the last request has finished"));

	protected static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = CommandManager.literal("defaultsettings");
		
		
		literalargumentbuilder.then(CommandManager.literal("save").executes((command) -> {
	         return saveProcess(command.getSource(), null);
	      }).then(CommandManager.argument("argument", StringArgumentType.string()).executes((command) -> {
	         return saveProcess(command.getSource(), StringArgumentType.getString(command, "argument"));
	      })));
		LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literalargumentbuilder);
		
		dispatcher.register(CommandManager.literal("ds").redirect(node));
	}
	
	private static int saveProcess(ServerCommandSource source, String argument) throws CommandSyntaxException {

		if (tpe.getQueue().size() > 0)
			throw FAILED_EXCEPTION.create();
		
		if((FileUtil.keysFileExist() || FileUtil.optionsFilesExist() || FileUtil.serversFileExists()) && (argument == null || (!argument.equals("-o") && !argument.equals("-of")))) {
			source.sendFeedback(new LiteralText(Formatting.GOLD + "These files already exist! If you want to overwrite"), true);
			source.sendFeedback(new LiteralText(Formatting.GOLD + "them, add the '-o' argument"), true);
			return 0;
		}
		
		MutableBoolean issue = new MutableBoolean(false);

		tpe.execute(new ThreadRunnable(source, issue) {

			@Override
			public void run() {
				try {
					boolean somethingChanged = FileUtil.checkChanged();

					if(somethingChanged && !argument.equals("-of")) {
						source.sendFeedback(new LiteralText(Formatting.GOLD + "\n\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "You seem to have updated certain config files!"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "Users who already play your pack won't (!) receive those changes.\n"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "If you want to ship the new configs to those players too,"), true);
						source.sendFeedback(new LiteralText(Formatting.GOLD + "append the '-of' argument instead of '-o'"), true);
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
					FileUtil.saveKeys(false);
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the key configuration"), true);
					FileUtil.restoreKeys(true, false);
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
					boolean optifine = FileUtil.saveOptions(false);
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the default game options" + (optifine ? " (+ Optifine)" : "")), true);
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
					FileUtil.saveServers(false);
					source.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully saved the server list"), true);
				} catch (Exception e) {
					DefaultSettings.log.log(Level.ERROR, "An exception occurred while saving the server list:", e);
					source.sendFeedback(new LiteralText(Formatting.RED + "Couldn't save the server list!"), true);
					issue.setBoolean(true);
				}

				if (issue.getBoolean())
					source.sendFeedback(new LiteralText(Formatting.YELLOW + "Please inspect the log files for further information!"), true);
				else
					try {
						boolean updateExisting = argument != null && argument.equals("-of");
						FileUtil.checkMD5(updateExisting);
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