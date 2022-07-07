package me.flashyreese.mods.commandaliases.command;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class Scheduler {

    private final Queue<Event> events = new PriorityQueue<>(Comparator.comparingLong(Event::getTriggerTime));

    public void processEvents() {
        while(true) {
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

    public void remove(String name) {
        Optional<Event> optionalEvent = this.events.stream().filter(event -> event.getName().equals(name)).findFirst();
        optionalEvent.ifPresent(this.events::remove);
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
