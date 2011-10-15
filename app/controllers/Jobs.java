package controllers;

import org.apache.commons.lang.StringUtils;

import models.JobInfo;
import play.Logger;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.jobs.JobsPlugin;
import play.mvc.Before;

@CRUD.For(JobInfo.class)
public class Jobs extends CRUD {

	@Before
	public static void doBefore() {
		
		JobInfo.deleteAll();
		for (Job j : JobsPlugin.scheduledJobs) {
			new JobInfo(j).save();
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
			job.now();
			Logger.info("Started Job: %s", job);
		} catch (Throwable t) {
			Logger.error(t, "Error running job");
			throw new UnexpectedException(String.format("Couldn't run job: %s", jobClass));
		}
		
		flash.success("Ran Job: %s", jobClass);
	}
}
