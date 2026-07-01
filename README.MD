# HeatherSchedulers

HeatherSchedulers is a small Fabric mod that adds simple task schedulers for Minecraft mods.

The mod creates one server scheduler and one client scheduler. These schedulers run tasks from Fabric tick events instead of using separate threads.

Server tasks run during server ticks. Client tasks run during client ticks. Level tasks run during world or level ticks.

Tasks can be delayed, repeated, canceled individually, or cleared all at once.

When the server or client stops, the mod clears the stored tasks so old tasks do not remain active after shutdown.

## Example

```java
HeatherSchedulers.SERVER.schedule(server -> {
    // Runs once after 20 server ticks.
}, 20);

HeatherSchedulers.SERVER.scheduleRepeating(server -> {
    // Runs every 20 server ticks.
}, 20, 20);