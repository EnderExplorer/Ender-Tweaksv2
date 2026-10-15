package com.endertweeks.core;

import java.util.ArrayDeque;

/** Runs queued actions one per game tick, with an optional pause (in ticks) between them. */
public final class Steps {
	private final ArrayDeque<Runnable> queue = new ArrayDeque<>();
	private int wait;

	public void add(Runnable r) { queue.add(r); }
	public boolean busy() { return !queue.isEmpty(); }
	public void clear() { queue.clear(); wait = 0; }

	public void tick() { tick(0); }

	public void tick(int delay) {
		if (wait > 0) { wait--; return; }
		Runnable r = queue.poll();
		if (r != null) {
			r.run();
			wait = delay;
		}
	}
}
