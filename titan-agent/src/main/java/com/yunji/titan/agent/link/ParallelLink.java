package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.yunji.titan.agent.bean.bo.OutParamBO;
import com.yunji.titan.agent.stresstest.Stresstest;
import com.yunji.titan.utils.ThreadPoolManager;

public class ParallelLink implements Link{

	private Logger logger = LoggerFactory.getLogger(ParallelLink.class);
	private List<Link> links=new ArrayList();
	private CountDownLatch latch ;
	private ThreadPoolManager threadPoolManager;
	@Override
	public StressTestResult execute(StressTestContext stc) {
		logger.info("--start ParallelLink");
		latch= new CountDownLatch(links.size());
		StressTestResult result=new StressTestResult();
		for(Link link :links){
			threadPoolManager.getThreadPool().execute(() -> {
				StressTestResult r=link.execute(stc);
				if(!r.isSuccess()){
					result.setSuccess(false);
				}
				latch.countDown();
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		logger.info("--end ParallelLink");
		return result;
	}

	@Override
	public void addLink(Link link) {
		links.add(link);
	}

	@Override
	public void setLinkIds(String ids) {
		// TODO Auto-generated method stub
		
	}

	public ThreadPoolManager getThreadPoolManager() {
		return threadPoolManager;
	}

	public void setThreadPoolManager(ThreadPoolManager threadPoolManager) {
		this.threadPoolManager = threadPoolManager;
	}

}
