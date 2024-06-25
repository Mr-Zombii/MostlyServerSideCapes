package me.zombii.mostly_server_capes.mixin.client;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

@Mixin(MinecraftProfileTexture.class)
public class HASH_FIX {

    @Shadow @Final private String url;

    /**
     * @author Mr_Zombii
     * @reason Remove the ability to cache items
     */
    @Overwrite(remap = false)
    public String getHash() {
        try {
            new URL(url).getPath();
            return FilenameUtils.getBaseName(String.valueOf(new Date().getTime()));
        } catch (final MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid profile texture url");
        }
    }
}
