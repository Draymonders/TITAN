package com.yunji.titan.agent.link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.yunji.titan.agent.bean.bo.OutParamBO;
import com.yunji.titan.agent.stresstest.Stresstest;
import com.yunji.titan.utils.AgentTaskBean;
import com.yunji.titan.utils.ContentType;
import com.yunji.titan.utils.RequestType;

public class UrlLink  implements Link{
	
	private Logger logger = LoggerFactory.getLogger(UrlLink.class);
	
	private String url;

	@Override
	public StressTestResult execute(StressTestContext stc) {
		StressTestResult result=new StressTestResult();
		String outParam = null;
		logger.info("--开始请求url="+url);
		int code = -10000;
		Stresstest stresstest = null;
		String inParam = null;
		OutParamBO outParamBO = null;
		AgentTaskBean taskBean=stc.getTaskBean();
		Map<String, Integer> paramIndex=stc.getParamIndex();
		Map<String, RequestType> requestTypes=stc.getRequestTypes();
		Stresstest httpGetRequestStresstest=stc.getHttpGetRequestStresstest();
		Stresstest httpPostRequestStresstest=stc.getHttpPostRequestStresstest();
		Map<String, String> charsets=stc.getCharsets();
		Map<String, ContentType> contentTypes=stc.getContentTypes();
		Map<String,String> varValue=stc.getVarValue();
		Map<String, String> successExpression=stc.getSuccessExpression();
		Map<String, List<String>> variables=stc.getVariables();
		if (taskBean.getParams().containsKey(url)) {
			List<String> params = taskBean.getParams().get(url);
			if (!params.isEmpty()) {
				/* 获取压测参数索引 */
				int paramIdex = getParamIndex(taskBean, url, paramIndex, params.size());
				inParam = params.get(paramIdex);
			}
		}
		switch (requestTypes.get(url)) {
		case GET:
			stresstest = httpGetRequestStresstest;
			break;
		case POST:
			stresstest = httpPostRequestStresstest;
			break;
		default:
			break;
		}
		outParamBO = stresstest.runStresstest(url, outParam, inParam, contentTypes.get(url),
				charsets.get(url),varValue);
		code = outParamBO.getErrorCode();
		String expression=successExpression.get(url);
		if(!StringUtils.isEmpty(expression)){
			Pattern pattern = Pattern.compile(expression);
			Matcher matcher = pattern.matcher(outParamBO.getData());
			if(!matcher.matches())
			{
				result.setSuccess(false); 
				return result;
			}
		}else if (Integer.parseInt(stc.getCode()) != code) {
			/* 返回业务码不为${code}则失败 */
			result.setSuccess(false); 
			return result;
		}
		outParam = outParamBO.getData();
		Map<String,String> varTemp=this.getVariableValue(variables, url, outParam);

		for(Entry<String, String> entry:varTemp.entrySet()){
			if(!entry.getKey().contains("_")){
				varValue.put(entry.getKey(), entry.getValue());
			}
		}
		result.setSuccess(true); 
		return result;
	
	}

	@Override
	public void addLink(Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLinkIds(String ids) {
		// TODO Auto-generated method stub
		
	}
	
	public void setUrl(String url){
		this.url=url;
	}

	/**
	 * 获取压测参数索引
	 * 
	 * @author gaoxianglong
	 */
	private int getParamIndex(AgentTaskBean taskBean, String url, Map<String, Integer> paramIndex, int paramSize) {
		synchronized (paramIndex) {
			paramIndex.put(url, !paramIndex.containsKey(url) ? 0 : paramIndex.get(url) + 1);
			/* 持续轮询 */
			if (paramIndex.get(url) == paramSize) {
				logger.debug("重新开始轮询参数");
				paramIndex.put(url, 0);
			}
			return paramIndex.get(url);
		}
	}
	/**
	 * 解析返回数据，给链路定义的变量赋值
	 * @param variables
	 * @param url
	 * @param outParam
	 * @return
	 */
   private Map<String,String> getVariableValue(Map<String, List<String>> variables,String url,String outParam){
		List<String> vars=variables.get(url);
		Map<String,String> map=new HashMap();
		if(outParam==null){
			return null;
		}
		for(String var:vars){
			String[] v=var.split(",");
			String varName=v[0];
			String varExpression=v[1];
			Object param=outParam;
			String[] keys=varExpression.split("\\.");
			for(int i=0;i<keys.length;i++){
				   JSONObject j=JSONObject.parseObject((String)param);
				   param=j.get(keys[i]);
				   if(param instanceof JSONObject){
					   param=((JSONObject)param).toString();
				   }
			}
			map.put(varName, String.valueOf(param) );
		}
		return map;
   }
}
