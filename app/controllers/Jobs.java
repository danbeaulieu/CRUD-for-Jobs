package controllers;

import models.JobInfo;
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
}
