package io.github.pepe3012.scheduler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Static access point for the task scheduler instances.
 */
public final class TaskSchedulers {
    public static final ServerTaskScheduler SERVER = new ServerTaskScheduler();

    @Environment(EnvType.CLIENT)
    public static final ClientTaskScheduler CLIENT = new ClientTaskScheduler();

    private TaskSchedulers() {
    }
}