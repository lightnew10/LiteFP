package fr.lightnew.npc.tools;

import fr.lightnew.npc.entities.npc.NPCCreator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkerLoadNPC extends BukkitRunnable {
    private Queue<Runnable> todo = new ArrayDeque<>();

    @Override
    public void run() {
        if (todo.isEmpty())
            return;
        Runnable current = todo.poll();
        current.run();
    }

    public void put(Player player, NPCCreator creator) {
        /*todo.add(() -> {
            // TODO : INJECT
        });*/
        CompletableFuture.runAsync(() -> {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeOnTimeout(null, 200, TimeUnit.MILLISECONDS).thenRunAsync(() -> creator.spawnNPC(player));
        }, Executors.newCachedThreadPool()).thenRun(() -> {
        });
    }
}
