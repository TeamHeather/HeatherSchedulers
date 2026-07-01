package org.heather.scheduler.api;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Base implementation for schedulers that execute delayed and repeating tasks from a tick loop.
 *
 * <p>The scheduler separates tasks into two independent groups:</p>
 *
 * <ul>
 *     <li><strong>Global tasks</strong>, which receive a shared scheduler context of type {@code G}.</li>
 *     <li><strong>Level tasks</strong>, which receive a level-specific context of type {@code L}.</li>
 * </ul>
 *
 * <p>This class does not create threads, timers, or asynchronous workers. Tasks only advance when
 * {@link #tickGlobalTasks(Object)} or {@link #tickLevelTasks(Object)} is called by a concrete
 * scheduler implementation. In this project, the client and server scheduler implementations connect
 * those methods to Fabric tick events.</p>
 *
 * <p>Delays and periods are measured in scheduler ticks. A delay of {@code 0} or {@code 1} causes
 * the task to run the next time the matching task group is ticked. Larger delay values wait until
 * the counter reaches zero through subsequent tick calls. A repeating period of {@code 1} runs every
 * matching scheduler tick.</p>
 *
 * <p>Scheduled actions are executed directly inside the tick callback. Exceptions thrown by a task
 * are not caught by this class and will propagate to the caller that is ticking the scheduler.</p>
 *
 * @param <G> the context type passed to global tasks
 * @param <L> the context type passed to level-specific tasks
 */
public abstract class AbstractTaskScheduler<G, L> {
    /**
     * Generates unique task identifiers shared by both global and level task groups.
     */
    private final AtomicInteger nextTaskId = new AtomicInteger(1);

    /**
     * Active tasks that are ticked with the global scheduler context.
     */
    private final Map<Integer, ScheduledTask<G>> globalTasks = new ConcurrentHashMap<>();

    /**
     * Active tasks that are ticked with the level-specific scheduler context.
     */
    private final Map<Integer, ScheduledTask<L>> levelTasks = new ConcurrentHashMap<>();

    /**
     * Creates a scheduler base instance.
     *
     * <p>The constructor is protected because this class only provides shared scheduling behavior.
     * Concrete platform schedulers are responsible for exposing the public scheduling API and
     * registering the appropriate tick callbacks.</p>
     */
    protected AbstractTaskScheduler() {
    }

    /**
     * Initializes the concrete scheduler.
     *
     * <p>Implementations should register their platform-specific lifecycle or tick callbacks from
     * this method. Calling this method more than once may register duplicate callbacks unless the
     * implementation prevents it.</p>
     */
    public abstract void initialize();

    /**
     * Advances all currently registered global tasks by one scheduler tick.
     *
     * <p>Each task receives the supplied global context. One-shot tasks are removed after they run.
     Repeating tasks remain registered until canceled through {@link #cancelTask(int)} or
     * {@link #cancelAllTasks()}.</p>
     *
     * @param context the global context passed to each global task
     */
    protected void tickGlobalTasks(G context) {
        this.globalTasks.values().removeIf(task -> task.tick(context));
    }

    /**
     * Advances all currently registered level tasks by one scheduler tick.
     *
     * <p>Each task receives the supplied level context. One-shot tasks are removed after they run.
     * Repeating tasks remain registered until canceled through {@link #cancelTask(int)} or
     * {@link #cancelAllTasks()}.</p>
     *
     * @param level the level context passed to each level task
     */
    protected void tickLevelTasks(L level) {
        this.levelTasks.values().removeIf(task -> task.tick(level));
    }

    /**
     * Cancels a scheduled task by its identifier.
     *
     * <p>The identifier may belong to either the global task group or the level task group. If both
     * groups somehow contain the same identifier, the global task is removed first and the level task
     * is not checked because of short-circuit evaluation.</p>
     *
     * @param taskId the identifier returned when the task was scheduled
     * @return {@code true} if a task was removed; {@code false} if no task with that identifier was registered
     */
    public boolean cancelTask(int taskId) {
        return this.globalTasks.remove(taskId) != null || this.levelTasks.remove(taskId) != null;
    }

    /**
     * Cancels every registered task in both task groups.
     *
     * <p>This method clears global and level tasks immediately. It does not reset the task identifier
     * counter, so newly scheduled tasks continue using later identifiers.</p>
     */
    public void cancelAllTasks() {
        this.globalTasks.clear();
        this.levelTasks.clear();
    }

    /**
     * Returns the number of global tasks currently registered.
     *
     * @return the current global task count
     */
    public int getGlobalTaskCount() {
        return this.globalTasks.size();
    }

    /**
     * Returns the number of level tasks currently registered.
     *
     * @return the current level task count
     */
    public int getLevelTaskCount() {
        return this.levelTasks.size();
    }

    /**
     * Schedules a one-shot global task.
     *
     * <p>The task runs once with the global context passed to {@link #tickGlobalTasks(Object)} and is
     * then removed automatically.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the number of scheduler ticks to wait before execution; must not be negative
     * @return the unique identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    protected int scheduleTask(Consumer<G> action, int delayTicks) {
        return this.registerGlobalTask(action, delayTicks, 0, false);
    }

    /**
     * Schedules a repeating global task.
     *
     * <p>The task receives the global context passed to {@link #tickGlobalTasks(Object)}. After each
     * execution, its delay counter is reset to {@code periodTicks} and it remains registered until
     * explicitly canceled.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the number of scheduler ticks to wait before the first execution; must not be negative
     * @param periodTicks the number of scheduler ticks between repeated executions; must be greater than zero
     * @return the unique identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    protected int scheduleRepeatingTask(Consumer<G> action, int delayTicks, int periodTicks) {
        return this.registerGlobalTask(action, delayTicks, periodTicks, true);
    }

    /**
     * Schedules a one-shot level task.
     *
     * <p>The task runs once with the level context passed to {@link #tickLevelTasks(Object)} and is
     * then removed automatically.</p>
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the number of scheduler ticks to wait before execution; must not be negative
     * @return the unique identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     */
    protected int scheduleLevelTask(Consumer<L> action, int delayTicks) {
        return this.registerLevelTask(action, delayTicks, 0, false);
    }

    /**
     * Schedules a repeating level task.
     *
     * <p>The task receives the level context passed to {@link #tickLevelTasks(Object)}. After each
     * execution, its delay counter is reset to {@code periodTicks} and it remains registered until
     * explicitly canceled.</p>
     *
     * @param action the action to execute whenever the task runs
     * @param delayTicks the number of scheduler ticks to wait before the first execution; must not be negative
     * @param periodTicks the number of scheduler ticks between repeated executions; must be greater than zero
     * @return the unique identifier assigned to the scheduled task
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws IllegalArgumentException if {@code delayTicks} is negative
     * @throws IllegalArgumentException if {@code periodTicks} is not greater than zero
     */
    protected int scheduleRepeatingLevelTask(Consumer<L> action, int delayTicks, int periodTicks) {
        return this.registerLevelTask(action, delayTicks, periodTicks, true);
    }

    /**
     * Creates and stores a global task.
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the initial delay, measured in scheduler ticks
     * @param periodTicks the repeat period, measured in scheduler ticks
     * @param repeating whether the task should remain registered after execution
     * @return the unique identifier assigned to the scheduled task
     */
    private int registerGlobalTask(Consumer<G> action, int delayTicks, int periodTicks, boolean repeating) {
        int taskId = this.nextTaskId.getAndIncrement();
        this.globalTasks.put(taskId, new ScheduledTask<>(action, delayTicks, periodTicks, repeating));
        return taskId;
    }

    /**
     * Creates and stores a level task.
     *
     * @param action the action to execute when the task runs
     * @param delayTicks the initial delay, measured in scheduler ticks
     * @param periodTicks the repeat period, measured in scheduler ticks
     * @param repeating whether the task should remain registered after execution
     * @return the unique identifier assigned to the scheduled task
     */
    private int registerLevelTask(Consumer<L> action, int delayTicks, int periodTicks, boolean repeating) {
        int taskId = this.nextTaskId.getAndIncrement();
        this.levelTasks.put(taskId, new ScheduledTask<>(action, delayTicks, periodTicks, repeating));
        return taskId;
    }

    /**
     * Represents a single scheduled action and its remaining delay state.
     *
     * <p>The task does not know whether it belongs to the global or level task group. It only stores
     * the action, delay counter, repeat period, and repeat mode for one context type.</p>
     *
     * @param <C> the context type accepted by the scheduled action
     */
    private static final class ScheduledTask<C> {
        private final Consumer<C> action;
        private final int periodTicks;
        private final boolean repeating;

        /**
         * Remaining scheduler ticks before the next execution.
         */
        private int ticksUntilNextRun;

        /**
         * Creates a scheduled task wrapper.
         *
         * @param action the action to execute when the delay counter reaches zero
         * @param delayTicks the initial delay, measured in scheduler ticks
         * @param periodTicks the repeat period, measured in scheduler ticks
         * @param repeating whether this task should continue after its first execution
         * @throws NullPointerException if {@code action} is {@code null}
         * @throws IllegalArgumentException if {@code delayTicks} is negative
         * @throws IllegalArgumentException if {@code repeating} is {@code true} and {@code periodTicks} is not greater than zero
         */
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
         * Advances this task by one scheduler tick and runs it when its delay has elapsed.
         *
         * <p>If the task is not repeating, this method returns {@code true} after the action runs so
         * the owning scheduler can remove it. If the task is repeating, the delay counter is reset to
         * the configured period and the task remains active.</p>
         *
         * @param context the context passed to the scheduled action
         * @return {@code true} if this task has completed and should be removed; {@code false} otherwise
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