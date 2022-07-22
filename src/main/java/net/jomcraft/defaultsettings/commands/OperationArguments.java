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
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypeInfo;
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
	
	public static class Serializer implements ArgumentTypeInfo<OperationArguments, Serializer.Template> {

		@Override
		public void serializeToNetwork(Template template, PacketByteBuf buf) {
			buf.writeBoolean(template.limited);
		}

		@Override
		public Template deserializeFromNetwork(PacketByteBuf buf) {
			boolean limited = buf.readBoolean();
			return new Template(limited);
		}

		@Override
		public void serializeToJson(Template template, JsonObject jsonObject) {
			jsonObject.addProperty("limited", template.limited);
		}

		@Override
		public Template unpack(OperationArguments type) {
			return new Template(type.limited);
		}
		
		public final class Template implements ArgumentTypeInfo.Template<OperationArguments> {
			
			final boolean limited;

			Template(boolean limited) {
				this.limited = limited;
			}

			@Override
			public OperationArguments instantiate(CommandBuildContext context) {
				return new OperationArguments(this.limited);
			}

			@Override
			public ArgumentTypeInfo<OperationArguments, ?> type() {
				return Serializer.this;
			}
		}
	}
}