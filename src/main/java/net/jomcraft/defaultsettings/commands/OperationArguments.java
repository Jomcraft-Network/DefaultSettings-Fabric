package net.jomcraft.defaultsettings.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class OperationArguments implements ArgumentType<String> {

	private static final List<String> ARGUMENTS = Arrays.asList("override", "forceOverride");
	private static final List<String> ARGUMENTS_LIMITED = Arrays.asList("forceOverride");
	private final boolean limited;

	public OperationArguments(boolean limited) {
		this.limited = limited;
	}

	public static OperationArguments operationArguments(boolean limited) {
		return new OperationArguments(limited);
	}

	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException {
		return reader.readUnquotedString();
	}

	public static String getString(final CommandContext<?> context, final String name) {
		return context.getArgument(name, String.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(this.limited ? ARGUMENTS_LIMITED : ARGUMENTS, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return ARGUMENTS;
	}
	
	public static class Serializer implements ArgumentSerializer<OperationArguments, Serializer.Properties> {

		@Override
		public void writePacket(Serializer.Properties var1, PacketByteBuf var2) {
			var2.writeBoolean(var1.limited);
		}

		@Override
		public Serializer.Properties fromPacket(PacketByteBuf var1) {
			boolean limited = var1.readBoolean();
			return new Properties(limited);
		}

		@Override
		public void writeJson(Serializer.Properties var1, JsonObject var2) {
			var2.addProperty("limited", var1.limited);
		}

		@Override
		public Serializer.Properties getArgumentTypeProperties(OperationArguments var1) {
			return new Properties(var1.limited);
		}

		public final class Properties implements ArgumentSerializer.ArgumentTypeProperties<OperationArguments> {
			
			final boolean limited;

			Properties(boolean limited) {
				this.limited = limited;
			}

			@Override
			public OperationArguments createType(CommandRegistryAccess commandRegistryAccess) {
				return new OperationArguments(this.limited);
			}

			@Override
			public ArgumentSerializer<OperationArguments, ?> getSerializer() {
				return Serializer.this;
			}
		}
	}
}