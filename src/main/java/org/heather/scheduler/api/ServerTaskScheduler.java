package org.heather.scheduler.api;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

/**
 * Server-side scheduler implementation backed by Fabric server tick events.
 *
 * <p>This scheduler exposes the server-specific scheduling API for tasks that run from the server
 * tick lifecycle. Global tasks receive the active {@link MinecraftServer} instance, while level
 * tasks receive the {@link ServerLevel} instance supplied by Fabric's server level tick event.</p>
 *
 * <p>The scheduler does not create threads, timers, or asynchronous workers. Task storage, delay
 * handling, repeat handling, cancellation, and task execution are provided by
 * {@link AbstractTaskScheduler}. This class is responsible only for registering the server tick
 * callbacks and exposing public methods with server-specific context types.</p>
 *
 * <p>All delays and periods are measured in server-side ticks. A task action is executed directly
 * from the Fabric tick callback that advances its task group.</p>
 */
public final class ServerTaskScheduler extends AbstractTaskScheduler<MinecraftServer, ServerLevel> {
    /**
     * Creates a server task scheduler.
     *
     * <p>The constructor has package-private visibility so scheduler instances can be controlled by
     * the scheduler API package instead of being created freely by external code.</p>
     */
    ServerTaskScheduler() {
        // Package-private.
    }

    /**
     * Registers this scheduler with Fabric's server lifecycle tick events.
     *
     * <p>Global tasks are advanced during {@link ServerTickEvents#START_SERVER_TICK}. Level tasks are
     * advanced during {@link ServerTickEvents#START_LEVEL_TICK}. Calling this method more than once
     * may register the same scheduler callbacks multiple times.</p>
     */
    @Override
    public void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(this::tickGlobalTasks);
        ServerTickEvents.START_LEVEL_TICK.register(this::tickLevelTasks);
    }

    /**
     * Schedules a one-shot server task.
     *
     * <p>The task receives the active {@link MinecraftServer} instance and is removed after its first
     * execution.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the delay before execution, measured in server ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    public int schedule(Consumer<MinecraftServer> action, int delayTicks) {
        return this.scheduleTask(action, delayTicks);
    }

    /**
     * Schedules a repeating server task.
     *
     * <p>The task receives the active {@link MinecraftServer} instance each time it runs and remains
     * registered until it is canceled through the scheduler API.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the delay before the first execution, measured in server ticks
     * @param periodTicks the delay between executions, measured in server ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    public int scheduleRepeating(Consumer<MinecraftServer> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingTask(action, delayTicks, periodTicks);
    }

    /**
     * Schedules a one-shot server level task.
     *
     * <p>The task receives the {@link ServerLevel} instance supplied by the server level tick event
     * and is removed after its first execution.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the delay before execution, measured in server level ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    public int scheduleLevel(Consumer<ServerLevel> action, int delayTicks) {
        return this.scheduleLevelTask(action, delayTicks);
    }

    /**
     * Schedules a repeating server level task.
     *
     * <p>The task receives the {@link ServerLevel} instance supplied by the server level tick event
     * each time it runs and remains registered until it is canceled through the scheduler API.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the delay before the first execution, measured in server level ticks
     * @param periodTicks the delay between executions, measured in server level ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    public int scheduleRepeatingLevel(Consumer<ServerLevel> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingLevelTask(action, delayTicks, periodTicks);
    }
}