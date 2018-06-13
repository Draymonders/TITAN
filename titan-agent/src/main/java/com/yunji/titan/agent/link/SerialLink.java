package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialLink  implements Link{
	
	private Logger logger = LoggerFactory.getLogger(SerialLink.class);
	private List<Link> links=new ArrayList();

	@Override
	public StressTestResult execute(StressTestContext stc) {
		StressTestResult result=new StressTestResult();
//		logger.info("--start SerialLink"+result.toString().split("@")[1]);
		for(Link link :links){
			StressTestResult r=link.execute(stc);
			if(!r.isSuccess()){
				result.setSuccess(false);
				break;
			}
		}
//		logger.info("--end SerialLink"+result.toString().split("@")[1]);
		return result;
	}

	@Override
	public void addLink(Link link) {
		links.add(link);
	}

	@Override
	public void collectData() {
		for(Link link :links){
			link.collectData();
		}
	}


}
