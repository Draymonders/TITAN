package com.yunji.titan.agent.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yunji.titan.utils.RequestResultData;

public class DataCollector {
	
	private static Map<String,List<RequestResultData>> datas=new ConcurrentHashMap(); 

	public static void add(String url,RequestResultData data){
		getList(url).add(data);
	}
	public static void add(String url,List<RequestResultData> data){
		getList(url).addAll(data);
	}
	public static void clear(){
		datas.clear();
	}
	private static List<RequestResultData> getList(String url){
		List<RequestResultData> result=datas.get(url);
		if(result==null){
			result=new ArrayList();//new CopyOnWriteArrayList();
			datas.put(url, result);
		}
		return result;
	}
	public static Map<String,List<RequestResultData>> getData(){
		return datas;
	}
}
