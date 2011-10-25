package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import models.JobInfo;
import models.RunJobResponse;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.jobs.JobsPlugin;
import play.modules.crudjobs.ProgressHandler;
import play.modules.crudjobs.ProgressJob;
import play.mvc.Before;
import play.mvc.WebSocketController;

@CRUD.For(JobInfo.class)
public class Jobs extends CRUD {
	
	public static class WebSocket extends WebSocketController {
		
		public static void runJobWithProgress(String jobClass) {
			Job job = getJobInstance(jobClass);
			if (job == null) {
				throw new RuntimeException("No job instance returned: " + jobClass);
			}
			if (job instanceof ProgressJob) {
				((ProgressJob) job).registerProgressHandler(new ProgressHandler() {

					@Override
					public void handle(Integer value) {
						outbound.send(value.toString());
					}

					@Override
					public boolean remove() {
						// TODO Auto-generated method stub
						return false;
					}
					
				});
			}
			job.now();
			outbound.send("Foo");
		}
	}
	
	private static Job getJobInstance(String jobClass) {
		Logger.info("get instance");
		Job job = null;
		if (StringUtils.isBlank(jobClass)) {
			throw new RuntimeException("Invalid Job Class Name");
		}
		
		try {
			// Try to pull the instance from the scheduled jobs first
			for (Job j : JobsPlugin.scheduledJobs) {
				if (j.toString().equals(jobClass)) {
					return j;
				}
	    	}
			Logger.info("%s is not a scheduled job", jobClass);
			Class clazz = Class.forName(jobClass);
			if (clazz == null) {
				throw new RuntimeException("Invalid Job Class: " + jobClass);
			}
			// Create Instance
			Object o = clazz.newInstance();

			// Check Instance Type
			if ((o instanceof Job) == false) {
				throw new RuntimeException("Invalid Class Instance: " + o);
			}

			job = (Job) o;
			
		} catch (Throwable t) {
			Logger.error(t, "Error running job");
			throw new UnexpectedException(String.format("Couldn't run job: %s", jobClass));
		}
		return job;
	}
	/*
	 * Executed before every request handled by this controller
	 */
	@Before
	public static void doBefore() {
		
		Set<String> scheduledNames = new HashSet<String>();
		// get all of the scheduled jobs
		// this should probably be an update instead of clearing them all out
		// and adding them back in
		JobInfo.deleteAll();
		for (Job j : JobsPlugin.scheduledJobs) {
			scheduledNames.add(j.toString());
			new JobInfo(j).save();
    	}
		// get all of the unscheduled jobs
		// this really only needs to happen once
		for (Class clazz : Play.classloader.getAllClasses()) {
			if ((clazz != null) && Job.class.isAssignableFrom(clazz)) {
				if(!scheduledNames.contains(clazz.getCanonicalName())) {
					new JobInfo(clazz).save();
				}
			}
		}
	}
	
	public static void blank() throws Exception {
		
		render("CRUD/index.html");
	}
	
	public static void create() throws Exception {
		
		render("CRUD/index.html");
	}
	
	/*
	 * Lots of code cribbed from play-jobs module
	 * 
	 */
	public static void runNow(String jobClass) {
		Logger.info(String.format("Job Class: %s", jobClass));
		Job job = getJobInstance(jobClass);
		if (job == null) {
			throw new RuntimeException("No job instance returned: " + jobClass);
		}
		final Long jobKey = System.currentTimeMillis();
		boolean hasProgress = false;
		if (job instanceof ProgressJob) {
			
			hasProgress = true;
			((ProgressJob) job).registerProgressHandler(new ProgressHandler() {
				public void handle(Integer value) {
					Logger.info("Handled progress " + value);
					Cache.set(jobKey.toString(), value, "10mn");
				}
				
				public boolean remove() {
					return true;
				}
			});
		}
		Logger.info("Starting job: %s", jobClass);
		job.now();
		
		flash.success("Ran Job: %s", jobClass);
		
		renderJSON(new RunJobResponse(jobKey, hasProgress));
	}
	
	public static void jobProgress(String jobKey) {
		renderJSON("{\"progress\":" + Cache.get(jobKey) + "}");
	}
}
