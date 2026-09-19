package me.flashyreese.mods.commandaliases.command;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Represents an event scheduler.
 *
 * @author FlashyReese
 * @version 0.9.0
 * @since 0.9.0
 */
public class Scheduler {
    private final Queue<Event> events = new PriorityQueue<>(Comparator.comparingLong(Event::getTriggerTime));

    public void processEvents() {
        while (true) {
            Event event = this.events.peek();
            if (event == null || event.triggerTime > System.currentTimeMillis()) {
                return;
            }

            this.events.remove();
            event.runnable.run();
        }
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public boolean remove(String name) {
        Stream<Event> eventStream = this.events.stream().filter(event -> event.getName().equals(name));
        return this.events.removeAll(eventStream.toList());
    }

    public boolean contains(String name) {
        return this.events.stream().anyMatch(event -> event.getName().equals(name));
    }

    public static class Event {
        private final long triggerTime;
        private final String name;
        private final Runnable runnable;

        public Event(long triggerTime, String name, Runnable runnable) {
            this.triggerTime = triggerTime;
            this.name = name;
            this.runnable = runnable;
        }

        public long getTriggerTime() {
            return triggerTime;
        }

        public String getName() {
            return name;
        }

        public Runnable getRunnable() {
            return runnable;
        }
    }
}
