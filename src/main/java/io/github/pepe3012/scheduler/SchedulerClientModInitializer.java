package io.github.pepe3012.scheduler;

import io.github.pepe3012.scheduler.api.TaskSchedulers;
import net.fabricmc.api.ClientModInitializer;

public class SchedulerClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TaskSchedulers.CLIENT.initialize();
    }
}