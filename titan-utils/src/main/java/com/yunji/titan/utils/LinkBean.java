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

import java.util.ArrayList;
import java.util.List;

public class LinkBean {
	/**
	 * 主键自增ID
	 */
	private Long linkId;
//	private String url;
	private List<String> linkScope = new ArrayList<String>();
	public Long getLinkId() {
		return linkId;
	}
	public void setLinkId(Long linkId) {
		this.linkId = linkId;
	}
	public List<String> getLinkScope() {
		return linkScope;
	}
	public void setLinkScope(List<String> linkScope) {
		this.linkScope = linkScope;
	}
	
	
//	public String getUrl() {
//		return url;
//	}
//	public void setUrl(String url) {
//		this.url = url;
//	}
	public boolean contain(String scope){
		for(String s:linkScope){
			if(s.equals(scope)){
				return true;
			}
		}
		return false;
	}
}