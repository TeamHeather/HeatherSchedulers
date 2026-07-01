package io.github.pepe3012.scheduler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Base scheduler for delayed and repeating tasks that are executed from a tick loop.
 *
 * <p>This scheduler manages two independent task groups:</p>
 *
 * <ul>
 *     <li>Global tasks, executed with a shared global context.</li>
 *     <li>Level tasks, executed with a level-specific context.</li>
 * </ul>
 *
 * <p>This class does not create or manage its own thread. Tasks only run when
 * {@link #tickGlobalTasks(Object)} or {@link #tickLevelTasks(Object)} is called by a
 * platform-specific implementation.</p>
 *
 * <p>All delays and periods are measured in scheduler ticks. A delay of {@code 0}
 * runs on the next scheduler tick. A repeating period of {@code 1} runs every tick.</p>
 *
 * @param <G> the global context type used by global tasks
 * @param <L> the level context type used by level tasks
 */
public abstract class AbstractTaskScheduler<G, L> {
    private final AtomicInteger nextTaskId = new AtomicInteger(1);
    private final Map<Integer, ScheduledTask<G>> globalTasks = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledTask<L>> levelTasks = new ConcurrentHashMap<>();

    /**
     * Creates a scheduler instance.
     *
     * <p>The constructor is protected so only subclasses can create scheduler instances.</p>
     */
    protected AbstractTaskScheduler() {
    }

    /**
     * Initializes the scheduler implementation.
     *
     * <p>Implementations should use this method to register their platform-specific tick callbacks.</p>
     */
    public abstract void initialize();

    /**
     * Ticks all global tasks once.
     *
     * <p>Completed one-shot tasks are removed automatically. Repeating tasks stay registered
     * until cancelled.</p>
     *
     * @param context the global context passed to each global task
     */
    protected void tickGlobalTasks(G context) {
        this.globalTasks.values().removeIf(task -> task.tick(context));
    }

    /**
     * Ticks all level tasks once.
     *
     * <p>Completed one-shot tasks are removed automatically. Repeating tasks stay registered
     * until cancelled.</p>
     *
     * @param level the level context passed to each level task
     */
    protected void tickLevelTasks(L level) {
        this.levelTasks.values().removeIf(task -> task.tick(level));
    }

    /**
     * Cancels a task by ID.
     *
     * <p>The ID may belong to either a global task or a level task.</p>
     *
     * @param taskId the task ID returned when the task was scheduled
     * @return {@code true} if a task was cancelled, otherwise {@code false}
     */
    public boolean cancelTask(int taskId) {
        return this.globalTasks.remove(taskId) != null || this.levelTasks.remove(taskId) != null;
    }

    /**
     * Cancels every registered global and level task.
     */
    public void cancelAllTasks() {
        this.globalTasks.clear();
        this.levelTasks.clear();
    }

    /**
     * Returns the amount of currently registered global tasks.
     *
     * @return the global task count
     */
    public int getGlobalTaskCount() {
        return this.globalTasks.size();
    }

    /**
     * Returns the amount of currently registered level tasks.
     *
     * @return the level task count
     */
    public int getLevelTaskCount() {
        return this.levelTasks.size();
    }

    /**
     * Schedules a one-shot global task.
     *
     * @param action the action to run
     * @param delayTicks the amount of scheduler ticks to wait before running the task
     * @return the scheduled task ID
     */
    protected int scheduleTask(Consumer<G> action, int delayTicks) {
        return this.registerGlobalTask(action, delayTicks, 0, false);
    }

    /**
     * Schedules a repeating global task.
     *
     * @param action the action to run
     * @param delayTicks the amount of scheduler ticks to wait before the first execution
     * @param periodTicks the amount of scheduler ticks between executions
     * @return the scheduled task ID
     */
    protected int scheduleRepeatingTask(Consumer<G> action, int delayTicks, int periodTicks) {
        return this.registerGlobalTask(action, delayTicks, periodTicks, true);
    }

    /**
     * Schedules a one-shot level task.
     *
     * @param action the action to run
     * @param delayTicks the amount of scheduler ticks to wait before running the task
     * @return the scheduled task ID
     */
    protected int scheduleLevelTask(Consumer<L> action, int delayTicks) {
        return this.registerLevelTask(action, delayTicks, 0, false);
    }

    /**
     * Schedules a repeating level task.
     *
     * @param action the action to run
     * @param delayTicks the amount of scheduler ticks to wait before the first execution
     * @param periodTicks the amount of scheduler ticks between executions
     * @return the scheduled task ID
     */
    protected int scheduleRepeatingLevelTask(Consumer<L> action, int delayTicks, int periodTicks) {
        return this.registerLevelTask(action, delayTicks, periodTicks, true);
    }

    private int registerGlobalTask(Consumer<G> action, int delayTicks, int periodTicks, boolean repeating) {
        int taskId = this.nextTaskId.getAndIncrement();
        this.globalTasks.put(taskId, new ScheduledTask<>(action, delayTicks, periodTicks, repeating));
        return taskId;
    }

    private int registerLevelTask(Consumer<L> action, int delayTicks, int periodTicks, boolean repeating) {
        int taskId = this.nextTaskId.getAndIncrement();
        this.levelTasks.put(taskId, new ScheduledTask<>(action, delayTicks, periodTicks, repeating));
        return taskId;
    }

    /**
     * Internal scheduled task wrapper.
     *
     * @param <C> the context type accepted by the scheduled action
     */
    private static final class ScheduledTask<C> {
        private final Consumer<C> action;
        private final int periodTicks;
        private final boolean repeating;
        private int ticksUntilNextRun;

        private ScheduledTask(Consumer<C> action, int delayTicks, int periodTicks, boolean repeating) {
            if (delayTicks < 0) {
                throw new IllegalArgumentException("delayTicks cannot be negative");
            }

            if (repeating && periodTicks <= 0) {
                throw new IllegalArgumentException("periodTicks must be greater than zero for repeating tasks");
            }

            this.action = Objects.requireNonNull(action, "action");
            this.ticksUntilNextRun = delayTicks;
            this.periodTicks = periodTicks;
            this.repeating = repeating;
        }

        /**
         * Ticks this task once.
         *
         * @param context the context passed to the scheduled action
         * @return {@code true} if this task is complete and should be removed
         */
        private boolean tick(C context) {
            if (this.ticksUntilNextRun > 0 && --this.ticksUntilNextRun > 0) {
                return false;
            }

            this.action.accept(context);

            if (!this.repeating) {
                return true;
            }

            this.ticksUntilNextRun = this.periodTicks;
            return false;
        }
    }
}