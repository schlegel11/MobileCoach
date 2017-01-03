package ch.ethz.mc.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class InternalTimer {
	
	private Timer timer = new Timer();
	private Set<ScheduledTask> scheduledTasks = new HashSet<>();
	
	public InternalTimer(){
		// TODO Dominik: make sure the listeners are also removed
		InternalDateTime.addJumpListener(new Runnable(){
			@Override
			public void run() {
				jumpAhead();
			}
		});
	}
	
	public void cancelAll(){
		timer.cancel();
		timer = new Timer();
	}
	
	public void schedule(Runnable task, long milliseconds){
		final ScheduledTask scheduledTask = new ScheduledTask();
		TimerTask timerTask = new java.util.TimerTask() {
			@Override
			public void run() {
				scheduledTasks.remove(scheduledTask);
				task.run();
			};
		};
		timer.schedule(timerTask, milliseconds);
		
		scheduledTask.milliseconds = milliseconds;
		scheduledTask.scheduledAt = InternalDateTime.currentTimeMillis();
		scheduledTask.scheduledTask = timerTask;
		scheduledTask.task = task;
		
		scheduledTasks.add(scheduledTask);
	}

	private void jumpAhead() {
		for (ScheduledTask t: scheduledTasks){
			if (t.scheduledTask.cancel()){
				// we successfully canceled the task: re-schedule
				scheduledTasks.remove(t);
				long now = InternalDateTime.currentTimeMillis();
				schedule(t.task, Math.max(0, t.milliseconds - (now - t.scheduledAt)));
			}
		}
	}
	
	private class ScheduledTask {
		
		public long milliseconds;
		public long scheduledAt;
		public Runnable task;
		public TimerTask scheduledTask;
	}

}
