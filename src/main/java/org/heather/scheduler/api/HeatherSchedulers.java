package org.heather.scheduler.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Static access point for the scheduler instances exposed by the scheduler API.
 *
 * <p>This class owns the shared server and client scheduler instances used by the rest of the
 * project. Platform entrypoint can initialize these instances during their own lifecycle setup, and
 * other code can use them to schedule delayed or repeating tasks without creating scheduler objects
 * directly.</p>
 *
 * <p>The client scheduler is marked with {@link Environment} because it depends on client-only
 * Minecraft classes through {@link ClientTaskScheduler}. Server-side code should only use
 * {@link #SERVER}.</p>
 */
public final class HeatherSchedulers {
    /**
     * Shared scheduler instance for server-side tasks.
     */
    public static final ServerTaskScheduler SERVER = new ServerTaskScheduler();

    /**
     * Shared scheduler instance for client-side tasks.
     */
    @Environment(EnvType.CLIENT)
    public static final ClientTaskScheduler CLIENT = new ClientTaskScheduler();

    /**
     * Prevents utility-class instantiation.
     */
    private HeatherSchedulers() {
    }
}