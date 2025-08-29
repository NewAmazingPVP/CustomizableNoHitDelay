package newamazingpvp.nohitdelay;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;

public final class SchedulerAdapter {
    private static final boolean FOLIA_AVAILABLE;

    static {
        boolean folia;
        try {
            Bukkit.class.getMethod("getGlobalRegionScheduler");
            folia = true;
        } catch (NoSuchMethodException e) {
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

        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(entity);

            Method runDelayed = null;
            for (Method m : scheduler.getClass().getMethods()) {
                if (!m.getName().equals("runDelayed")) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 3
                        && isPluginType(params[0])
                        && isConsumerType(params[1])
                        && (params[2] == long.class || params[2] == Long.class)) {
                    runDelayed = m;
                    break;
                }
            }

            if (runDelayed == null) throw new NoSuchMethodException("EntityScheduler#runDelayed not found");

            Consumer<Object> consumer = (ignored) -> task.run();
            runDelayed.invoke(scheduler, plugin, consumer, delay);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    private static boolean isPluginType(Class<?> c) {
        return c != null && (c.getName().equals("org.bukkit.plugin.Plugin") || Plugin.class.isAssignableFrom(c));
    }

    private static boolean isConsumerType(Class<?> c) {
        return c != null && (c.getName().equals("java.util.function.Consumer") || Consumer.class.isAssignableFrom(c));
    }
}
