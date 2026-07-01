package org.heather.scheduler.internal;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.heather.scheduler.api.HeatherSchedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for the Heather scheduler mod.
 *
 * <p>This initializer connects the shared server scheduler instance to Fabric's server lifecycle.
 * During mod initialization, it initializes {@link HeatherSchedulers#SERVER}, allowing server and
 * server-level scheduled tasks to begin receiving Fabric tick callbacks.</p>
 *
 * <p>When the Minecraft server is stopping, all registered server-side tasks are canceled. This
 * clears scheduler state at the end of the server lifecycle and prevents stale tasks from being kept
 * across server shutdown.</p>
 */
public final class SchedulerModInitializer implements ModInitializer {
    public static final String MOD_ID = "scheduler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Initializes the server-side scheduler lifecycle.
     *
     * <p>This method is called by Fabric during mod initialization. It initializes the shared server
     * scheduler and registers a shutdown callback that clears all server tasks when the server stops.</p>
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Scheduler server task scheduler.");

        HeatherSchedulers.SERVER.initialize();

        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> {
            HeatherSchedulers.SERVER.cancelAllTasks();
            LOGGER.info("Canceled all Scheduler server tasks.");
        });

        LOGGER.info("Scheduler server task scheduler initialized.");
    }
}