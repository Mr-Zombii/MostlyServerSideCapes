package me.zombii.mostly_server_capes.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
    }

    static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
        CapeSetterCommand.registerCapeCommand(dispatcher, registryAccess, env);
        SkinSetterCommand.registerSkinCommand(dispatcher, registryAccess, env);
    }

}
