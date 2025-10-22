package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class SchedulerAdapter {
    private static final boolean FOLIA_AVAILABLE;
    private static final AtomicBoolean WARNED_FOLIA_FAILURE = new AtomicBoolean(false);

    static {
        boolean folia;
        try {
            Bukkit.class.getMethod("getGlobalRegionScheduler");
            folia = true;
        } catch (NoSuchMethodException ignored) {
            folia = false;
        }
        FOLIA_AVAILABLE = folia;
    }

    private SchedulerAdapter() {
    }

    public static boolean isFolia() {
        return FOLIA_AVAILABLE;
    }

    public static void runEntityLater(Plugin plugin, Entity entity, long delayTicks, Runnable task) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");

        long delay = Math.max(0L, delayTicks);

        if (!FOLIA_AVAILABLE) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            return;
        }

        if (delay <= 0L) {
            deliverToEntity(plugin, entity, task);
            return;
        }

        if (tryEntityDelayed(plugin, entity, delay, task)) {
            return;
        }

        if (scheduleDelayThenPostToEntity(plugin, entity, delay, task)) {
            return;
        }

        deliverToEntity(plugin, entity, task);
    }

    private static boolean tryEntityDelayed(Plugin plugin, Entity entity, long delay, Runnable task) {
        try {
            Object scheduler = getEntityScheduler(entity);

            Method runDelayedConsumerRetired = findMethod(scheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class});
            if (runDelayedConsumerRetired != null) {
                Consumer<Object> consumer = ignored -> task.run();
                runDelayedConsumerRetired.invoke(scheduler, plugin, consumer, null, delay);
                return true;
            }

            Method runDelayedConsumer = findMethod(scheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class});
            if (runDelayedConsumer != null) {
                Consumer<Object> consumer = ignored -> task.run();
                runDelayedConsumer.invoke(scheduler, plugin, consumer, delay);
                return true;
            }

            Method runDelayedRunnableRetired = findMethod(scheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Runnable.class, Runnable.class, long.class});
            if (runDelayedRunnableRetired != null) {
                runDelayedRunnableRetired.invoke(scheduler, plugin, task, null, delay);
                return true;
            }

            Method runDelayedRunnable = findMethod(scheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Runnable.class, long.class});
            if (runDelayedRunnable != null) {
                runDelayedRunnable.invoke(scheduler, plugin, task, delay);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean scheduleDelayThenPostToEntity(Plugin plugin, Entity entity, long delay, Runnable task) {
        try {
            Object globalScheduler = getGlobalRegionScheduler();
            if (globalScheduler != null) {
                Consumer<Object> consumer = ignored -> deliverToEntity(plugin, entity, task);
                Method runDelayedConsumer = findMethod(globalScheduler.getClass(), "runDelayed",
                        new Class<?>[]{Plugin.class, Consumer.class, long.class});
                if (runDelayedConsumer != null) {
                    runDelayedConsumer.invoke(globalScheduler, plugin, consumer, delay);
                    return true;
                }

                Method runDelayedRunnable = findMethod(globalScheduler.getClass(), "runDelayed",
                        new Class<?>[]{Plugin.class, Runnable.class, long.class});
                if (runDelayedRunnable != null) {
                    Runnable wrapped = () -> deliverToEntity(plugin, entity, task);
                    runDelayedRunnable.invoke(globalScheduler, plugin, wrapped, delay);
                    return true;
                }
            }

            Object regionScheduler = getRegionScheduler();
            if (regionScheduler != null) {
                World world = entity.getWorld();
                if (world == null) {
                    return false;
                }
                int chunkX = entity.getLocation().getBlockX() >> 4;
                int chunkZ = entity.getLocation().getBlockZ() >> 4;

                Consumer<Object> consumer = ignored -> deliverToEntity(plugin, entity, task);
                Method runDelayedConsumer = findMethod(regionScheduler.getClass(), "runDelayed",
                        new Class<?>[]{Plugin.class, World.class, int.class, int.class, Consumer.class, long.class});
                if (runDelayedConsumer != null) {
                    runDelayedConsumer.invoke(regionScheduler, plugin, world, chunkX, chunkZ, consumer, delay);
                    return true;
                }

                Method runDelayedRunnable = findMethod(regionScheduler.getClass(), "runDelayed",
                        new Class<?>[]{Plugin.class, World.class, int.class, int.class, Runnable.class, long.class});
                if (runDelayedRunnable != null) {
                    Runnable wrapped = () -> deliverToEntity(plugin, entity, task);
                    runDelayedRunnable.invoke(regionScheduler, plugin, world, chunkX, chunkZ, wrapped, delay);
                    return true;
                }

                Method execute = findMethod(regionScheduler.getClass(), "execute",
                        new Class<?>[]{Plugin.class, World.class, int.class, int.class, Runnable.class});
                if (execute != null) {
                    Runnable wrapped = () -> deliverToEntity(plugin, entity, task);
                    Method runDelayedConsumerGlobal = null;
                    Object global = getGlobalRegionScheduler();
                    if (global != null) {
                        runDelayedConsumerGlobal = findMethod(global.getClass(), "runDelayed",
                                new Class<?>[]{Plugin.class, Consumer.class, long.class});
                        if (runDelayedConsumerGlobal != null) {
                            Consumer<Object> execConsumer = ignored -> executeInCurrentRegion(plugin, entity, execute, wrapped);
                            runDelayedConsumerGlobal.invoke(global, plugin, execConsumer, delay);
                            return true;
                        }

                        Method runDelayedRunnableGlobal = findMethod(global.getClass(), "runDelayed",
                                new Class<?>[]{Plugin.class, Runnable.class, long.class});
                        if (runDelayedRunnableGlobal != null) {
                            Runnable execRunnable = () -> executeInCurrentRegion(plugin, entity, execute, wrapped);
                            runDelayedRunnableGlobal.invoke(global, plugin, execRunnable, delay);
                            return true;
                        }
                    }

                    if (scheduleAsyncDelay(plugin, delay, () -> executeInCurrentRegion(plugin, entity, execute, wrapped))) {
                        return true;
                    }
                }
            } else {
                return scheduleAsyncDelay(plugin, delay, () -> deliverToEntity(plugin, entity, task));
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void executeInCurrentRegion(Plugin plugin, Entity entity, Method execute, Runnable task) {
        try {
            Object regionScheduler = getRegionScheduler();
            if (regionScheduler == null) {
                deliverToEntity(plugin, entity, task);
                return;
            }
            World world = entity.getWorld();
            if (world == null) {
                return;
            }
            int chunkX = entity.getLocation().getBlockX() >> 4;
            int chunkZ = entity.getLocation().getBlockZ() >> 4;
            execute.invoke(regionScheduler, plugin, world, chunkX, chunkZ, task);
        } catch (Throwable ignored) {
        }
    }

    private static boolean scheduleAsyncDelay(Plugin plugin, long delay, Runnable task) {
        try {
            Method getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
            Object asyncScheduler = getAsyncScheduler.invoke(null);
            Method runDelayedConsumer = findMethod(asyncScheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class});
            if (runDelayedConsumer != null) {
                runDelayedConsumer.invoke(asyncScheduler, plugin, (Consumer<Object>) ignored -> task.run(), delay);
                return true;
            }

            Method runDelayedRunnable = findMethod(asyncScheduler.getClass(), "runDelayed",
                    new Class<?>[]{Plugin.class, Runnable.class, long.class});
            if (runDelayedRunnable != null) {
                runDelayedRunnable.invoke(asyncScheduler, plugin, task, delay);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean safePostToEntity(Plugin plugin, Entity entity, Runnable task) {
        try {
            Object scheduler = getEntityScheduler(entity);

            Method runConsumerRetired = findMethod(scheduler.getClass(), "run",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class});
            if (runConsumerRetired != null) {
                runConsumerRetired.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run(), null);
                return true;
            }

            Method runConsumer = findMethod(scheduler.getClass(), "run",
                    new Class<?>[]{Plugin.class, Consumer.class});
            if (runConsumer != null) {
                runConsumer.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run());
                return true;
            }

            Method runRunnableRetired = findMethod(scheduler.getClass(), "run",
                    new Class<?>[]{Plugin.class, Runnable.class, Runnable.class});
            if (runRunnableRetired != null) {
                runRunnableRetired.invoke(scheduler, plugin, task, null);
                return true;
            }

            Method runRunnable = findMethod(scheduler.getClass(), "run",
                    new Class<?>[]{Plugin.class, Runnable.class});
            if (runRunnable != null) {
                runRunnable.invoke(scheduler, plugin, task);
                return true;
            }

            Method executeRetired = findMethod(scheduler.getClass(), "execute",
                    new Class<?>[]{Plugin.class, Runnable.class, Runnable.class, long.class});
            if (executeRetired != null) {
                executeRetired.invoke(scheduler, plugin, task, null, 1L);
                return true;
            }

            Method execute = findMethod(scheduler.getClass(), "execute",
                    new Class<?>[]{Plugin.class, Runnable.class});
            if (execute != null) {
                execute.invoke(scheduler, plugin, task);
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static Object getEntityScheduler(Entity entity) throws Exception {
        Method getScheduler = entity.getClass().getMethod("getScheduler");
        return getScheduler.invoke(entity);
    }

    private static Object getGlobalRegionScheduler() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
            return method.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Object getRegionScheduler() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getRegionScheduler");
            return method.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>[] desired) {
        for (Method method : owner.getMethods()) {
            if (!method.getName().equals(name)) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != desired.length) continue;
            boolean match = true;
            for (int i = 0; i < params.length; i++) {
                if (!isCompatible(params[i], desired[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                try {
                    method.setAccessible(true);
                } catch (Throwable ignored) {
                }
                return method;
            }
        }
        return null;
    }

    private static boolean isCompatible(Class<?> have, Class<?> want) {
        if (want == Plugin.class) {
            return isPluginType(have);
        }
        if (want == Consumer.class) {
            return isConsumerType(have);
        }
        if (want == Runnable.class) {
            return Runnable.class.isAssignableFrom(have);
        }
        if (want == long.class) {
            return have == long.class || have == Long.class || have == Long.TYPE;
        }
        if (want == int.class) {
            return have == int.class || have == Integer.class || have == Integer.TYPE;
        }
        if (want == World.class) {
            return have != null && (have == World.class || World.class.isAssignableFrom(have));
        }
        return want.isAssignableFrom(have);
    }

    private static boolean isPluginType(Class<?> c) {
        return c != null && (c.getName().equals("org.bukkit.plugin.Plugin") || Plugin.class.isAssignableFrom(c));
    }

    private static boolean isConsumerType(Class<?> c) {
        return c != null && (c.getName().equals("java.util.function.Consumer") || Consumer.class.isAssignableFrom(c));
    }

    private static void deliverToEntity(Plugin plugin, Entity entity, Runnable task) {
        if (!safePostToEntity(plugin, entity, task)) {
            warnOnce(plugin, null);
            task.run();
        }
    }

    private static void warnOnce(Plugin plugin, Throwable cause) {
        if (!WARNED_FOLIA_FAILURE.compareAndSet(false, true)) {
            return;
        }
        String message = "NoHitDelay could not schedule a delayed task on Folia; executing immediately as a fallback. Please report this.";
        if (cause != null) {
            plugin.getLogger().warning(message + " Reason: " + cause.getMessage());
        } else {
            plugin.getLogger().warning(message);
        }
    }
}
