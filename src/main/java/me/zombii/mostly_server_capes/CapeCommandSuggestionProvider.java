package me.zombii.mostly_server_capes;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CapeCommandSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        return CommandSource.suggestMatching(
                Arrays.stream(Capes.values())
                        .map(cape -> cape.toString().toLowerCase()), builder);
    }
}