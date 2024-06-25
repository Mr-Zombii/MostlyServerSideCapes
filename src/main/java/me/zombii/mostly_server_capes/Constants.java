package me.zombii.mostly_server_capes;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Set;

public class Constants {

    public static final CustomPayload.Id<CustomPayload> INSTALLED_ID = new CustomPayload.Id<>(
            Identifier.of("capecommand", "installed"));

    public static final Set<String> UPSIDE_DOWN_PLAYERS = Set.of(
            "7b05bc2d-14d3-40b1-bf90-05a5a36649e5", // M4ximumPizza
            "27c5d8e7-889c-4c40-b63c-c1d54db72580" // Mr_Zombii
    );


}
