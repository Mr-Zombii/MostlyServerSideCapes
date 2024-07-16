package me.zombii.mostly_server_capes;

import com.mojang.authlib.GameProfile;
import me.zombii.mostly_server_capes.commands.Commands;
import me.zombii.mostly_server_capes.mixin.ServerConfigurationNetworkHandlerAccessor;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.zombii.mostly_server_capes.Constants.INSTALLED_ID;

public class MostlyServerCapes implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("mostly_server_capes");
	public static final CapeConfig CAPE_CONFIG = new CapeConfig();
	public static final SkinConfig SKIN_CONFIG = new SkinConfig();

	@Override
	public void onInitialize() {
		Commands.register();
		LOGGER.info("Registering server network handlers");
		S2CConfigurationChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
			if (ServerConfigurationNetworking.canSend(handler, INSTALLED_ID)) {
				GameProfile profile = ((ServerConfigurationNetworkHandlerAccessor) handler).getProfile();
				LOGGER.info("Player {} has cape commands installed client side", profile.getName());
				CAPE_CONFIG.registerCapeCommandPlayer(profile);
				SKIN_CONFIG.registerSkinCommandPlayer(profile);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register(
				((handler, server) -> {
					CAPE_CONFIG.unregisterCapeCommandPlayer(handler.getPlayer());
					SKIN_CONFIG.unregisterSkinCommandPlayer(handler.getPlayer());
				}));
	}
}