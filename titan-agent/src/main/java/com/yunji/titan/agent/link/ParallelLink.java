package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yunji.titan.utils.ThreadPoolManager;

public class ParallelLink implements Link{

	private Logger logger = LoggerFactory.getLogger(ParallelLink.class);
	private List<Link> links=new ArrayList();
	private CountDownLatch latch ;
	private ThreadPoolManager threadPoolManager;
	@Override
	public StressTestResult execute(StressTestContext stc) {
		latch= new CountDownLatch(links.size());
		List<StressTestContext> list=new ArrayList<StressTestContext>();
		StressTestResult result=new StressTestResult();
		logger.info("--start ParallelLink"+result.toString().split("@")[1]);
		for(Link link :links){
			threadPoolManager.getThreadPool().execute(() -> {
				//上一个链路的局部变量可供所有并发的链路使用，因此要copy一份局部变量到urlLink执行
				StressTestContext tmp=stc.copyLocalVarValue();
				list.add(tmp);
				StressTestResult r=link.execute(tmp);
				if(!r.isSuccess()){
					result.setSuccess(false);
				}
				latch.countDown();
			});
		}
		try {
			latch.await();
			//清空上一链接的局部变量
			stc.getLocalVarValue().clear();
			for(StressTestContext tmp:list){
				if(tmp.getLocalVarValue().size()>0){
					//汇总所有并发链路产生的局部变量
					stc.getLocalVarValue().putAll(tmp.getLocalVarValue());
				}
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
		logger.info("--end ParallelLink"+result.toString().split("@")[1]);
		return result;
	}

	@Override
	public void addLink(Link link) {
		links.add(link);
	}


	public ThreadPoolManager getThreadPoolManager() {
		return threadPoolManager;
	}

	public void setThreadPoolManager(ThreadPoolManager threadPoolManager) {
		this.threadPoolManager = threadPoolManager;
	}

}
