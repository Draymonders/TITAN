/*
 * Copyright 2015-2101 yunjiweidian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunji.titan.datacollect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.yunji.titan.utils.MonitorBean;
import com.yunji.titan.utils.PerformanceDataBean;
import com.yunji.titan.utils.RequestResultData;
import com.yunji.titan.utils.ResultBean;
import com.yunji.titan.utils.ThreadPoolManager;
import com.yunji.titan.utils.config.RocketMqDataSource;

/**
 * 从消息队列中获取压测结果数据信息
 * 
 * @author gaoxianglong
 */
@Service
public class SubscribeMessage {
	@Resource
	private ThreadPoolManager threadPoolManager;
	@Resource
	private UploadData uploadData;
	@Resource(name = "operationalIndicator")
	private RocketMqDataSource operationalIndicator;
	@Resource(name = "monitorIndicator")
	private RocketMqDataSource monitorIndicator;
	@Resource(name = "performanceIndicator")
	private RocketMqDataSource performanceIndicator;
	private Map<String, List<ResultBean>> resultMap;
	private Map<String,Map<String, List<RequestResultData>>> resultPerformanceMap;
	private Map<String, CountDownLatch> countDownLatchMap;
	private Logger log = LoggerFactory.getLogger(SubscribeMessage.class);

	public SubscribeMessage() {
		countDownLatchMap = new ConcurrentHashMap<String, CountDownLatch>();
		resultMap = new ConcurrentHashMap<String, List<ResultBean>>();
		resultPerformanceMap=new ConcurrentHashMap(); 
	}

	protected void getMsg() {
		getOperationalIndicator();
		getMonitorIndicator();
		this.getPerformanceIndicatorIndicator();
	}

