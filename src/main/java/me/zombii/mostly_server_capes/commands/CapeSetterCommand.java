package me.zombii.mostly_server_capes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.zombii.mostly_server_capes.CapeCommandSuggestionProvider;
import me.zombii.mostly_server_capes.Capes;
import me.zombii.mostly_server_capes.MostlyServerCapes;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static me.zombii.mostly_server_capes.MostlyServerCapes.CAPE_CONFIG;
import static me.zombii.mostly_server_capes.MostlyServerCapes.LOGGER;

public class CapeSetterCommand {

        static InputStream getCapeFromURL(String capeStringURL) {
            try {
                URL capeURL = new URL(capeStringURL);
                return capeURL.openStream();
            } catch (IOException e) {
                return null;
            }
        }

        static void setCapeFromUrl(CommandContext<ServerCommandSource> context, String url) throws CommandSyntaxException {
            if (getCapeFromURL(url) == null) throw new SimpleCommandExceptionType(Text.of("Cape does not exist, try another url or provider")).create();

            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (!CAPE_CONFIG.hasCapeCommand(player)) {
                throw new SimpleCommandExceptionType(Text.of("This cape requires you to install the MostlyServerSideCapes mod locally.")).create();
            }

            CAPE_CONFIG.setPlayerCape(context.getSource()
                    .getPlayerOrThrow().getGameProfile(), url);

            String clientNote = "Note that this cape is only visible to you and other players that have MostlyServerSideCapes installed.";
            context.getSource().sendFeedback(() -> Text.of(
                    "Cape saved. Relog for it to apply. "
                            + clientNote), true);

        }

        static void registerCapeCommand(LiteralArgumentBuilder<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
            LOGGER.info("Initialising cape command");
            LOGGER.info("Trying to load config, if it exists");
            CAPE_CONFIG.readFromConfig();

            LOGGER.info("Registering cape command");

            dispatcher.then(CommandManager.literal("cape")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .then(CommandManager.literal("providers")
                                    .then(CommandManager.literal("skinmc").executes(context -> {
                                        String url = String.format("https://skinmc.net/api/v1/skinmcCape/%s", context.getSource().getPlayer().getUuid());
                                        setCapeFromUrl(context, url);
                                        return 0;
                                    }))
                                    .then(CommandManager.literal("optifine").executes(context -> {
                                        String url = String.format("http://s.optifine.net/capes/%s.png", context.getSource().getPlayer().getDisplayName());
                                        setCapeFromUrl(context, url);
                                        return 0;
                                    }))
                                    .then(CommandManager.literal("minecraftapi").executes(context -> {
                                        String url = String.format("https://minecraftapi.net//api//v2//profile//%s//capes//minecraftcapes", context.getSource().getPlayer().getUuid());
                                        setCapeFromUrl(context, url);
                                        return 0;
                                    }))
                                    .then(CommandManager.literal("minecraftcapes").executes(context -> {
                                        String url = String.format("https://api.minecraftcapes.net/profile/%s/cape", context.getSource().getPlayer().getUuid().toString().replaceAll("-", ""));
                                        setCapeFromUrl(context, url);
                                        return 0;
                                    }))
                    )
                    .then(CommandManager.literal("byUrl").then(CommandManager.argument("url", StringArgumentType.greedyString())
                            .executes(context -> {
                                String url = StringArgumentType.getString(context, "url");
                                setCapeFromUrl(context, url);
                                return 0;
                            })))
                    .then(CommandManager.literal("byName").then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(new CapeCommandSuggestionProvider())
                            .executes(context -> {
                                String capeString = StringArgumentType.getString(
                                        context, "name");
                                Capes cape;
                                try {
                                    cape = Capes.valueOf(capeString.toUpperCase());
                                } catch (IllegalArgumentException exception) {
                                    throw new SimpleCommandExceptionType(Text.of("Unknown cape, try \"url\"")).create();
                                }

                                setCapeFromUrl(context, cape.getCapeURL().replaceAll("%PLAYER_NAME%", context.getSource().getDisplayName().getString()));

                                return 0;
                            })))
                    .then(CommandManager.literal("reset")
                            .executes(context -> {
                                CAPE_CONFIG.resetPlayerCape(context.getSource()
                                        .getPlayerOrThrow().getGameProfile());
                                context.getSource().sendFeedback(() -> Text.of(
                                        "Cape reset. Relog for it to apply."), true);
                                return 0;
                            })));
        }

}
