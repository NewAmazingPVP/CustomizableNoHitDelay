package newamazingpvp.nohitdelay;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Events implements Listener {
    private static final long TRACKED_BLOCK_TTL_MILLIS = 3000L;
    private static final double TRACKED_BLOCK_RADIUS_SQUARED = 144.0D;

    private final NoHitDelay plugin;
    private final Config config;
    private final Map<BlockKey, TrackedBlock> recentTargetBlocks = new ConcurrentHashMap<BlockKey, TrackedBlock>();

    public Events(NoHitDelay plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        String blockType = getBlockTypeName(clickedBlock);
        if (DamageRules.shouldTrackBlock(getMode(), blockType, getCustomBlocks())) {
            pruneTrackedBlocks(System.currentTimeMillis());
            recentTargetBlocks.put(BlockKey.from(clickedBlock), TrackedBlock.from(clickedBlock, blockType));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (DamageRules.shouldApplyEntityDamage(
                getMode(),
                damager instanceof Player,
                damager instanceof LivingEntity,
                getEntityTypeName(damager),
                entity instanceof Player,
                entity instanceof LivingEntity,
                event.getCause().name(),
                getCustomEntities(),
                isCustomExplosionsOnly()
        )) {
            resetNoDamageTicks((LivingEntity) entity, config.getConfig().getLong("delay"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockDamage(EntityDamageByBlockEvent event) {
        applyBlockDamage(event, getDamageBlockName(event));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onGenericDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent || event instanceof EntityDamageByBlockEvent) {
            return;
        }
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        applyBlockDamage(event, findRecentTargetBlockType(event.getEntity().getLocation()));
    }

    private void applyBlockDamage(EntityDamageEvent event, String blockType) {
        Entity entity = event.getEntity();
        if (blockType == null) {
            blockType = findRecentTargetBlockType(entity.getLocation());
        }

        if (DamageRules.shouldApplyBlockDamage(
                getMode(),
                entity instanceof LivingEntity,
                blockType,
                event.getCause().name(),
                getCustomBlocks(),
                isCustomExplosionsOnly()
        )) {
            resetNoDamageTicks((LivingEntity) entity, config.getConfig().getLong("delay"));
        }
    }

    private void resetNoDamageTicks(LivingEntity entity, long hitDelay) {
        double multiplier = config.getConfig().getDouble("knockback-multiplier");
        if (Math.abs(multiplier - 1.0) > 0.0001) {
            SchedulerAdapter.runEntityLater(plugin, entity, 1, () ->
                    entity.setVelocity(entity.getVelocity().multiply(multiplier))
            );
        }
        final int safeHitDelay = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, hitDelay));
        SchedulerAdapter.runEntityLater(plugin, entity, 1, () -> entity.setNoDamageTicks(safeHitDelay));
    }

    private String getMode() {
        String mode = config.getConfig().getString("mode");
        return mode == null ? DamageRules.MODE_ANY : mode;
    }

    private List<String> getCustomEntities() {
        return config.getConfig().getStringList("custom-targets.entities");
    }

    private List<String> getCustomBlocks() {
        return config.getConfig().getStringList("custom-targets.blocks");
    }

    private boolean isCustomExplosionsOnly() {
        return config.getConfig().getBoolean("custom-targets.explosions-only", false);
    }

    private String getEntityTypeName(Entity entity) {
        if (entity == null || entity.getType() == null) {
            return null;
        }
        return entity.getType().name();
    }

    private String getBlockTypeName(Block block) {
        if (block == null || block.getType() == null) {
            return null;
        }
        return block.getType().name();
    }

    private String getBlockStateTypeName(BlockState blockState) {
        if (blockState == null || blockState.getType() == null) {
            return null;
        }
        return blockState.getType().name();
    }

    private String getDamageBlockName(EntityDamageByBlockEvent event) {
        String stateType = getDamagerBlockStateName(event);
        if (stateType != null && !"AIR".equals(DamageRules.normalizeTargetName(stateType))) {
            return stateType;
        }
        return getBlockTypeName(event.getDamager());
    }

    private String getDamagerBlockStateName(EntityDamageByBlockEvent event) {
        try {
            Method method = event.getClass().getMethod("getDamagerBlockState");
            Object result = method.invoke(event);
            if (result instanceof BlockState) {
                return getBlockStateTypeName((BlockState) result);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private String findRecentTargetBlockType(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        pruneTrackedBlocks(now);

        String mode = getMode();
        List<String> customBlocks = getCustomBlocks();
        String bestType = null;
        double bestDistance = Double.MAX_VALUE;

        for (TrackedBlock trackedBlock : recentTargetBlocks.values()) {
            if (!trackedBlock.sameWorld(location.getWorld().getUID())) {
                continue;
            }
            if (!DamageRules.shouldTrackBlock(mode, trackedBlock.blockType, customBlocks)) {
                continue;
            }

            double distance = trackedBlock.distanceSquared(location);
            if (distance <= TRACKED_BLOCK_RADIUS_SQUARED && distance < bestDistance) {
                bestDistance = distance;
                bestType = trackedBlock.blockType;
            }
        }

        return bestType;
    }

    private void pruneTrackedBlocks(long now) {
        for (Map.Entry<BlockKey, TrackedBlock> entry : recentTargetBlocks.entrySet()) {
            if (now - entry.getValue().trackedAtMillis > TRACKED_BLOCK_TTL_MILLIS) {
                recentTargetBlocks.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private static final class BlockKey {
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        private BlockKey(UUID worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BlockKey)) {
                return false;
            }
            BlockKey blockKey = (BlockKey) other;
            return x == blockKey.x
                    && y == blockKey.y
                    && z == blockKey.z
                    && worldId.equals(blockKey.worldId);
        }

        @Override
        public int hashCode() {
            int result = worldId.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    private static final class TrackedBlock {
        private final String blockType;
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;
        private final long trackedAtMillis;

        private TrackedBlock(String blockType, UUID worldId, int x, int y, int z, long trackedAtMillis) {
            this.blockType = blockType;
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.trackedAtMillis = trackedAtMillis;
        }

        private static TrackedBlock from(Block block, String blockType) {
            return new TrackedBlock(blockType, block.getWorld().getUID(), block.getX(), block.getY(), block.getZ(), System.currentTimeMillis());
        }

        private boolean sameWorld(UUID otherWorldId) {
            return worldId.equals(otherWorldId);
        }

        private double distanceSquared(Location location) {
            double dx = (x + 0.5D) - location.getX();
            double dy = (y + 0.5D) - location.getY();
            double dz = (z + 0.5D) - location.getZ();
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