	/**
	 * 获取业务指标数据
	 * 
	 * @author gaoxianglong
	 */
	private void getOperationalIndicator() {
		DefaultMQPushConsumer consumer = operationalIndicator.getConsumer();
		try {
			consumer.subscribe(operationalIndicator.getRocketTopic(), "*");
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			MessageListenerConcurrently titanMessageListenerConcurrently = (msgs, context) -> {
				if (!msgs.isEmpty()) {
					msgs.stream().forEach(msg -> {
						final String body = new String(msg.getBody());
						try {
							log.info("id-->" + msg.getMsgId() + "\tbody-->" + body);
							ResultBean resultBean = JSONObject.parseObject(body, ResultBean.class);
							if (null != resultBean) {
								upload(resultBean.getTaskId(), resultBean);
							}
						} catch (Exception e) {
							log.error("error", e);
						}
					});
				}
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			};
			consumer.registerMessageListener(titanMessageListenerConcurrently);
			consumer.start();
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	/**
	 * 获取业务指标数据
	 * 
	 * @author gaoxianglong
	 */
	private void getMonitorIndicator() {
		DefaultMQPushConsumer consumer = monitorIndicator.getConsumer();
		try {
			consumer.subscribe(monitorIndicator.getRocketTopic(), "*");
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			MessageListenerConcurrently titanMessageListenerConcurrently = (msgs, context) -> {
				if (!msgs.isEmpty()) {
					msgs.stream().forEach(msg -> {
						final String body = new String(msg.getBody());
						try {
							log.info("id-->" + msg.getMsgId() + "\tbody-->" + body);
							if (null != body) {
								MonitorBean monitorBean = JSONObject.parseObject(body, MonitorBean.class);
								if (null != monitorBean) {
									uploadData.uploadMontor(monitorBean);
								}
							}
						} catch (Exception e) {
							log.error("error", e);
						}
					});
				}
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			};
			consumer.registerMessageListener(titanMessageListenerConcurrently);
			consumer.start();
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	/**
	 * 获取性能指标数据
	 * 
	 * @author gaoxianglong
	 */
	private void getPerformanceIndicatorIndicator() {
		DefaultMQPushConsumer consumer = performanceIndicator.getConsumer();
		try {
			consumer.subscribe(performanceIndicator.getRocketPerformanceTopic(), "*");
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			MessageListenerConcurrently titanMessageListenerConcurrently = (msgs, context) -> {
				if (!msgs.isEmpty()) {
					msgs.stream().forEach(msg -> {
						final String body = new String(msg.getBody());
						try {
							log.info("id-->" + msg.getMsgId() + "\tbody-->" + body);
							if (null != body) {
								Object json=JSONObject.parseObject(body);
								PerformanceDataBean pd=(PerformanceDataBean)JSONObject.toJavaObject((JSON)json, PerformanceDataBean.class);
								if (null != pd) {
									uploadPerformance(pd.getTaskId(), pd);
								}
								
							}
						} catch (Exception e) {
							log.error("error", e);
						}
					});
				}
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			};
			consumer.registerMessageListener(titanMessageListenerConcurrently);
			consumer.start();
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	/**
	 * 执行数据上报
	 * 
	 * @author gaoxianglong
	 */
	private synchronized void upload(final String taskId, ResultBean resultBean) {
		if (!resultMap.containsKey(taskId)) {
			log.info("taskId-->" + taskId + "\tagentSize-->" + resultBean.getAgentSize());
			final CountDownLatch latch = new CountDownLatch(resultBean.getAgentSize());
			countDownLatchMap.put(taskId, latch);
			threadPoolManager.getThreadPool().execute(() -> {
				/* 执行数据上报 */
				try {
					uploadData.upload(taskId, countDownLatchMap, resultMap);
				} catch (Exception e) {
					log.error("error", e);
				}
			});
			List<ResultBean> list = new ArrayList<ResultBean>();
			list.add(resultBean);
			resultMap.put(taskId, list);
			latch.countDown();
		} else {
			resultMap.get(taskId).add(resultBean);
			CountDownLatch latch = countDownLatchMap.get(taskId);
			latch.countDown();
		}
	}

	/**
	 * 执行性能数据上报
	 * 
	 * @author gaoxianglong
	 */
	private synchronized void uploadPerformance(final String taskId, PerformanceDataBean pdb) {
		if (!resultPerformanceMap.containsKey(taskId)) {
			log.info("taskId-->" + taskId + "\tagentSize-->" + pdb.getAgentSize());
			final CountDownLatch latch = new CountDownLatch(pdb.getAgentSize());
			countDownLatchMap.put(taskId+"Performance", latch);
			threadPoolManager.getThreadPool().execute(() -> {
				/* 执行数据上报 */
				try {
					uploadData.uploadPerformance(taskId, countDownLatchMap, resultPerformanceMap);
				} catch (Exception e) {
					log.error("error", e);
				}
			});
			Map<String, List<RequestResultData>> rdatas=resultPerformanceMap.get(taskId);
			for(Entry<String, List<RequestResultData>> entry:pdb.getRequestDatas().entrySet()){
				if(rdatas.containsKey(entry.getKey())){
					rdatas.get(entry.getKey()).addAll(entry.getValue());
				}else{
					rdatas.put(entry.getKey(), entry.getValue());
				}
			}
			latch.countDown();
		} else {
			resultPerformanceMap.put(taskId,pdb.getRequestDatas());
			CountDownLatch latch = countDownLatchMap.get(taskId+"Performance");
			latch.countDown();
		}
	}
	
	public static void main(String[] args) {
		PerformanceDataBean pdb=new PerformanceDataBean();
		pdb.setSceneId(1);
		pdb.setTaskId("1133");
		Map<String,List<RequestResultData>> obj=new HashMap();
		pdb.setRequestDatas(obj);
		List<RequestResultData> list=new ArrayList();
		obj.put("www.baidu.com", list);
		RequestResultData r=new RequestResultData();
		r.setStartTime(0l);
		r.setStopTime(100l);
		r.setSuccess(true);
		list.add(r);
		r=new RequestResultData();
		r.setStartTime(10l);
		r.setStopTime(200l);
		r.setSuccess(true);
		list.add(r);
		String performanceMsg=JSONObject.toJSONString(pdb);
		System.out.println(performanceMsg);
		Object json=JSONObject.parseObject(performanceMsg);
		System.out.println(json.getClass().toString());
		PerformanceDataBean pd=(PerformanceDataBean)JSONObject.toJavaObject((JSON)json, PerformanceDataBean.class);
		Map<String,List<RequestResultData>> map=pd.getRequestDatas();
		List<RequestResultData > li=map.get("www.baidu.com");
		for(RequestResultData  d:li){
			System.out.println(d.isSuccess());
		}
		System.out.println(li.size());
		
	}
}