package me.szumielxd.mirrornpc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

public class MirrorNPCCommand {
	
	
	public MirrorNPCCommand(Citizens citizens) {
	}
	
	
	@Command(
			aliases = { "npc" },
			modifiers = { "mirror" },
			usage = "mirror",
			desc = "Toggle player skin mirroring",
			min = 1,
			max = 1,
			permission = "citizens.npc.name")
	@Requirements(selected = true, ownership = true, types = EntityType.PLAYER)
	public void mirror(CommandContext args, CommandSender sender, final NPC npc) {
		if (!npc.hasTrait(MirrorSkinTrait.class)) npc.addTrait(MirrorSkinTrait.class);
		MirrorSkinTrait trait = npc.getTraitNullable(MirrorSkinTrait.class);
		final Location loc = npc.getEntity().getLocation();
		Bukkit.getScheduler().runTask(MirrorNPC.getInstance(), () -> {
			if (npc.isSpawned()) {
				npc.despawn(DespawnReason.PENDING_RESPAWN);
				Bukkit.getScheduler().runTask(MirrorNPC.getInstance(), () -> {
					npc.spawn(loc, SpawnReason.RESPAWN);
				});
			}
		});
		Messaging.send(sender, trait.setMirrorSkin(!trait.hasMirrorSkin()) ? "Skin mirroring toggled on" : "Skin mirroring toggled off");
	}
	

}
