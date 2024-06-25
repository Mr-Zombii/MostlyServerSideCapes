package me.zombii.mostly_server_capes.mixin;

import com.mojang.authlib.GameProfile;
import me.zombii.mostly_server_capes.network.PlayerEntityAccess;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public class ServerPlayerEntityMixin implements PlayerEntityAccess {

    @Shadow
    @Final
    @Mutable
    private GameProfile gameProfile;


    @Override
    public void mostlyServerSideCapes$setProfile(GameProfile profile) {
        gameProfile = profile;
    }
}
