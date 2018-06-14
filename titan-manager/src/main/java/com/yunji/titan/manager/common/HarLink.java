package com.yunji.titan.manager.common;

public class HarLink {
	private HarRequest request;
	private String startedDateTime;
	private String cache;
	private String response;
	private String timings;
	private String time;

	public HarRequest getRequest() {
		return request;
	}

	public void setRequest(HarRequest request) {
		this.request = request;
	}

	public String getStartedDateTime() {
		return startedDateTime;
	}

	public void setStartedDateTime(String startedDateTime) {
		this.startedDateTime = startedDateTime;
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getTimings() {
		return timings;
	}

	public void setTimings(String timings) {
		this.timings = timings;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}