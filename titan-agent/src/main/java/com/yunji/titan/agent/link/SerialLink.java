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
		logger.info("--start SerialLink");
		StressTestResult result=new StressTestResult();
		for(Link link :links){
			StressTestResult r=link.execute(stc);
			if(!r.isSuccess()){
				result.setSuccess(false);
				break;
			}
		}
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

}
