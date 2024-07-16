package me.zombii.mostly_server_capes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

public class SkinConfig {

    private static final Path CONFIG_FILE = Path.of("playerskins.json");
    private final Map<UUID, Pair<String, SkinTypes>> playerSkins = new HashMap<>();
    private final List<GameProfile> skinCommandPlayers = new ArrayList<>();

    public String getPlayerSkin(GameProfile gameProfile) {
        if (playerSkins.get(gameProfile.getId()) == null) return null;
        return playerSkins.get(gameProfile.getId()).getA();
    }

    public SkinTypes getPlayerSkinType(GameProfile gameProfile) {
        if (playerSkins.get(gameProfile.getId()) == null) return null;
        return playerSkins.get(gameProfile.getId()).getB();
    }

    public void setPlayerSkin(GameProfile gameProfile, String skinUrl) {
        playerSkins.put(gameProfile.getId(), new Pair<>(skinUrl, SkinTypes.CLASSIC));
        writeToConfig();
    }

    public void setPlayerSkin(GameProfile gameProfile, String skinUrl, SkinTypes type) {
        playerSkins.put(gameProfile.getId(), new Pair<>(skinUrl, type));
        writeToConfig();
    }

    public void resetPlayerSkin(GameProfile gameProfile) {
        playerSkins.remove(gameProfile.getId());
        writeToConfig();
    }

    public void registerSkinCommandPlayer(GameProfile serverPlayerEntity) {
        skinCommandPlayers.add(serverPlayerEntity);
    }

    public boolean hasSkinCommand(ServerPlayerEntity serverPlayerEntity) {
        return skinCommandPlayers.contains(serverPlayerEntity.getGameProfile());
    }

    public void unregisterSkinCommandPlayer(ServerPlayerEntity serverPlayerEntity) {
        skinCommandPlayers.remove(serverPlayerEntity.getGameProfile());
    }

    private void writeToConfig() {
        JsonObject capesJson = new JsonObject();
        for (Entry<UUID, Pair<String, SkinTypes>> playerSkin : playerSkins.entrySet()) {
            JsonObject json2 = new JsonObject();
            json2.addProperty("url", playerSkin.getValue().getA());
            json2.addProperty("stature", playerSkin.getValue().getB().toString());
            capesJson.add(playerSkin.getKey().toString(), json2);
        }

        Path capeConfigPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        try {
            Files.writeString(capeConfigPath, capesJson.toString());
        } catch (IOException exception) {
            MostlyServerCapes.LOGGER.warn("Failed to save player cape config!", exception);
        }
    }

    public void readFromConfig() {
        Path capeConfigPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(capeConfigPath)) {
            try {
                JsonObject capesJson = JsonParser.parseString(Files.readString(capeConfigPath))
                        .getAsJsonObject();
                for (Entry<String, JsonElement> playerCape : capesJson.entrySet()) {
                    try {
                        playerSkins.put(UUID.fromString(playerCape.getKey()),
                                new Pair<>(
                                        playerCape.getValue().getAsJsonObject().get("url").getAsString(),
                                        SkinTypes.valueOf(playerCape.getValue().getAsJsonObject().get("stature").getAsString())
                                ));
                    } catch (IllegalArgumentException exception) {
                        MostlyServerCapes.LOGGER.warn("Read invalid skin for UUID " + playerCape.getKey()
                                + "! (" + playerCape.getValue().getAsString() + ")");
                    }
                }
            } catch (IOException exception) {
                MostlyServerCapes.LOGGER.warn("Failed to read player skin config!", exception);
            }
        }
    }
}