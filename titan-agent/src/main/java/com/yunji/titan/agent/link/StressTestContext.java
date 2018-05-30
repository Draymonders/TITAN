package com.yunji.titan.agent.link;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.yunji.titan.agent.stresstest.Stresstest;
import com.yunji.titan.utils.AgentTaskBean;
import com.yunji.titan.utils.ContentType;
import com.yunji.titan.utils.RequestType;

public class StressTestContext {
	private Map<String, Integer> paramIndex;
	private Map<String, RequestType> requestTypes;
	private AgentTaskBean taskBean;
	private Stresstest httpGetRequestStresstest;
	private Stresstest httpPostRequestStresstest;
	private Map<String, String> charsets;
	//存储一个url有哪些变量，变量取值公式
	private Map<String, List<String>> variables;
	private Map<String, String> successExpression;
	private Map<String, ContentType> contentTypes;
	//全局的变量、变量值
	private Map<String,String> varValue;
	//是否成功编码
	private String code;
	
	public Map<String, Integer> getParamIndex() {
		return paramIndex;
	}
	public void setParamIndex(Map<String, Integer> paramIndex) {
		this.paramIndex = paramIndex;
	}
	public Map<String, RequestType> getRequestTypes() {
		return requestTypes;
	}
	public void setRequestTypes(Map<String, RequestType> requestTypes) {
		this.requestTypes = requestTypes;
	}
	public AgentTaskBean getTaskBean() {
		return taskBean;
	}
	public void setTaskBean(AgentTaskBean taskBean) {
		this.taskBean = taskBean;
	}
	public Stresstest getHttpGetRequestStresstest() {
		return httpGetRequestStresstest;
	}
	public void setHttpGetRequestStresstest(Stresstest httpGetRequestStresstest) {
		this.httpGetRequestStresstest = httpGetRequestStresstest;
	}
	public Stresstest getHttpPostRequestStresstest() {
		return httpPostRequestStresstest;
	}
	public void setHttpPostRequestStresstest(Stresstest httpPostRequestStresstest) {
		this.httpPostRequestStresstest = httpPostRequestStresstest;
	}
	public Map<String, String> getCharsets() {
		return charsets;
	}
	public void setCharsets(Map<String, String> charsets) {
		this.charsets = charsets;
	}
	public Map<String, List<String>> getVariables() {
		return variables;
	}
	public void setVariables(Map<String, List<String>> variables) {
		this.variables = variables;
	}
	public Map<String, String> getSuccessExpression() {
		return successExpression;
	}
	public void setSuccessExpression(Map<String, String> successExpression) {
		this.successExpression = successExpression;
	}
	public Map<String, ContentType> getContentTypes() {
		return contentTypes;
	}
	public void setContentTypes(Map<String, ContentType> contentTypes) {
		this.contentTypes = contentTypes;
	}
	public Map<String, String> getVarValue() {
		return varValue;
	}
	public void setVarValue(Map<String, String> varValue) {
		this.varValue = varValue;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
}
