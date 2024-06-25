package me.zombii.mostly_server_capes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static me.zombii.mostly_server_capes.Constants.INSTALLED_ID;

public class MostlyServerCapesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.configurationS2C().register(INSTALLED_ID,
				new PacketCodec<>() {
					@Override
					public CustomPayload decode(PacketByteBuf buf) {
						throw new AssertionError();
					}

					@Override
					public void encode(PacketByteBuf buf, CustomPayload value) {
						throw new AssertionError();
					}
				});
		ClientConfigurationNetworking.registerGlobalReceiver(INSTALLED_ID, (payload, context) -> {});
	}
}