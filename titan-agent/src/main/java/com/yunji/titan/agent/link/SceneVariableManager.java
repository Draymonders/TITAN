package com.yunji.titan.agent.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

public class SceneVariableManager {

	private static Logger logger = Logger.getLogger(SceneVariableManager.class);
	//场景全局变量,场景n个用户并发,这些用户共享的变量、变量值
	private Map<String,List<Map<String,String>>> sceneVarValue=new HashMap();

	public synchronized void add(String url,Map<String,String> map){
		List<Map<String,String>> list=sceneVarValue.get(url);
		if(list==null){
			list=new ArrayList();
			sceneVarValue.put(url, list);
		}
		list.add(map);
	}
	
	public synchronized String getVarValue(String key){
		String value=null;
		//遍历各个url产生的变量
		for(Entry<String,List<Map<String,String>>> e:sceneVarValue.entrySet()){
			//这个url产生的变量组大小
			int size=e.getValue().size();
			Random rand = new Random();
			//从变量组中随机取一个变量集合map
			int m=rand.nextInt(size);
			Map<String,String> map=e.getValue().get(m);
			//变量集合是否包含该key
			if(map.containsKey(key)){
				value=map.get(key);
				break;
			}
		}
		return value;
	}
	/**
	 * 整合各个url产生的变量，同一个url随机抽取一个变量、变量值的map
	 * 要求各个url的变量不能同名
	 * @return
	 */
	public synchronized Map<String,String> mapVariableValue(){
		Map<String,String> result=new HashMap();
		for(Entry<String,List<Map<String,String>>> e:sceneVarValue.entrySet()){
			//一个个url产生的变量组大小
			int size=e.getValue().size();
			Random rand = new Random();
			//从变量组中随机取一个变量集合map
			int m=rand.nextInt(size);
			Map<String,String> map=e.getValue().get(m);
			result.putAll(map);
		}
		return result;
	}

}
