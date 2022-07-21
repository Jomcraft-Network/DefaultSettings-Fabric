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

public class TypeArguments implements ArgumentType<String> {

	private static final List<String> ARGUMENTS = Arrays.asList("options", "keybinds", "servers");

	public static TypeArguments typeArguments() {
		return new TypeArguments();
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
		return CommandSource.suggestMatching(ARGUMENTS, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return ARGUMENTS;
	}

	public static class Serializer implements ArgumentSerializer<TypeArguments, Serializer.Properties> {

		@Override
		public void writePacket(Serializer.Properties var1, PacketByteBuf var2) {
		
			
		}

		@Override
		public Serializer.Properties fromPacket(PacketByteBuf var1) {
			return new Properties();
		}

		@Override
		public void writeJson(Serializer.Properties var1, JsonObject var2) {
			
		}

		@Override
		public Serializer.Properties getArgumentTypeProperties(TypeArguments var1) {
			return new Properties();
		}

		public final class Properties implements ArgumentSerializer.ArgumentTypeProperties<TypeArguments> {
			

			Properties() {
			
			}

			@Override
			public TypeArguments createType(CommandRegistryAccess commandRegistryAccess) {
				return new TypeArguments();
			}

			@Override
			public ArgumentSerializer<TypeArguments, ?> getSerializer() {
				return Serializer.this;
			}
		}
	}
}