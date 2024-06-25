package me.zombii.mostly_server_capes.mixin;

import me.zombii.mostly_server_capes.network.PlayerListS2CPacketEntriesUpdater;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public class PlayerListS2CPacketMixin implements PlayerListS2CPacketEntriesUpdater {

    @Shadow
    @Final
    @Mutable
    private List<Entry> entries;

    @Override
    @Unique
    public void capeCommand$setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}