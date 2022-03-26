package me.szumielxd.mirrornpc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
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
							NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(data.getProfile().getUUID());
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
						});
					}
				}
			}
		});
	}
	
	
	
	@Override
	public void onDisable() {
		CitizensAPI.getTraitFactory().deregisterTrait(TraitInfo.create(MirrorSkinTrait.class));
	}
	
	
	
	
	
	
	
	
	
	

}
