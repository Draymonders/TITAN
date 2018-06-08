/*
 * Copyright (C) 2015-2020 yunjiweidian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.yunji.titan.utils;

/**
 * Znode路径信息
 * 
 * @author gaoxianglong
 */
public class LinkScope {
	/**
	 * 请求参数数据不重复地分配给链路
	 */
	public static String PARAM_NONREPEAT = "param_nonrepeat";
	/**
	 * 一个场景下，所有并发用户，请求参数数据单循环执行一次
	 * 所有并发用户共享这些请求地输出参数
	 */
	public static String SCENE_ONELOOP = "scene_oneloop";
	/**
	 * 一个并发用户下，请求参数数据单循环执行一次
	 * 这个用户所有的请求(链路会被重复执行)共享这些请求地输出参数
	 */
	public static String USER_ONELOOP = "user_oneloop";
	
}
