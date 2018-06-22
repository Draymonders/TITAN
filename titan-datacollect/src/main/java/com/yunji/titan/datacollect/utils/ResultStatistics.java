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
package com.yunji.titan.datacollect.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yunji.titan.datacollect.bean.po.PerformanceDetailPO;
import com.yunji.titan.datacollect.bean.po.PerformancePO;
import com.yunji.titan.datacollect.bean.po.ReportPO;
import com.yunji.titan.utils.RequestResultData;
import com.yunji.titan.utils.ResultBean;

/**
 * 压测结果统计
 * 
 * @author gaoxianglong
 */
public class ResultStatistics {
	private static Logger log = LoggerFactory.getLogger(ResultStatistics.class);

	public static ReportPO result(List<ResultBean> reportPOs) {
		ReportPO reportEntity = null;
		if (reportPOs.isEmpty()) {
			return null;
		}
		reportEntity = new ReportPO();
		ResultBean resultBean = reportPOs.get(0);
		try {
			int httpSuccessNum = (int) reportPOs.stream().mapToInt(ResultBean::getHttpSuccessNum).summaryStatistics()
					.getSum();
			int serviceSuccessNum = (int) reportPOs.stream().mapToInt(ResultBean::getServiceSuccessNum)
					.summaryStatistics().getSum();
			/* 寻找各个agent节点中开始时间最早的 */
			long startTime = reportPOs.stream().mapToLong(ResultBean::getStartTime).summaryStatistics().getMin();
			/* 寻找各个agent节点中结束时间最晚的 */
			long endTime = reportPOs.stream().mapToLong(ResultBean::getEndTime).summaryStatistics().getMax();
			long continuedTime = endTime - startTime;
			reportEntity.setSceneId(resultBean.getSenceId());
			reportEntity.setSceneName(resultBean.getSenceName());
			reportEntity.setExpectTps(resultBean.getExpectThroughput());
			reportEntity.setStartTime(startTime);
			reportEntity.setEndTime(endTime);
			reportEntity.setSuccessRequest(httpSuccessNum);
			reportEntity.setBusinessSuccessRequest(serviceSuccessNum);
			reportEntity.setConcurrentUser(resultBean.getConcurrentUsers() * resultBean.getAgentSize());
			reportEntity.setCreateTime(System.currentTimeMillis());
			reportEntity.setReportName(reportEntity.getSceneName() + "_" + reportEntity.getCreateTime());
			reportEntity.setTotalRequest(resultBean.getTaskSize() * reportEntity.getConcurrentUser());
			/* 吞吐量,单位秒 */
			reportEntity.setActualTps((int) (reportEntity.getTotalRequest() / ((double) continuedTime / 1000)));
			/*
			 * 压测结果,满足3个条件(HTTP200成功请求数>=(总并发请求数/2)&&业务成功率>=(总并发请求数/2)&&实际吞吐量>=
			 * 期待吞吐量)
			 */
			reportEntity.setConclusion(reportEntity.getActualTps() >= reportEntity.getExpectTps()
					&& httpSuccessNum >= (reportEntity.getTotalRequest() / 2)
					&& serviceSuccessNum >= (reportEntity.getTotalRequest() / 2) ? 0 : 1);
			/*
			 * 服务器平均请求等待时间,计算公式:处理完成所有请求数所花费的时间 / 总请求数:即Time taken for /
			 * testsComplete requests,单位毫秒
			 */
			reportEntity.setServerWaittime(continuedTime / reportEntity.getTotalRequest());
			/*
			 * 用户平均请求等待时间,计算公式:处理完成所有请求数所花费的时间/(总请求数 / 并发用户数),即 Time per request
			 * = Time taken for tests / (Complete requests / Concurrency
			 * Level),单位毫秒
			 */
			reportEntity.setUserWaittime(
					continuedTime / (reportEntity.getTotalRequest() / reportEntity.getConcurrentUser()));
		} catch (ArithmeticException e) {
			log.debug("error", e);
		}
		return reportEntity;
	}
	public static void result(PerformancePO performancePO,List<PerformanceDetailPO> details,
			Map<String, List<RequestResultData>> datas) {
		long min=calMin(datas);
		for(Entry<String, List<RequestResultData>> entry:datas.entrySet()){
			String url=entry.getKey();
			List<RequestResultData> list=entry.getValue();
			for(RequestResultData r:list){
				PerformanceDetailPO d=new PerformanceDetailPO();
				d.setUrl(url);
				d.setMessage(r.getErrorMessage());
				d.setPerformanceId(performancePO.getId());
				d.setStart(r.getStartTime()-min);
				d.setStop(r.getStopTime()-min);
				d.setSuccess(r.isSuccess());
				int x_time=(int)(r.getStartTime()-min)/1000;
				d.setX_time(x_time);
				details.add(d);
			}
		}
	}
	
	private static long calMin(Map<String, List<RequestResultData>> datas){
		long min=999999999999999999l;
		for(Entry<String, List<RequestResultData>> entry:datas.entrySet()){
			String url=entry.getKey();
			List<RequestResultData> list=entry.getValue();
			for(RequestResultData r:list){
				if(r.getStartTime()<min){
					min=r.getStartTime();
				}
			}
		}
		return min;
	}

	public static void main(String[] args) {
		long b1=20000;
		long b2=20500;
		long b3=21500;
		long b4=21700;
		long b5=22700;
		long r=(b2-b1)/1000;
		System.out.println(r);
		r=(b3-b1)/1000;
		System.out.println(r);
		r=(b4-b1)/1000;
		System.out.println(r);
		r=(b5-b1)/1000;
		System.out.println(r);
	}

}