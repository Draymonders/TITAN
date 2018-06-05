/*
 * Copyright (C) 2015-2020 yunjiweidian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.yunji.titan.agent;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titan.agent.link.Link;
import com.yunji.titan.agent.link.LinkRelolver;
import com.yunji.titan.agent.link.StressTestContext;
import com.yunji.titan.agent.link.StressTestResult;
import com.yunji.titan.agent.state.AgentStateContext;
import com.yunji.titan.agent.state.FreeState;
import com.yunji.titan.agent.state.StopState;
import com.yunji.titan.agent.stresstest.Stresstest;
import com.yunji.titan.agent.utils.ChangeAgentState;
import com.yunji.titan.agent.utils.MsgProvider;
import com.yunji.titan.agent.utils.ResultStatistics;
import com.yunji.titan.agent.watch.registry.RegisterAgent;
import com.yunji.titan.utils.AgentTaskBean;
import com.yunji.titan.utils.ContentType;
import com.yunji.titan.utils.RequestType;
import com.yunji.titan.utils.ThreadPoolManager;
import com.yunji.titan.utils.ZookeeperConnManager;

/**
 * 请求处理者实现
 * 
 * @author gaoxianglong
 */
@Service
public class RequestHandler {
	private Thread checkInterrupted;
	@Resource
	private ZookeeperConnManager zookeeperConnManager;
	@Resource
	private ThreadPoolManager threadPoolManager;
	@Resource(name = "httpGetRequestStresstest")
	private Stresstest httpGetRequestStresstest;
	@Resource(name = "httpPostRequestStresstest")
	private Stresstest httpPostRequestStresstest;
	@Resource
	private MsgProvider msgProvider;
	@Resource
	private RegisterAgent registerAgent;
	@Resource
	private AgentStateContext agentStateContext;

	/**
	 * 当前实际并发用户
	 */
	private AtomicInteger concurrentUser;
	private int initConcurrentUsersSize;
	private int concurrentUsersSize;
	@Value("${code}")
	private String code;
	private Logger log = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler() {
		concurrentUser = new AtomicInteger(0);

	}

	public void handler(final String nodePath, final AgentTaskBean taskBean) {
		if (null == taskBean) {
			return;
		}
		final List<String> urls = taskBean.getUrls();
		initConcurrentUsersSize = taskBean.getInitConcurrentUsersSize();
		concurrentUsersSize = taskBean.getConcurrentUsersSize();
		/* 每个并发用户分配的任务数 */
		final long taskSize = taskBean.getTaskSize();
		final Map<String, ContentType> contentTypes = taskBean.getContentTypes();
		final Map<String, String> charsets = taskBean.getCharsets();
		final Map<String, List<String>> variables = taskBean.getVariables();
		final Map<String, String> successExpression = taskBean.getSuccessExpression();
		final Map<String, RequestType> requestTypes = taskBean.getRequestTypes();
		final CountDownLatch latch = new CountDownLatch(concurrentUsersSize);
		final Map<String, Integer> paramIndex = new HashMap<String, Integer>(16);
		final AtomicInteger httpSuccessNum = new AtomicInteger(0);
		final AtomicInteger serviceSuccessNum = new AtomicInteger(0);
		/* 只有线程池shutdown后才重新初始化 */
		if (threadPoolManager.getThreadPool().isShutdown()) {
			threadPoolManager.init();
		}
		/* 获取每个agent所允许的最大的执行时间 */
		final long continuedTime = taskBean.getContinuedTime();
		if (0 < continuedTime) {
			checkInterrupted = new Thread(() -> {
				try {
					TimeUnit.SECONDS.sleep(continuedTime);
					ZooKeeper zkClient = zookeeperConnManager.getZkClient();
					String value = new String(zkClient.getData(nodePath, false, null));
					if (!StringUtils.isEmpty(value)) {
						Properties properties = new Properties();
						properties.load(new StringReader(value));
						String agentStateKey = "task";
						String interruptedKey = "interrupted";
						/* 忙碌状态才可停止task=true\interrupted=false */
						if (Boolean.parseBoolean(properties.getProperty(agentStateKey))
								&& !Boolean.parseBoolean(properties.getProperty(interruptedKey))) {
							ChangeAgentState.change(agentStateContext, new StopState(), zkClient, nodePath);
						} else {
							log.info("当前agent任务已经结束,无需停止");
						}
					}
				} catch (Exception e) {
					log.error("error", e);
				}
			});
			checkInterrupted.start();
		}
		log.debug("agent准备工作已完成");
		long startTime = System.currentTimeMillis();
		if (0 < initConcurrentUsersSize) {
			/* 准备开始预热 */
			runStresstest(initConcurrentUsersSize, taskSize, urls, requestTypes, taskBean, paramIndex, httpSuccessNum,
					serviceSuccessNum, latch, contentTypes, charsets,variables,successExpression,taskBean.getContainLinkIds(),
					taskBean.getIdUrls());
			log.info("起步量级的预热任务(起步量级-->" + initConcurrentUsersSize + ",每个并发用户分配的任务数-->" + taskSize
					+ ")已经完成,开始准备过渡到正常流量");
		}
		final int remConcurrentusersSize = concurrentUsersSize - initConcurrentUsersSize;
		if (remConcurrentusersSize > 0) {
			runStresstest(remConcurrentusersSize, taskSize, urls, requestTypes, taskBean, paramIndex, httpSuccessNum,
					serviceSuccessNum, latch, contentTypes, charsets,variables,successExpression,taskBean.getContainLinkIds(),
					taskBean.getIdUrls());
		}
		try {
			latch.await();
			long endTime = System.currentTimeMillis();
			String result = ResultStatistics.result(startTime, endTime, httpSuccessNum.get(), serviceSuccessNum.get(),
					taskBean);
			log.info("压测结果-->" + result);
			ChangeAgentState.change(agentStateContext, new FreeState(), zookeeperConnManager.getZkClient(), nodePath);
			if (null != checkInterrupted) {
				/* 暴力结束interrupted信号检测线程 */
				checkInterrupted.stop();
			}
			if (!StringUtils.isEmpty(result)) {
				msgProvider.sendMsg(result);
			}
		} catch (Exception e) {
			log.error("error", e);
		} finally {
			concurrentUser.set(0);
		}
	}

