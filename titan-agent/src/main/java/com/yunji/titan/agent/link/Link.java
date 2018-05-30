package com.yunji.titan.agent.link;

public interface Link {
	StressTestResult execute(StressTestContext stc);
	void addLink(Link link);
	void setLinkIds(String ids);
}
