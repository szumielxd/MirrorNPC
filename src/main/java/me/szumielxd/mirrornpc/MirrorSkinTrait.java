package me.szumielxd.mirrornpc;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists data related to {@link Player} NPCs.
 */
@TraitName("mirrorskintrait")
public class MirrorSkinTrait extends Trait {
	@Persist
	private boolean mirrorSkin = false;

	public MirrorSkinTrait() {
		super("mirrorskintrait");
	}

	public boolean hasMirrorSkin() {
		return this.mirrorSkin;
	}

	/**
	 * @param boolean whether npc should have skin mirrored to 
	 * skin of player who watching him
	 */
	public boolean setMirrorSkin(boolean mirrorSkin) {
		return this.mirrorSkin = mirrorSkin;
	}
}
