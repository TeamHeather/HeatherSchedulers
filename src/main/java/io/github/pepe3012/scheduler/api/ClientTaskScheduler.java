package io.github.pepe3012.scheduler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.function.Consumer;

/**
 * Client-side task scheduler.
 *
 * <p>Global tasks are ticked once per client tick. Level tasks are ticked once per
 * client world tick.</p>
 */
@Environment(EnvType.CLIENT)
public final class ClientTaskScheduler extends AbstractTaskScheduler<Minecraft, ClientLevel> {
    ClientTaskScheduler() {
        //package-private
    }

    /**
     * Registers the scheduler into Fabric's client lifecycle tick events.
     */
    @Override
    public void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(this::tickGlobalTasks);
        ClientTickEvents.START_LEVEL_TICK.register(this::tickLevelTasks);
    }

    /**
     * Schedules a one-shot client task.
     *
     * @param action the task action
     * @param delayTicks the delay before execution, in client ticks
     * @return the scheduled task ID
     */
    public int schedule(Consumer<Minecraft> action, int delayTicks) {
        return this.scheduleTask(action, delayTicks);
    }

    /**
     * Schedules a repeating client task.
     *
     * @param action the task action
     * @param delayTicks the delay before the first execution, in client ticks
     * @param periodTicks the delay between executions, in client ticks
     * @return the scheduled task ID
     */
    public int scheduleRepeating(Consumer<Minecraft> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingTask(action, delayTicks, periodTicks);
    }

    /**
     * Schedules a one-shot client world task.
     *
     * @param action the task action
     * @param delayTicks the delay before execution, in world ticks
     * @return the scheduled task ID
     */
    public int scheduleLevel(Consumer<ClientLevel> action, int delayTicks) {
        return this.scheduleLevelTask(action, delayTicks);
    }

    /**
     * Schedules a repeating client world task.
     *
     * @param action the task action
     * @param delayTicks the delay before the first execution, in world ticks
     * @param periodTicks the delay between executions, in world ticks
     * @return the scheduled task ID
     */
    public int scheduleRepeatingLevel(Consumer<ClientLevel> action, int delayTicks, int periodTicks) {
        return this.scheduleRepeatingLevelTask(action, delayTicks, periodTicks);
    }
}