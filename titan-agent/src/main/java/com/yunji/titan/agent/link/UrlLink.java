package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titan.agent.bean.bo.OutParamBO;
import com.yunji.titan.agent.collector.DataCollector;
import com.yunji.titan.agent.stresstest.Stresstest;
import com.yunji.titan.utils.AgentTaskBean;
import com.yunji.titan.utils.ContentType;
import com.yunji.titan.utils.LinkBean;
import com.yunji.titan.utils.LinkScope;
import com.yunji.titan.utils.RequestResultData;
import com.yunji.titan.utils.RequestType;

public class UrlLink  implements Link{

	private static Logger logger = Logger.getLogger(UrlLink.class);
	private static Map<String,String> oneLoopLock=new ConcurrentHashMap();
	private List<RequestResultData> datas=new ArrayList();
	private String url;
	
	private boolean singleLoop;
	private boolean done=false;
	private StressTestResult result;

	@Override
	public StressTestResult execute(StressTestContext stc) {
		result=new StressTestResult();
		RequestResultData rrData=new RequestResultData();
		rrData.setStartTime(System.currentTimeMillis());
		boolean oneLoop=this.isSceneOneLoop(stc);
		try{
			result.setData(this.url);
			String outParam = null;
			logger.info("--开始请求url="+url+" | Thread:"+Thread.currentThread().getName()+" | Time:"+new Date());
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
			Map<String, String> idUrls=stc.getIdUrls();
			if (taskBean.getParams().containsKey(url)) {
				List<String> params = taskBean.getParams().get(url);
				if (!params.isEmpty()) {
					/* 获取压测参数索引 */
					int paramIdex = getParamIndex(taskBean, url, paramIndex, params.size(),stc);
					logger.info("--getParamIndex,url="+url+",paramIdex="+paramIdex);
					if(paramIdex==-1){
						logger.info("-- scene oneloop,url="+this.url);
						waitFirstExecuteDone();
						logger.info("-- firstExecute Done,url="+this.url);
						return result;
					}
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
			Map<String,String> tmpVarValue=new HashMap<String,String>();
			tmpVarValue.putAll(varValue);
			tmpVarValue.putAll(stc.getLocalVarValue());
			tmpVarValue.putAll(stc.getSceneVariableManager().mapVariableValue());
			outParamBO = stresstest.runStresstest(url, outParam, inParam, contentTypes.get(url),
					charsets.get(url),tmpVarValue);
			code = outParamBO.getErrorCode();
			String expression=successExpression.get(url);
			if(!StringUtils.isEmpty(expression)){
				Pattern pattern = Pattern.compile(expression);
				Matcher matcher = pattern.matcher(outParamBO.getData());
				if(!matcher.matches())
				{
					rrData.setSuccess(false);
					rrData.setErrorMessage(outParamBO.getData());
					result.setSuccess(false); 
					logger.info("-- request failed(!matcher.matches()),url="+this.url+" | param:"+outParam+" | inParam:"+inParam+" | tmpVarValue:"+tmpVarValue);
					return result;
				}
			}else if (Integer.parseInt(stc.getCode()) != code) {
				/* 返回业务码不为${code}则失败 */
				result.setSuccess(false); 
				logger.info("-- request failed(stc.getCode()) != code),url="+this.url);
				return result;
			}
			outParam = outParamBO.getData();
			Map<String,String> varTemp=this.getVariableValue(variables, url, outParam);

			stc.getLocalVarValue().clear();
			Map<String,String> map=new HashMap();
			for(Entry<String, String> entry:varTemp.entrySet()){
				if(oneLoop){//场景所有并发用户共享的变量
					map.put(entry.getKey(), entry.getValue());
				}else if(!entry.getKey().contains("_")){//场景执行一次，各个链路共享的变量
					varValue.put(entry.getKey(), entry.getValue());
				}else{//局部变量
					stc.getLocalVarValue().put(entry.getKey(), entry.getValue());
				}
			}
			if(oneLoop){
				String data=JSONObject.toJSONString(map);
				logger.info("--SceneVariableManager add data="+data);
				stc.getSceneVariableManager().add(this.url,map);
			}
			result.setSuccess(true); 
			rrData.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			result.setSuccess(false); 
			rrData.setSuccess(false);
			rrData.setErrorMessage(e.getMessage());
			logger.error(e);
		}finally{
			if(oneLoop){
				oneLoopLock.put(this.url, "");
			}
			rrData.setStopTime(System.currentTimeMillis());
			datas.add(rrData);
		}
		return result;
	
	}
	private void waitFirstExecuteDone(){
		while(!done){
			String data=oneLoopLock.get(this.url);
			if(data!=null){
				done=true;
				break;
			}
		}
	}

	@Override
	public void addLink(Link link) {
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
	private int getParamIndex(AgentTaskBean taskBean, String url, Map<String, Integer> paramIndex, int paramSize,
			StressTestContext stc) {
		synchronized (paramIndex) {
			Integer index=paramIndex.get(url);
			if (index==null || index !=-1) {
				paramIndex.put(url, !paramIndex.containsKey(url) ? 0 : paramIndex.get(url) + 1);
			}
			/* 持续轮询 */
			if (paramIndex.get(url) == paramSize) {
				if(this.isSceneOneLoop(stc)){
					paramIndex.put(url, -1);
				}else{
					logger.debug("重新开始轮询参数");
					paramIndex.put(url, 0);
				}
			}
			return paramIndex.get(url);
		}
	}
	private boolean isSceneOneLoop(StressTestContext stc){
		Long linkId=Long.parseLong( getLinkId(url,stc.getIdUrls()));
		LinkBean lb=stc.getLinks().stream().filter(
				(LinkBean b) -> b.getLinkId().equals(linkId)
				).findFirst().orElse(new LinkBean());
		boolean oneloop=lb.contain(LinkScope.SCENE_ONELOOP);
		return oneloop;
	}

	private String getLinkId(String url,Map<String,String> map){
		for(Entry<String, String> e:map.entrySet()){
			if(e.getValue().equals(url)){
				return e.getKey();
			}
		}
		return "";
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
		Map<String,String> map=new HashMap<String,String>();
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
				   }else if(param instanceof JSONArray){
					   param = ((JSONArray) param).getJSONObject(0).toString();
				   }
			}
			map.put(varName, String.valueOf(param) );
		}
		return map;
   }

	public boolean isSingleLoop() {
		return singleLoop;
	}

	public void setSingleLoop(boolean singleLoop) {
		this.singleLoop = singleLoop;
	}
   public static void init(){
	   oneLoopLock.clear();
   }
   @Override
   public void collectData() {
	   	DataCollector.add(this.url, this.datas);
   }
}
