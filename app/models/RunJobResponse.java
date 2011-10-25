package models;

public class RunJobResponse {

	Long key;
	boolean hasProgress;
	
	public RunJobResponse(Long jobKey, boolean hasProgress) {
		this.key = jobKey;
		this.hasProgress = hasProgress;
	}

}
