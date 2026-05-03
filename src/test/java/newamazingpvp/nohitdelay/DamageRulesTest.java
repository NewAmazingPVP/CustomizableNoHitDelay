package newamazingpvp.nohitdelay;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DamageRulesTest {
    @Test
    public void keepsExistingPvpModesWorking() {
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "pvp", true, true, "PLAYER", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "pvp", false, true, "ZOMBIE", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));

        assertTrue(DamageRules.shouldApplyEntityDamage(
                "evp", false, true, "ZOMBIE", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "evp", true, true, "PLAYER", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));

        assertTrue(DamageRules.shouldApplyEntityDamage(
                "player-only", true, true, "PLAYER", false, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "player-only", false, true, "ZOMBIE", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));
    }

    @Test
    public void crystalModeOnlyMatchesEndCrystalExplosions() {
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "crystal", false, false, "ENDER_CRYSTAL", true, true, "ENTITY_EXPLOSION", Collections.<String>emptyList(), false));
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "crystal", false, false, "minecraft:end_crystal", true, true, "ENTITY_EXPLOSION", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "crystal", false, false, "PRIMED_TNT", true, true, "ENTITY_EXPLOSION", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "crystal", false, false, "ENDER_CRYSTAL", true, true, "CUSTOM", Collections.<String>emptyList(), false));
    }

    @Test
    public void anchorModeOnlyMatchesRespawnAnchorExplosions() {
        assertTrue(DamageRules.shouldApplyBlockDamage(
                "anchor", true, "RESPAWN_ANCHOR", "BLOCK_EXPLOSION", Collections.<String>emptyList(), false));
        assertTrue(DamageRules.shouldApplyBlockDamage(
                "anchor", true, "minecraft:respawn-anchor", "BLOCK_EXPLOSION", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyBlockDamage(
                "anchor", true, "BED_BLOCK", "BLOCK_EXPLOSION", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyBlockDamage(
                "anchor", true, "RESPAWN_ANCHOR", "CUSTOM", Collections.<String>emptyList(), false));
    }

    @Test
    public void crystalAnchorModeMatchesBothRequestedTargets() {
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "crystal-anchor", false, false, "ENDER_CRYSTAL", true, true, "ENTITY_EXPLOSION", Collections.<String>emptyList(), false));
        assertTrue(DamageRules.shouldApplyBlockDamage(
                "crystal-anchor", true, "RESPAWN_ANCHOR", "BLOCK_EXPLOSION", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "crystal-anchor", true, true, "PLAYER", true, true, "ENTITY_ATTACK", Collections.<String>emptyList(), false));
        assertFalse(DamageRules.shouldApplyBlockDamage(
                "crystal-anchor", true, "TNT", "BLOCK_EXPLOSION", Collections.<String>emptyList(), false));
    }

    @Test
    public void customModeUsesConfiguredEntitiesAndBlocks() {
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "custom", false, true, "zombie", true, true, "ENTITY_ATTACK", Arrays.asList("minecraft:zombie"), false));
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "custom", false, true, "SKELETON", true, true, "ENTITY_ATTACK", Arrays.asList("minecraft:zombie"), false));

        assertTrue(DamageRules.shouldApplyBlockDamage(
                "custom", true, "minecraft:respawn_anchor", "BLOCK_EXPLOSION", Arrays.asList("respawn-anchor"), false));
        assertFalse(DamageRules.shouldApplyBlockDamage(
                "custom", true, "TNT", "BLOCK_EXPLOSION", Arrays.asList("respawn-anchor"), false));
    }

    @Test
    public void customModeCanRequireExplosions() {
        assertFalse(DamageRules.shouldApplyEntityDamage(
                "custom", false, true, "ZOMBIE", true, true, "ENTITY_ATTACK", Arrays.asList("ZOMBIE"), true));
        assertTrue(DamageRules.shouldApplyEntityDamage(
                "custom", false, true, "ENDER_CRYSTAL", true, true, "ENTITY_EXPLOSION", Arrays.asList("ENDER_CRYSTAL"), true));
        assertTrue(DamageRules.shouldApplyBlockDamage(
                "custom", true, "RESPAWN_ANCHOR", "BLOCK_EXPLOSION", Arrays.asList("RESPAWN_ANCHOR"), true));
    }

    @Test
    public void blockTrackingOnlyTracksTargetedBlockModes() {
        assertTrue(DamageRules.shouldTrackBlock("anchor", "RESPAWN_ANCHOR", Collections.<String>emptyList()));
        assertTrue(DamageRules.shouldTrackBlock("crystal-anchor", "minecraft:respawn_anchor", Collections.<String>emptyList()));
        assertTrue(DamageRules.shouldTrackBlock("custom", "TNT", Arrays.asList("minecraft:tnt")));
        assertFalse(DamageRules.shouldTrackBlock("crystal", "RESPAWN_ANCHOR", Collections.<String>emptyList()));
        assertFalse(DamageRules.shouldTrackBlock("custom", "TNT", Arrays.asList("RESPAWN_ANCHOR")));
    }

    @Test
    public void targetNameNormalizationAcceptsCommonUserInputs() {
        assertEquals("ENDER_CRYSTAL", DamageRules.normalizeTargetName("minecraft:end_crystal"));
        assertEquals("ENDER_CRYSTAL", DamageRules.normalizeTargetName("EnderCrystal"));
        assertEquals("RESPAWN_ANCHOR", DamageRules.normalizeTargetName("minecraft:respawn-anchor"));
        assertEquals("RESPAWN_ANCHOR", DamageRules.normalizeTargetName("Respawn Anchor"));
    }

    @Test
    public void modeNormalizationAcceptsCommonSeparators() {
        assertEquals("crystal-anchor", DamageRules.normalizeModeName("CRYSTAL_ANCHOR"));
        assertEquals("pvp-evp", DamageRules.normalizeModeName("pvp evp"));
        assertTrue(DamageRules.isValidMode("custom"));
    }
}
