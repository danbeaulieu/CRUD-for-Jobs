package models;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import play.jobs.OnApplicationStop;

@Entity
public class JobInfo extends Model {

	public enum Type {
		EVERY,
		CRON,
		STARTUP,
		SHUTDOWN,
		UNSCHEDULED
	}
	
	@Transient
	Job job;
	// if last run resulted in an error
	public Boolean wasError;
	// Date of last run
	public Date lastRun;
	// Date of next planned run
	public Date nextPlannedExecution;
	// Type of job
	public Type type;
	// Annotation value
	public String value = "";
	// The name (class name for now)
	public String name = "";
	
	public JobInfo(Job job) {
		
		this.job = job;
		this.name = job.toString();
		init();
	}
	
	public JobInfo(Class clazz) {
		this.job = null;
		this.name = clazz.getCanonicalName();
		this.type = Type.UNSCHEDULED;
	}

	private void init() {
	
		try {
			this.nextPlannedExecution = nextPlannedExecution();
			this.wasError = wasError();
			this.lastRun = lastRun();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean wasError() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		Field wasError = Job.class.getDeclaredField("wasError");
		wasError.setAccessible(true);
		return (Boolean) wasError.get(this.job);
	}
	
	public Date lastRun() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		Field lastRun = Job.class.getDeclaredField("lastRun");
		lastRun.setAccessible(true);
		Long timestamp = (Long)lastRun.get(this.job);
		if (timestamp == 0l) {
			return null;
		}
		return new Date((Long)lastRun.get(this.job));
	}
	
	public Date nextPlannedExecution() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		Field nextPlannedExecution = Job.class.getDeclaredField("nextPlannedExecution");
		nextPlannedExecution.setAccessible(true);
		//return (Date) nextPlannedExecution.get(this.job);
		if (this.job.getClass().isAnnotationPresent(Every.class)) {
			this.type = Type.EVERY;
			this.value = job.getClass().getAnnotation(Every.class).value();
		}
		if (this.job.getClass().isAnnotationPresent(On.class)) {
			this.type = Type.CRON;
			this.value = job.getClass().getAnnotation(Every.class).value();
		}
		if (this.job.getClass().isAnnotationPresent(OnApplicationStart.class)) {
			this.type = Type.STARTUP;
		}
		if (this.job.getClass().isAnnotationPresent(OnApplicationStop.class)) {
			this.type = Type.SHUTDOWN;
		}
		// TODO finish this
		return new Date();
	}
	
	public String toString() {
		
		return this.name;
	}
}
