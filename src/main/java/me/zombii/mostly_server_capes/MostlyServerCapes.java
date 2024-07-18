package me.zombii.mostly_server_capes;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import me.zombii.mostly_server_capes.commands.Commands;
import me.zombii.mostly_server_capes.mixin.ServerConfigurationNetworkHandlerAccessor;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.source.BiomeAccess;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.mixin.accessors.AChunkMap;
import org.samo_lego.fabrictailor.mixin.accessors.ATrackedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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
	
	public static void reloadPlayer(ServerPlayerEntity entity) {
		if (entity.getServer() == null) {
			FabricTailor.errorLog("Tried to reload skin form client side! This should not happen!");
		} else {
			PlayerManager playerManager = entity.getServer().getPlayerManager();
			playerManager.sendToAll(new PlayerRemoveS2CPacket(new ArrayList(Collections.singleton(entity.getUuid()))));
			playerManager.sendToAll(PlayerListS2CPacket.entryFromPlayer(Collections.singleton(entity)));

			ServerWorld level = entity.getServerWorld();
			entity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(new CommonPlayerSpawnInfo(level.getDimensionEntry(), level.getRegistryKey(), BiomeAccess.hashSeed(level.getSeed()), entity.interactionManager.getGameMode(), entity.interactionManager.getPreviousGameMode(), level.isDebugWorld(), level.isFlat(), entity.getLastDeathPos(), entity.getPortalCooldown()), (byte)3));
			entity.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), Collections.emptySet(), 0));
			entity.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(entity.getInventory().selectedSlot));
			entity.networkHandler.sendPacket(new DifficultyS2CPacket(level.getDifficulty(), level.getLevelProperties().isDifficultyLocked()));
			entity.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(entity.experienceProgress, entity.totalExperience, entity.experienceLevel));
			playerManager.sendWorldInfo(entity, level);
			playerManager.sendCommandTree(entity);
			entity.networkHandler.sendPacket(new HealthUpdateS2CPacket(entity.getHealth(), entity.getHungerManager().getFoodLevel(), entity.getHungerManager().getSaturationLevel()));
			Iterator var6 = entity.getStatusEffects().iterator();

			while(var6.hasNext()) {
				StatusEffectInstance statusEffect = (StatusEffectInstance)var6.next();
				entity.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entity.getId(), statusEffect, false));
			}
			ArrayList<Pair<EquipmentSlot, ItemStack>> equipmentList = new ArrayList();
			EquipmentSlot[] var13 = EquipmentSlot.values();
			int var8 = var13.length;

			for(int var9 = 0; var9 < var8; ++var9) {
				EquipmentSlot equipmentSlot = var13[var9];
				ItemStack itemStack = entity.getEquippedStack(equipmentSlot);
				if (!itemStack.isEmpty()) {
					equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
				}
			}

			if (!equipmentList.isEmpty()) {
				entity.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(entity.getId(), equipmentList));
			}

			if (!entity.getPassengerList().isEmpty()) {
				entity.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
			}

			if (entity.hasVehicle()) {
				entity.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity.getVehicle()));
			}

			entity.sendAbilitiesUpdate();
			playerManager.sendPlayerStatus(entity);
		}
	}
}