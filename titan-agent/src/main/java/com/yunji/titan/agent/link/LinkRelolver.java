package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.List;

import com.yunji.titan.utils.ThreadPoolManager;

public class LinkRelolver {
	private ThreadPoolManager threadPoolManager;//=new ThreadPoolManager()
	public Link relover(String expression){
//		threadPoolManager.setCorePoolSize(2000);
//		threadPoolManager.setMaximumPoolSize(2000);
//		threadPoolManager.setKeepAliveTime(5);
//		threadPoolManager.setCapacity(1000);
//		threadPoolManager.init();
		expression=rmchar(expression);
		Link rootLink=new SerialLink();
		Link curLink=null;
		List<String> list=new ArrayList();
		while(true){
			int start=expression.indexOf("[");
			int end=expression.indexOf("]");
			//start>0说明并发请求前有串行的请求 如 3,5[7,8]
			if(start>0){
				//截取串行请求的ids eg: 3,5
				String ids=expression.substring(0,start);
				//去掉左右的 , 字符
				ids=rmchar(ids);
				list.add(ids);
				curLink=new SerialLink();
				curLink.setLinkIds(ids);
				this.addUrlLink(ids,curLink);
				rootLink.addLink(curLink);
			}
			if(start>=0){
				//截取并行的请求 ids
				String ids=expression.substring(start+1, end);
				ids=rmchar(ids);
				list.add(ids);
				ParallelLink pLink=new ParallelLink();
				pLink.setThreadPoolManager(threadPoolManager);
				pLink.setLinkIds(ids);
				rootLink.addLink(pLink);
				this.addUrlLink(ids,pLink);
				//去掉已经加入list的ids
				expression=expression.substring(end+1);
			}else{//说明没有并发请求标志([)了 
				String ids=rmchar(expression);
				list.add(ids);
				curLink=new SerialLink();
				curLink.setLinkIds(ids);
				rootLink.addLink(curLink);
				this.addUrlLink(ids,curLink);
				break;
			}
		}
		return rootLink;
	}
	private void addUrlLink(String ids,Link link){
		String[] list=ids.split(",");
		for(int i=0;i<list.length;i++){
			UrlLink urlLink=new UrlLink();
			urlLink.setUrl(list[i]);
			link.addLink(urlLink);
		}
	}
	private String rmchar(String str){
		String c1=""+str.charAt(0);
		String c2=""+str.charAt(str.length()-1);
		if(",".equals(c1)){
			str=str.substring(1);
		}
		if(",".equals(c2)){
			str=str.substring(0,str.length()-1);
		}
		return str;
	}

	public ThreadPoolManager getThreadPoolManager() {
		return threadPoolManager;
	}
	public void setThreadPoolManager(ThreadPoolManager threadPoolManager) {
		this.threadPoolManager = threadPoolManager;
	}
	public static void main(String[] args) {
		LinkRelolver r=new LinkRelolver();
		Link link=r.relover("[1,5],8,9,[10,11],25,9");
		StressTestContext stc=new StressTestContext();
		link.execute(stc);
	}
}
