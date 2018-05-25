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
package com.yunji.titan.manager.entity;

/**
 * @desc 链路表实体,对应表[t_link]
 *
 * @author liuliang
 *
 */
public class LinkVariable {
	/**
	 * 主键自增ID
	 */
	private Long linkVariableId;
	
	/**
	 * 链路ID
	 */
	private Long linkId;
	
	/**
	 * 压测URL
	 */
	private String stresstestUrl;
	
	/**
	 * 变量名称
	 */
	private String varName;
	
	/**
	 * 变量取值表达式
	 */
	private String varExpression;
	
	/**
	 * 记录创建时间
	 */
	private Long createTime;
	/**
	 * 记录最后修改时间
	 */
	private Long modifyTime;
	public String getStresstestUrl() {
		return stresstestUrl;
	}
	public void setStresstestUrl(String stresstestUrl) {
		this.stresstestUrl = stresstestUrl;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public Long getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Long modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Long getLinkVariableId() {
		return linkVariableId;
	}
	public void setLinkVariableId(Long linkVariableId) {
		this.linkVariableId = linkVariableId;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getVarExpression() {
		return varExpression;
	}
	public void setVarExpression(String varExpression) {
		this.varExpression = varExpression;
	}
	public Long getLinkId() {
		return linkId;
	}
	public void setLinkId(Long linkId) {
		this.linkId = linkId;
	}
	
}