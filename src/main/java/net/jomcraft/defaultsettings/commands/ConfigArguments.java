package net.jomcraft.defaultsettings.commands;

import java.io.IOException;
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
import net.jomcraft.defaultsettings.DefaultSettings;
import net.jomcraft.defaultsettings.FileUtil;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.network.PacketByteBuf;

public class ConfigArguments implements ArgumentType<String> {

	private static List<String> ARGUMENTS = Arrays.asList("fml.toml", "forge-client.toml");

	public static ConfigArguments configArguments() {
		return new ConfigArguments();
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
		try {
			ARGUMENTS = FileUtil.listConfigFiles();
		} catch (IOException e) {
			DefaultSettings.log.error(e);
		}
		return CommandSource.suggestMatching(ARGUMENTS, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return ARGUMENTS;
	}
	
	public static class Serializer implements ArgumentTypeInfo<ConfigArguments, Serializer.Template> {

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
		public Template unpack(ConfigArguments type) {
			return new Template();
		}
		
		public final class Template implements ArgumentTypeInfo.Template<ConfigArguments> {
			

			Template() {
			
			}

			@Override
			public ConfigArguments instantiate(CommandBuildContext context) {
				return new ConfigArguments();
			}

			@Override
			public ArgumentTypeInfo<ConfigArguments, ?> type() {
				return Serializer.this;
			}
		}
	}
}