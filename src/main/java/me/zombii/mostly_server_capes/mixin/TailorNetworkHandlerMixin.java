package me.zombii.mostly_server_capes.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import me.zombii.mostly_server_capes.MostlyServerCapes;
import me.zombii.mostly_server_capes.SkinTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.network.NetworkHandler;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Base64;
import java.util.Optional;

@Mixin(NetworkHandler.class)
public class TailorNetworkHandlerMixin {


    /**
     * @author Mr_Zombii
     * @reason Disabling Fabric Tailor's setting system
     */
    @Overwrite
    public static void onInit(ServerPlayNetworkHandler listener, MinecraftServer _server) {
    }

    /**
     * @author Mr_Zombii
     * @reason Add Fabric Tailor Compat
     */
    @Overwrite
    public static void onSkinChangePacket(ServerPlayerEntity player, Property skin, Runnable callback) {
        long lastChange = ((TailoredPlayer)player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();
        if (now - lastChange <= FabricTailor.config.skinChangeTimer * 1000L && lastChange != 0L) {
            MutableText timeLeft = Text.literal(String.valueOf((FabricTailor.config.skinChangeTimer * 1000L - now + lastChange) / 1000L)).formatted(Formatting.LIGHT_PURPLE);
            player.sendMessage(TextTranslations.create("command.fabrictailor.skin.timer.please_wait", new Object[]{timeLeft}).formatted(Formatting.RED), false);
        } else {
            String texturesJson = new String(Base64.getDecoder().decode(skin.value()));
            JsonObject textureData = JsonParser.parseString(texturesJson).getAsJsonObject().getAsJsonObject("textures");
            MostlyServerCapes.LOGGER.info("Man, Screw FabricTailor, Converting Data to MostlyServerSideCape's");
            if (textureData.get("SKIN") != null) {
                JsonObject skinObj = textureData.get("SKIN").getAsJsonObject();
                String model = "classic";
                if (skinObj.get("metadata") != null)
                    model = skinObj.getAsJsonObject("metadata").get("model").getAsString();
                SkinTypes type = SkinTypes.valueOf(model.toUpperCase());
                MostlyServerCapes.SKIN_CONFIG.setPlayerSkin(
                        player.getGameProfile(),
                        textureData.get("SKIN").getAsJsonObject().get("url").getAsString(),
                        type
                );
                MostlyServerCapes.LOGGER.info(texturesJson + " " + skinObj + " " + model);
            }
            if (textureData.get("CAPE") != null)
                MostlyServerCapes.CAPE_CONFIG.setPlayerCape(player.getGameProfile(), textureData.get("CAPE").getAsJsonObject().get("url").getAsString());
//            ((TailoredPlayer)player).fabrictailor_setSkin(skin, true);
            callback.run();
        }

    }

}
