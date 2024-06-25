package me.zombii.mostly_server_capes.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.zombii.mostly_server_capes.MostlyServerCapes;
import me.zombii.mostly_server_capes.network.PlayerEntityAccess;
import me.zombii.mostly_server_capes.network.PlayerListS2CPacketEntriesUpdater;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.security.*;
import java.util.*;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server,
                                         ClientConnection connection,
                                         ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {

        if (packet instanceof PlayerListS2CPacket playerListS2CPacket) {
            if (playerListS2CPacket.getActions().contains(Action.ADD_PLAYER)) {
                List<Entry> entries = new ArrayList<>();
                for (Entry entry : playerListS2CPacket.getEntries()) {
                    GameProfile profile = entry.profile();
                    if (profile != null
                            && (MostlyServerCapes.CONFIG.hasCapeCommand(player) || profile.getId().equals(player.getUuid()))
                            && MostlyServerCapes.CONFIG.getPlayerCape(profile) != null) {
                        GameProfile newProfile = new GameProfile(profile.getId(), profile.getName());

                        assert entry.profile() != null;
                        newProfile.getProperties().putAll(entry.profile().getProperties());
                        setCustomCapeInGameProfile(newProfile);
                        profile = newProfile;

                        ((PlayerEntityAccess) this.player).mostlyServerSideCapes$setProfile(profile);
                    }
                    entries.add(new Entry(entry.profileId(), profile, entry.listed(),
                            entry.latency(), entry.gameMode(), entry.displayName(),
                            entry.chatSession()));
                }
                ((PlayerListS2CPacketEntriesUpdater) playerListS2CPacket).capeCommand$setEntries(
                        entries);

                this.server.getPlayerManager().getPlayerList().forEach(player -> {
                    if (player != null) {
                        if (player != this.player) {
                            player.networkHandler.send(fixPacket(entries), callbacks);
                        }
                    }
                });
            }
        }
        super.send(packet, callbacks);
    }

    @Unique
    private PlayerListS2CPacket fixPacket(List<Entry> entries) {
        EnumSet<Action> enumSet = EnumSet.of(Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY);
        PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket(enumSet, List.of());
        ((PlayerListS2CPacketEntriesUpdater) playerListS2CPacket).capeCommand$setEntries(entries);
        return playerListS2CPacket;
    }

    @Unique
    private void setCustomCapeInGameProfile(GameProfile gameProfile) {
        Property texturesProperty = gameProfile.getProperties().get("textures").stream().findAny()
                .orElse(null);
        JsonObject textures;
        if (texturesProperty != null) {
            String texturesJson = new String(Base64.getDecoder().decode(texturesProperty.value()));
            textures = JsonParser.parseString(texturesJson).getAsJsonObject();
        } else {
            // Create an empty textures object for offline players / dev accounts
            textures = new JsonObject();
            textures.add("textures", new JsonObject());
        }

        JsonObject capeObject;
        if (textures.getAsJsonObject("textures").get("CAPE") != null) {
            capeObject = textures.getAsJsonObject("textures").getAsJsonObject("CAPE");
        } else {
            capeObject = new JsonObject();
            textures.getAsJsonObject("textures").add("CAPE", capeObject);
        }
        capeObject.remove("alias");
        capeObject.addProperty("alias", String.valueOf(new Date().getTime()));
        capeObject.remove("url");
        capeObject.addProperty("url", MostlyServerCapes.CONFIG.getPlayerCape(gameProfile));

        textures.remove("signatureRequired");
        textures.add("signatureRequired", new JsonPrimitive(true));

        String newTextures = Base64.getEncoder().encodeToString(textures.toString().getBytes());
        Property newTexturesProperty;
        try {
            Signature s = Signature.getInstance("SHA1withRSA");
            PrivateKey key = NetworkEncryptionUtils.decodeRsaPrivateKeyPem("-----BEGIN RSA PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCXWH07BqcvG7zOXIU27RD+SWoA\ngSsbjwal4JXfMPJtJ0FMkZT60i3AGvpLx/3Z2hWxFC+rjJ/YyU7jwZ4UecxJXMByDCWhOLC9oFs8\n+jXefpnhJQnL1R4J42FBDOP+k4gLyoWiTVFhDnAeWJe+AHVPZoYpQiy4O6OxAnajWggUu7RMEe6/\n1U4pkainqO4IbwIU9HSI0Aj2CJaxwqplEZ2Ndk0FVAbjT+gTKU7jzTmAyucWvKgq+J9zzbqGox7e\nFdqa8lKeix1qZgg5HEX5+1U0zjo/j9lYqwpd2Dx6ltP8HX5233d1o1CCCiQiUxbQwpwBD7a1Kbes\ns8nVwHtHNUExAgMBAAECggEADNjDEI6ZBGOrXCz4Vdg7uAoNSGuueBFk9BGjRyXFfkR1w4UcC0pp\n/cOMeYBJaQOdEcHv5fTy4Jj97FFUFNLd52BLKaMkUpIYVpBVDlEC35cJgtX9BeNaQGafq8DnXYWy\nZ6XSQBumC8II/FpyzvsE+i9utfPbDexqEa1u2qDazL+ct8Lp+v/1N2v0gxXCtgangwQm+WhrMb33\nbG6Hb/+7SyyXZiDwuZQGb+NH7xAlUWNobwV6mOianm9Edpu7VdiKYNqckOA1KssjAU2XcfEjLKOK\nlrOVrmWuBf0zBghbyX1DLzSoIo6l6/ZuzWFlkskQ2WOrTCuNy/DeOvmrPFyaPQKBgQC+TCgP79P8\nBMbV8peDm/GpNmze7f+wufB7+E13lJi9qOkBVwpdvKrhHEle5kMTHVTyb/ld/2QB/GMjA4QOySml\n/HyI7yBrgzrtmrnJDBlSEKqLR9dLASjQ1277/2zLRNTX3C66+5HQf8bb1qy79BZ7wVQMYrTt/Bij\nHnNtuDbg0wKBgQDLmX5iJx7mlNbKd9nhh3CHgEDmkjyBjUNaoCZgBqyKKjqezYTBSWZlQoq4PxDr\njXEtjmxVJpnt0A2MJsDpch2SqY0NxRoRnv9lVQzQPm8lKLVSLvsgfzS14b9OOtvnOLso0IoE5lwH\nFy9SPnq7TtxHKNaN76ljahNMFxHdGY/zawKBgAKu2DHBU9/NwW/qx7AXVsTn+4j5Gg3H0VguHAl/\nkte5te9K5t6DdnVODMrFvFRcqYHxijaFD0fn4w3vUsFSnL+2W5sio/ZgF0iaUdw/y2uYyI7GKIbq\nMUUghHQRGduT0NxqQk5olZm283rOAAl2W4rLIwA5tAtW7pH+L+pwGnX5AoGAJ2xXToYos5lKZfYD\nGGpzXal156+VS7igvCGajKl+K1q18x30gMDHtP/HgJmlmxbOPXYAgBRzZdsNZH/0hr9z/nwaNfpe\na98PsP1g98m/F8DkKz+xnL9E45sMwgcfoYwYoXSP5Rb95tmUbiZu+WnxuU2tDdrEP93AUtF8wgU7\nHHUCgYB0qXO+BRcqG5QrGs5zBj8r/hR2uwawx1+IzDO07AlQzsTcARfPHo/i2XucNfjDqSz3QnI5\nToXMvISxRO/HjdTnN9Dtzr7wfvD0eEJhuLCY8/hYgfDHrnm9nwViX+yGZRFFG2qtMlkuYdzEXala\n0JPdaUiMF88VXG83DrWHdqkJYg==\n-----END RSA PRIVATE KEY-----");
            System.out.println(key.getEncoded().length);
            s.initSign(key);
            s.update(newTextures.getBytes());
            newTexturesProperty = new Property("textures", newTextures, Base64.getEncoder().encodeToString(s.sign()));
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NetworkEncryptionException e) {
            throw new RuntimeException(e);
        }

        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", newTexturesProperty);
    }
}