	/**
	 * 执行压测任务
	 * 
	 * @author gaoxianglong
	 */
	private void runStresstest(int concurrentUsersSize, final long taskSize, final List<String> urls,
			final Map<String, RequestType> requestTypes, final AgentTaskBean taskBean,
			final Map<String, Integer> paramIndex, final AtomicInteger httpSuccessNum,
			final AtomicInteger serviceSuccessNum, final CountDownLatch latch,
			final Map<String, ContentType> contentTypes, final Map<String, String> charsets,final Map<String, List<String>> variables,
			final Map<String, String> successExpression,String containLinkIds,final Map<String, String> idUrls) {
		if(containLinkIds==null || "".equals(containLinkIds)){
			log.error("--containLinkIds cann't be null");
			return;
		}
		for (int i = 0; i < concurrentUsersSize; i++) {
			threadPoolManager.getThreadPool().execute(() -> {
				concurrentUser.getAndIncrement();
				for (long j = 0; j < taskSize; j++) {
					int code = -10000;
					/* 如果线程收到interrupted信号则停止执行压测任务 */
					if (Thread.currentThread().isInterrupted()) {
						log.debug("当前线程" + Thread.currentThread().getName() + "收到interrupted信号,成功停止当前压测任务");
						break;
					}
					/* 全链路压测时上一个接口的出参 */
					String outParam = null;
					Map<String,String> varValue=new HashMap<String,String>();

					LinkRelolver r=new LinkRelolver();
					r.setThreadPoolManager(this.threadPoolManager);
					r.setIdUrls(idUrls);
					Link link=r.relover(containLinkIds);
					StressTestContext stc=new StressTestContext();
					stc.setCharsets(charsets);
					stc.setCode(this.code);
					stc.setContentTypes(contentTypes);
					stc.setHttpGetRequestStresstest(httpGetRequestStresstest);
					stc.setHttpPostRequestStresstest(httpPostRequestStresstest);
					stc.setParamIndex(paramIndex);
					stc.setRequestTypes(requestTypes);
					stc.setSuccessExpression(successExpression);
					stc.setTaskBean(taskBean);
					stc.setVariables(variables);
					stc.setVarValue(varValue);
					StressTestResult result=link.execute(stc);
					
//					boolean result = true;
					/* 检测一次链路的压测结果 */
					if (result.isSuccess()) {
						httpSuccessNum.getAndIncrement();
						serviceSuccessNum.getAndIncrement();
					} else if (-10000 != code) {
						httpSuccessNum.getAndIncrement();
					}
				}
				latch.countDown();
			});
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
				   }else if(param instanceof JSONArray){
					   param = ((JSONArray) param).getJSONObject(0).toString();
				   }
			}
			map.put(varName, String.valueOf(param) );
		}
		return map;
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
				log.debug("重新开始轮询参数");
				paramIndex.put(url, 0);
			}
			return paramIndex.get(url);
		}
	}

	/**
	 * 给线程池中所有的线程发出interrupted信号结束当前agent的压测任务,但线程并不会立即停止
	 * 
	 * @author gaoxianglong
	 */
	public void interruptedTask() {
		ThreadPoolExecutor threadPool = threadPoolManager.getThreadPool();
		/* 只有线程池未shutdown才允许shutdown */
		if (!threadPool.isShutdown()) {
			log.info("收到interrupted指令");
			while (true) {
				try {
					TimeUnit.SECONDS.sleep(1);
					if (0 < concurrentUsersSize) {
						/*
						 * 如果当前实际并发用户等于concurrentUsersSize才允许对ThreadPool执行shutdownNow操作
						 * ,因为需要等待所有线程countDown,否则主线程就会一直阻塞
						 */
						if (concurrentUser.get() == concurrentUsersSize) {
							threadPoolManager.getThreadPool().shutdownNow();
							log.info("正在尝试中断当前agent的所有压测任务...");
							break;
						}
					}
				} catch (InterruptedException e) {
					log.error("error", e);
				}
			}
		}
	}
	public static void main(String[] args) {
//		String expression=".*{5,}(\"total\").*{1,}";
		String expression="^.*\"total\".*$";
		if(expression!=null){
			Pattern pattern = Pattern.compile(expression);
			Matcher matcher = pattern.matcher("{\"total1\":446,\"rows\":[");
			if(!matcher.matches())
			{
				System.out.println("no");
			}
		}
	}
}
