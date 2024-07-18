package me.zombii.mostly_server_capes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
    }

    static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
        LiteralArgumentBuilder<ServerCommandSource> dispatcher2 = CommandManager.literal("mostlyserversidecapes");
        CapeSetterCommand.registerCapeCommand(dispatcher2, registryAccess, env);
        SkinSetterCommand.registerSkinCommand(dispatcher2, registryAccess, env);
        dispatcher.register(dispatcher2);
    }

}
