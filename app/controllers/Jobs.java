package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import models.JobInfo;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.jobs.JobsPlugin;
import play.modules.crudjobs.ProgressHandler;
import play.modules.crudjobs.ProgressJob;
import play.mvc.Before;

@CRUD.For(JobInfo.class)
public class Jobs extends CRUD {

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
		Logger.info(String.format("Job Class: %s, Number of Concurrent Instances: %s", jobClass, 1));
		if (StringUtils.isBlank(jobClass)) {
			throw new RuntimeException("Invalid Job Class Name");
		}
		
		try {
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

			// Log Debug
			Logger.info("Starting Job: %s", o);

			// Fire Job
			Job job = (Job) o;
			
			if (job instanceof ProgressJob) {
				((ProgressJob) job).registerProgressHandler(new ProgressHandler() {
					public void handle(Integer value) {
						Logger.info("Handled progress " + value);
					}
				});
			}
			job.now();
			Logger.info("Started Job: %s", job);
		} catch (Throwable t) {
			Logger.error(t, "Error running job");
			throw new UnexpectedException(String.format("Couldn't run job: %s", jobClass));
		}
		
		flash.success("Ran Job: %s", jobClass);
	}
}
