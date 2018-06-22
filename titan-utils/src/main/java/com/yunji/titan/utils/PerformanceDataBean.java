package com.yunji.titan.utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PerformanceDataBean implements Serializable{

	private static final long serialVersionUID = -2297284626420463425L;
	
	private String taskId;
	private int sceneId;
	private int agentSize;

	private Map<String,List<RequestResultData>> requestDatas;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}

	public Map<String, List<RequestResultData>> getRequestDatas() {
		return requestDatas;
	}

	public void setRequestDatas(Map<String, List<RequestResultData>> requestDatas) {
		this.requestDatas = requestDatas;
	}

	public int getAgentSize() {
		return agentSize;
	}

	public void setAgentSize(int agentSize) {
		this.agentSize = agentSize;
	}
	
	
}
