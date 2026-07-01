package io.github.pepe3012.scheduler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

/**
 * Server-side task scheduler.
 *
 * <p>Global tasks are ticked once per server tick. Level tasks are ticked once per
 * server world tick.</p>
 */
public final class ServerTaskScheduler extends AbstractTaskScheduler<MinecraftServer, ServerLevel> {
    ServerTaskScheduler() {
        //package-private
    }

    /**
     * Registers the scheduler into Fabric's server lifecycle tick events.
     */
    @Override
    public void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(this::tickGlobalTasks);
        ServerTickEvents.START_LEVEL_TICK.register(this::tickLevelTasks);
    }

    /**
     * Schedules a one-shot server task.
     *
     * @param action the task action
     * @param delayTicks the delay before execution, in server ticks
     * @return the scheduled task ID
     */
    public int schedule(Consumer<MinecraftServer> action, int delayTicks) {
        return this.scheduleTask(action, delayTicks);
    }

    /**
     * Schedules a repeating server task.
     *
     * @param action the task action
     * @param delayTicks the delay before the first execution, in server ticks
     * @param periodTicks the delay between executions, in server ticks
     * @return the scheduled task ID
     */
    public int scheduleRepeating(Consumer<MinecraftServer> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingTask(action, delayTicks, periodTicks);
    }

    /**
     * Schedules a one-shot server world task.
     *
     * @param action the task action
     * @param delayTicks the delay before execution, in world ticks
     * @return the scheduled task ID
     */
    public int scheduleLevel(Consumer<ServerLevel> action, int delayTicks) {
        return this.scheduleLevelTask(action, delayTicks);
    }

    /**
     * Schedules a repeating server world task.
     *
     * @param action the task action
     * @param delayTicks the delay before the first execution, in world ticks
     * @param periodTicks the delay between executions, in world ticks
     * @return the scheduled task ID
     */
    public int scheduleRepeatingLevel(Consumer<ServerLevel> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingLevelTask(action, delayTicks, periodTicks);
    }
}