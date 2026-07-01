package org.heather.scheduler.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.function.Consumer;

/**
 * Client-side scheduler implementation backed by Fabric client tick events.
 *
 * <p>This scheduler exposes the client-specific scheduling API for tasks that run on the Minecraft
 * client thread. Global tasks receive the active {@link Minecraft} instance, while level tasks
 * receive the current {@link ClientLevel} instance supplied by Fabric's client level tick event.</p>
 *
 * <p>The scheduler does not create threads, timers, or asynchronous workers. Task storage, delay
 * handling, repeat handling, cancellation, and task execution are provided by
 * {@link AbstractTaskScheduler}. This class is responsible only for registering the client tick
 * callbacks and exposing public methods with client-specific context types.</p>
 *
 * <p>All delays and periods are measured in client-side ticks. A task action is executed directly
 * from the Fabric tick callback that advances its task group.</p>
 */
@Environment(EnvType.CLIENT)
public final class ClientTaskScheduler extends AbstractTaskScheduler<Minecraft, ClientLevel> {
    /**
     * Creates a client task scheduler.
     *
     * <p>The constructor has package-private visibility so scheduler instances can be controlled by
     * the scheduler API package instead of being created freely by external code.</p>
     */
    ClientTaskScheduler() {
        // Package-private.
    }

    /**
     * Registers this scheduler with Fabric's client lifecycle tick events.
     *
     * <p>Global tasks are advanced during {@link ClientTickEvents#START_CLIENT_TICK}. Level tasks are
     * advanced during {@link ClientTickEvents#START_LEVEL_TICK}. Calling this method more than once
     * may register the same scheduler callbacks multiple times.</p>
     */
    @Override
    public void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(this::tickGlobalTasks);
        ClientTickEvents.START_LEVEL_TICK.register(this::tickLevelTasks);
    }

    /**
     * Schedules a one-shot client task.
     *
     * <p>The task receives the active {@link Minecraft} instance and is removed after its first
     * execution.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the delay before execution, measured in client ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    public int schedule(Consumer<Minecraft> action, int delayTicks) {
        return this.scheduleTask(action, delayTicks);
    }

    /**
     * Schedules a repeating client task.
     *
     * <p>The task receives the active {@link Minecraft} instance each time it runs and remains
     * registered until it is canceled through the scheduler API.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the delay before the first execution, measured in client ticks
     * @param periodTicks the delay between executions, measured in client ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    public int scheduleRepeating(Consumer<Minecraft> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingTask(action, delayTicks, periodTicks);
    }

    /**
     * Schedules a one-shot client level task.
     *
     * <p>The task receives the {@link ClientLevel} instance supplied by the client level tick event
     * and is removed after its first execution.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the delay before execution, measured in client level ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    public int scheduleLevel(Consumer<ClientLevel> action, int delayTicks) {
        return this.scheduleLevelTask(action, delayTicks);
    }

    /**
     * Schedules a repeating client level task.
     *
     * <p>The task receives the {@link ClientLevel} instance supplied by the client level tick event
     * each time it runs and remains registered until it is canceled through the scheduler API.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the delay before the first execution, measured in client level ticks
     * @param periodTicks the delay between executions, measured in client level ticks
     * @return the identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    public int scheduleRepeatingLevel(Consumer<ClientLevel> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingLevelTask(action, delayTicks, periodTicks);
    }
}