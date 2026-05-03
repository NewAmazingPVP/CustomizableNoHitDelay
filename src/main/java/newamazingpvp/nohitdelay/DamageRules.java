package newamazingpvp.nohitdelay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class DamageRules {
    public static final String MODE_PVP = "pvp";
    public static final String MODE_EVP = "evp";
    public static final String MODE_PVP_EVP = "pvp-evp";
    public static final String MODE_ANY = "any";
    public static final String MODE_PLAYER_ONLY = "player-only";
    public static final String MODE_CRYSTAL = "crystal";
    public static final String MODE_ANCHOR = "anchor";
    public static final String MODE_CRYSTAL_ANCHOR = "crystal-anchor";
    public static final String MODE_CUSTOM = "custom";

    private static final String ENDER_CRYSTAL = "ENDER_CRYSTAL";
    private static final String RESPAWN_ANCHOR = "RESPAWN_ANCHOR";
    private static final String ENTITY_EXPLOSION = "ENTITY_EXPLOSION";
    private static final String BLOCK_EXPLOSION = "BLOCK_EXPLOSION";

    private static final List<String> MODES = Collections.unmodifiableList(Arrays.asList(
            MODE_PVP,
            MODE_EVP,
            MODE_PVP_EVP,
            MODE_ANY,
            MODE_PLAYER_ONLY,
            MODE_CRYSTAL,
            MODE_ANCHOR,
            MODE_CRYSTAL_ANCHOR,
            MODE_CUSTOM
    ));

    private DamageRules() {
    }

    public static List<String> getModes() {
        return MODES;
    }

    public static String getModesText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MODES.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(MODES.get(i));
        }
        return builder.toString();
    }

    public static boolean isValidMode(String mode) {
        return MODES.contains(normalizeModeName(mode));
    }

    public static String normalizeModeName(String mode) {
        if (mode == null) {
            return MODE_ANY;
        }
        return mode.trim().toLowerCase(Locale.ROOT).replaceAll("[_\\s]+", "-");
    }

    public static boolean shouldApplyEntityDamage(
            String mode,
            boolean damagerPlayer,
            boolean damagerLiving,
            String damagerType,
            boolean targetPlayer,
            boolean targetLiving,
            String cause,
            Collection<String> customEntities,
            boolean customExplosionsOnly
    ) {
        if (!targetLiving) {
            return false;
        }

        String normalizedMode = normalizeModeName(mode);
        if (MODE_PVP.equals(normalizedMode)) {
            return damagerPlayer && targetPlayer;
        }
        if (MODE_EVP.equals(normalizedMode)) {
            return !damagerPlayer && targetPlayer;
        }
        if (MODE_PVP_EVP.equals(normalizedMode)) {
            return (damagerPlayer && targetLiving) || (targetPlayer && damagerLiving);
        }
        if (MODE_ANY.equals(normalizedMode)) {
            return true;
        }
        if (MODE_PLAYER_ONLY.equals(normalizedMode)) {
            return damagerPlayer;
        }
        if (MODE_CRYSTAL.equals(normalizedMode) || MODE_CRYSTAL_ANCHOR.equals(normalizedMode)) {
            return isEndCrystalExplosion(damagerType, cause);
        }
        if (MODE_CUSTOM.equals(normalizedMode)) {
            return matchesTargetName(damagerType, customEntities) && matchesExplosionPolicy(cause, customExplosionsOnly);
        }
        return false;
    }

    public static boolean shouldApplyBlockDamage(
            String mode,
            boolean targetLiving,
            String blockType,
            String cause,
            Collection<String> customBlocks,
            boolean customExplosionsOnly
    ) {
        if (!targetLiving) {
            return false;
        }

        String normalizedMode = normalizeModeName(mode);
        if (MODE_ANCHOR.equals(normalizedMode) || MODE_CRYSTAL_ANCHOR.equals(normalizedMode)) {
            return isRespawnAnchorExplosion(blockType, cause);
        }
        if (MODE_CUSTOM.equals(normalizedMode)) {
            return matchesTargetName(blockType, customBlocks) && matchesExplosionPolicy(cause, customExplosionsOnly);
        }
        return false;
    }

    public static boolean shouldTrackBlock(String mode, String blockType, Collection<String> customBlocks) {
        String normalizedMode = normalizeModeName(mode);
        if (MODE_ANCHOR.equals(normalizedMode) || MODE_CRYSTAL_ANCHOR.equals(normalizedMode)) {
            return RESPAWN_ANCHOR.equals(normalizeTargetName(blockType));
        }
        return MODE_CUSTOM.equals(normalizedMode) && matchesTargetName(blockType, customBlocks);
    }

    public static String normalizeTargetName(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        int namespaceIndex = normalized.indexOf(':');
        if (namespaceIndex >= 0 && namespaceIndex < normalized.length() - 1) {
            normalized = normalized.substring(namespaceIndex + 1);
        }

        normalized = normalized.replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+", "").replaceAll("_+$", "");

        if ("ENDERCRYSTAL".equals(normalized) || "ENDCRYSTAL".equals(normalized) || "END_CRYSTAL".equals(normalized)) {
            return ENDER_CRYSTAL;
        }
        if ("RESPAWNANCHOR".equals(normalized)) {
            return RESPAWN_ANCHOR;
        }
        return normalized;
    }

    private static boolean isEndCrystalExplosion(String entityType, String cause) {
        return ENDER_CRYSTAL.equals(normalizeTargetName(entityType)) && ENTITY_EXPLOSION.equals(normalizeTargetName(cause));
    }

    private static boolean isRespawnAnchorExplosion(String blockType, String cause) {
        return RESPAWN_ANCHOR.equals(normalizeTargetName(blockType)) && BLOCK_EXPLOSION.equals(normalizeTargetName(cause));
    }

    private static boolean matchesTargetName(String actualName, Collection<String> configuredNames) {
        String normalizedActual = normalizeTargetName(actualName);
        if (normalizedActual.length() == 0 || configuredNames == null) {
            return false;
        }

        for (String configuredName : configuredNames) {
            if (normalizedActual.equals(normalizeTargetName(configuredName))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesExplosionPolicy(String cause, boolean explosionsOnly) {
        if (!explosionsOnly) {
            return true;
        }
        String normalizedCause = normalizeTargetName(cause);
        return ENTITY_EXPLOSION.equals(normalizedCause) || BLOCK_EXPLOSION.equals(normalizedCause);
    }
}
