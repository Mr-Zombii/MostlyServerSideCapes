package me.zombii.mostly_server_capes.network;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;

import java.util.List;

public interface PlayerListS2CPacketEntriesUpdater {

    void capeCommand$setEntries(List<Entry> entries);
}