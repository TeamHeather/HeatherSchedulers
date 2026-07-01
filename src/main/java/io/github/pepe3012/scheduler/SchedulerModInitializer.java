package io.github.pepe3012.scheduler;

import io.github.pepe3012.scheduler.api.TaskSchedulers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchedulerMod implements ModInitializer {
    public static final String MOD_ID = "scheduler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Scheduler server task scheduler.");
        TaskSchedulers.SERVER.initialize();
        LOGGER.info("Scheduler server task scheduler initialized.");
    }
}