package me.zombii.mostly_server_capes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.zombii.mostly_server_capes.MostlyServerCapes;
import me.zombii.mostly_server_capes.SkinCommandSuggestionProvider;
import me.zombii.mostly_server_capes.SkinTypes;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static me.zombii.mostly_server_capes.MostlyServerCapes.*;

public class SkinSetterCommand {

        static InputStream getSkinFromURL(String capeStringURL) {
            try {
                URL capeURL = new URL(capeStringURL);
                return capeURL.openStream();
            } catch (IOException e) {
                return null;
            }
        }

        static void setSkinFromUrl(CommandContext<ServerCommandSource> context, String url, SkinTypes type) throws CommandSyntaxException {
            if (getSkinFromURL(url) == null) throw new SimpleCommandExceptionType(Text.of("Skin does not exist, try another url or uuid")).create();

            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (!CAPE_CONFIG.hasCapeCommand(player)) {
                throw new SimpleCommandExceptionType(Text.of("This skin requires you to install the MostlyServerSideCapes mod locally.")).create();
            }

            SKIN_CONFIG.setPlayerSkin(context.getSource()
                    .getPlayerOrThrow().getGameProfile(), url, type);

            String clientNote = "Note that this skin is only visible to you and other players that have MostlyServerSideCapes installed.";
            context.getSource().sendFeedback(() -> Text.of(
                    "Skin saved. Relog for it to apply. "
                            + clientNote), true);

        }

        static void registerSkinCommand(LiteralArgumentBuilder<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
            LOGGER.info("Initialising skin command");
            LOGGER.info("Trying to load config, if it exists");
            CAPE_CONFIG.readFromConfig();

            LOGGER.info("Registering skin command");

            dispatcher.then(CommandManager.literal("skin")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .then(CommandManager.literal("byUrl")
                            .then(CommandManager.argument("stature", StringArgumentType.word())
                                    .suggests(new SkinCommandSuggestionProvider())
                                    .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String url = StringArgumentType.getString(context, "url");
                                        String type = StringArgumentType.getString(context, "stature");
                                        setSkinFromUrl(context, url, SkinTypes.valueOf(type.toUpperCase()));
                                        return 0;
                                    }))
                    ))
                    .then(CommandManager.literal("reset")
                            .executes(context -> {
                                SKIN_CONFIG.resetPlayerSkin(context.getSource()
                                        .getPlayerOrThrow().getGameProfile());
                                context.getSource().sendFeedback(() -> Text.of(
                                        "Skin reset. Relog for it to apply."), true);
                                return 0;
                            })));
        }

}
