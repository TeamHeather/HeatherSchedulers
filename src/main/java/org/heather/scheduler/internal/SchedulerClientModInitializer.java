package org.heather.scheduler.internal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.heather.scheduler.api.HeatherSchedulers;

/**
 * Client entrypoint for the Heather scheduler mod.
 *
 * <p>This initializer connects the shared client scheduler instance to Fabric's client lifecycle.
 * During client initialization, it initializes {@link HeatherSchedulers#CLIENT}, allowing client and
 * client-level scheduled tasks to begin receiving Fabric tick callbacks.</p>
 *
 * <p>When the Minecraft client is stopping, all registered client-side tasks are canceled. This
 * prevents scheduled actions from remaining stored after the client lifecycle has ended.</p>
 */
public final class SchedulerClientModInitializer implements ClientModInitializer {

    /**
     * Initializes the client-side scheduler lifecycle.
     *
     * <p>This method is called by Fabric on the physical client. It initializes the shared client
     * scheduler and registers a shutdown callback that clears all client tasks when the client stops.</p>
     */
    @Override
    public void onInitializeClient() {
        SchedulerModInitializer.LOGGER.info("Initializing Scheduler client task scheduler.");

        HeatherSchedulers.CLIENT.initialize();

        ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> {
            HeatherSchedulers.CLIENT.cancelAllTasks();
            SchedulerModInitializer.LOGGER.info("Canceled all Scheduler client tasks.");
        });

        SchedulerModInitializer.LOGGER.info("Scheduler client task scheduler initialized.");
    }
}