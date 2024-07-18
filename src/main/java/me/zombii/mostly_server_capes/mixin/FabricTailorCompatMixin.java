package me.zombii.mostly_server_capes.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import me.zombii.mostly_server_capes.MostlyServerCapes;
import me.zombii.mostly_server_capes.SkinTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.compatibility.TaterzenSkins;
import org.samo_lego.fabrictailor.util.TextTranslations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Base64;
import java.util.function.Supplier;

import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;
import static org.samo_lego.fabrictailor.FabricTailor.config;

@Mixin(SkinCommand.class)
public class FabricTailorCompatMixin {

    @Shadow @Final private static MutableText SET_SKIN_ATTEMPT;

    @Shadow @Final private static boolean TATERZENS_LOADED;

    @Shadow @Final private static MutableText SKIN_SET_ERROR;

    /**
     * @author Mr_Zombii
     * @reason Add Compat Between This Mod & FabricTailor
     */
    @Overwrite(remap = false)
    public static void setSkin(ServerPlayerEntity player, Supplier<Property> skinProvider) {
        long lastChange = ((TailoredPlayer) player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();

        if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            player.sendMessageToClient(SET_SKIN_ATTEMPT.formatted(Formatting.AQUA), false);
            THREADPOOL.submit(() -> {
                Property skinData = skinProvider.get();

                if (skinData == null) {
                    player.sendMessageToClient(SKIN_SET_ERROR, false);
                } else {
                    if (!TATERZENS_LOADED || !TaterzenSkins.setTaterzenSkin(player, skinData)) {
                        String texturesJson = new String(Base64.getDecoder().decode(skinData.value()));
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
                        }
                        if (textureData.get("CAPE") != null)
                            MostlyServerCapes.CAPE_CONFIG.setPlayerCape(player.getGameProfile(), textureData.get("CAPE").getAsJsonObject().get("url").getAsString());
                        //                        ((TailoredPlayer) player).fabrictailor_setSkin(skinData, true);

                        String clientNote = "Note that this cape/skin is only visible to you and other players that have MostlyServerSideCapes installed.";
                        player.sendMessageToClient(Text.of(
                                "Cape/Skin saved. Relog for it to apply. "
                                        + clientNote), true);
                    }
                    player.sendMessageToClient(TextTranslations.create("command.fabrictailor.skin.set.success").formatted(Formatting.GREEN), false);
                }
            });
        } else {
            // Prevent skin change spamming
            MutableText timeLeft = Text.literal(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                    .formatted(Formatting.LIGHT_PURPLE);
            player.sendMessageToClient(
                    TextTranslations.create("command.fabrictailor.skin.timer.please_wait", timeLeft)
                            .formatted(Formatting.RED),
                    false
            );
        }

    }

}
