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

	public static class Serializer implements ArgumentTypeInfo<TypeArguments, Serializer.Template> {

		@Override
		public void serializeToNetwork(Template template, PacketByteBuf buf) {
			
		}

		@Override
		public Template deserializeFromNetwork(PacketByteBuf buf) {
			return new Template();
		}

		@Override
		public void serializeToJson(Template template, JsonObject jsonObject) {
			
		}

		@Override
		public Template unpack(TypeArguments type) {
			return new Template();
		}
		
		public final class Template implements ArgumentTypeInfo.Template<TypeArguments> {
			

			Template() {
			
			}

			@Override
			public TypeArguments instantiate(CommandBuildContext context) {
				return new TypeArguments();
			}

			@Override
			public ArgumentTypeInfo<TypeArguments, ?> type() {
				return Serializer.this;
			}
		}
	}
}