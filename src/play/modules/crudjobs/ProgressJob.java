package play.modules.crudjobs;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.jobs.Job;

public abstract class ProgressJob extends Job {
	
	public static final int DEFAULT_MAX = 100;
	
	private List<ProgressHandler> handlers = new ArrayList<ProgressHandler>();
	
	public void registerProgressHandler(ProgressHandler h) {
		this.handlers.add(h);
	}
	
	public void publishProgress(Integer value) {
		if (handlers != null) {
			for (ProgressHandler handler : handlers) {
				handler.handle(value);
			}
		}
		else {
			Logger.error("Progress Handler not set, ignoring Job progress");
		}
	}
	
	public void _finally() {
		Logger.info("ProgressJob finally");
		publishProgress(100);
		super._finally();
	}
}
