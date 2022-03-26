package me.szumielxd.mirrornpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;

public class MirrorNPC extends JavaPlugin implements Listener {
	
	
	private static MirrorNPC instance = null;
	public static MirrorNPC getInstance() {
		return instance;
	}
	
	private ProtocolManager protocolManager = null;
	
	
	private static final String NMS = Bukkit.getServer().getClass().getName().split("\\.")[3];
	private static final Class<?> CraftWorld = Optional.of(String.format("org.bukkit.craftbukkit.%s.CraftWorld", NMS)).map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	private static final Class<?> WorldServer = Optional.of(String.format("net.minecraft.server.%s.WorldServer", NMS)).map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	private static final Class<?> NMSEntity = Optional.of(String.format("net.minecraft.server.%s.Entity", NMS)).map(clazz -> { try { return Class.forName(clazz); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	private static final Method CraftWorld_getHandle = Optional.of("getHandle").map(method -> { try { return CraftWorld.getMethod(method); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	private static final Method WorldServer_getEntity = Optional.of("getEntity").map(method -> { try { return WorldServer.getMethod(method, UUID.class); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	private static final Method NMSEntity_getBukkitEntity = Optional.of("getBukkitEntity").map(method -> { try { return NMSEntity.getMethod(method); } catch (Exception e) { throw new RuntimeException(e); } }).get();
	
	
	@Override
	public void onEnable() {
		instance = this;
		Citizens citizens = (Citizens) CitizensAPI.getPlugin();
		citizens.registerCommandClass(MirrorNPCCommand.class);
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MirrorSkinTrait.class));
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
					PacketContainer packet = event.getPacket();
					if (packet.getPlayerInfoAction().read(0).equals(PlayerInfoAction.ADD_PLAYER)) {
						List<PlayerInfoData> dataList = packet.getPlayerInfoDataLists().read(0);
						dataList.forEach(data -> {
							Entity entity = getEntity(data.getProfile().getUUID());
							if (entity != null) {
								NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
								if (npc != null && npc.hasTrait(MirrorSkinTrait.class)) {
									MirrorSkinTrait trait = npc.getTraitNullable(MirrorSkinTrait.class);
									if (trait.hasMirrorSkin()) {
										List<WrappedSignedProperty> textures = new ArrayList<>(data.getProfile().getProperties().get("textures"));
										textures.removeIf(val -> val.getName().equals("textures"));
										try {
											GameProfile profile = (GameProfile) event.getPlayer().getClass().getMethod("getProfile").invoke(event.getPlayer());
											List<Property> properties = new ArrayList<>(profile.getProperties().get("textures"));
											Property texture = null;
											if (!properties.isEmpty()) for (Property prop : properties) {
												if (prop.getName().equals("textures")) {
													texture = prop;
													break;
												}
											}
											if (texture != null) textures.add(WrappedSignedProperty.fromValues("textures", texture.getValue(), texture.getSignature()));
											data.getProfile().getProperties().replaceValues("textures", textures);
											
										} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
												| NoSuchMethodException | SecurityException e) {
											e.printStackTrace();
										}
									}
								}
							}
						});
					}
				}
			}
		});
	}
	
	
	private static Entity getEntity(UUID uuid) {
		for (World world : Bukkit.getWorlds()) {
			try {
				Object nmsWorld = CraftWorld_getHandle.invoke(world);
				Object entity = WorldServer_getEntity.invoke(nmsWorld, uuid);
				if (entity != null) return (Entity) NMSEntity_getBukkitEntity.invoke(entity);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	
	
	@Override
	public void onDisable() {
		CitizensAPI.getTraitFactory().deregisterTrait(TraitInfo.create(MirrorSkinTrait.class));
	}
	
	
	
	
	
	
	
	
	
	

}
