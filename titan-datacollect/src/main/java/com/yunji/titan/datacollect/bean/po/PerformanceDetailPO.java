package com.yunji.titan.datacollect.bean.po;

public class PerformanceDetailPO {
	private long performanceId;
	private String url;
	private int x_time;
	private long start;
	private long stop;
	private boolean success;
	private String message;
	public long getPerformanceId() {
		return performanceId;
	}
	public void setPerformanceId(long performanceId) {
		this.performanceId = performanceId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getX_time() {
		return x_time;
	}
	public void setX_time(int x_time) {
		this.x_time = x_time;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getStop() {
		return stop;
	}
	public void setStop(long stop) {
		this.stop = stop;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	

}
