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
package com.yunji.titan.manager.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mysql.jdbc.Statement;
import com.yunji.titan.manager.bo.LinkBO;
import com.yunji.titan.manager.entity.Link;
import com.yunji.titan.manager.entity.LinkVariable;
import com.yunji.titan.manager.entity.mapper.LinkMapper;
import com.yunji.titan.manager.entity.mapper.LinkVariableMapper;

/**
 * @desc 链路表Dao
 *
 * @author liuliang
 *
 */
@Repository
public class LinkDao {

    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Resource
	private LinkMapper linkMapper;
    @Resource
	private LinkVariableMapper linkVariableMapper;
    
    /**
     * @desc 查询链路总数量
     *
     * @author liuliang
     *
     * @return
     * @throws Exception
     */
    public int queryLinkCount() throws Exception {
        final String sql = "SELECT count(*) FROM t_link";
        return jdbcTemplate.queryForObject(sql,Integer.class);
    }
    
    /**
  	 * @desc 查询符合条件的记录总数
  	 *
  	 * @author liuliang
  	 *
  	 * @param linkName 链路名
  	 * @return int 符合条件的记录总数
  	 */
	public int queryLinkCount(String linkName) throws Exception{
		final String sql = "SELECT count(*) FROM t_link WHERE link_name LIKE ?";
		linkName = "%" + linkName + "%";
	    return jdbcTemplate.queryForObject(sql,new Object[]{linkName},Integer.class);
	}
  
    /**
     * @desc 分页查询所有链路
     *
     * @author liuliang
     *
     * @param pageIndex 当前页数
     * @param pageSize 每页记录条数
     * @return List<Link>
     */
    public List<Link> queryLinkByPage(int pageIndex,int pageSize) throws Exception{
    	int offset =  pageIndex * pageSize;
    	final String sql = "SELECT * FROM t_link ORDER BY create_time DESC limit ?,?";
        return jdbcTemplate.query(sql,new Object[]{offset,pageSize},linkMapper);
    }

    /**
	 * @desc 分页查询所有链路列表
	 *
	 * @author liuliang
	 *
	 * @param pageIndex 当前页
	 * @param pageSize 每页条数
	 * @return List<Link> 链路实体集合
	 * @throws Exception
	 */
	public List<Link> queryLinkByPage(String linkName, int pageIndex,int pageSize) throws Exception{
		int offset =  pageIndex * pageSize;
		linkName = "%" + linkName + "%";
    	final String sql = "SELECT * FROM t_link WHERE link_name LIKE ? ORDER BY create_time DESC limit ?,?";
        return jdbcTemplate.query(sql,new Object[]{linkName,offset,pageSize},linkMapper);
	}

	/**
	 * @desc 增加链路记录
	 *
	 * @author liuliang
	 *
	 * @param linkBO 链路参数BO
	 * @return int 新增link主键值
	 * @throws Exception
	 */
	public int addLink(LinkBO linkBO) throws Exception{
		final String sql = "INSERT INTO t_link(link_name,protocol_type,stresstest_url,request_type,content_type,charset_type,testfile_path,create_time,modify_time,success_expression) VALUES(?,?,?,?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, linkBO.getLinkName());
                ps.setInt(2, linkBO.getProtocolType());
                ps.setString(3, linkBO.getStresstestUrl());
                ps.setInt(4, linkBO.getRequestType());
                ps.setInt(5, linkBO.getContentType());
                ps.setInt(6,linkBO.getCharsetType());
                ps.setString(7,linkBO.getTestfilePath());
                ps.setLong(8,System.currentTimeMillis());
                ps.setLong(9,System.currentTimeMillis());
                ps.setString(10,linkBO.getSuccessExpression());
                return ps;
			}
           
        }, keyHolder); 
//		String sql = "INSERT INTO t_link(link_name,protocol_type,stresstest_url,request_type,content_type,charset_type,testfile_path,create_time,modify_time,success_expression) VALUES(?,?,?,?,?,?,?,?,?,?)";
//		return jdbcTemplate.update(sql,new Object[]{linkBO.getLinkName(),linkBO.getProtocolType(),linkBO.getStresstestUrl(),linkBO.getRequestType(),linkBO.getContentType(),linkBO.getCharsetType(),linkBO.getTestfilePath(),System.currentTimeMillis(),System.currentTimeMillis(),linkBO.getSuccessExpression()});
        return keyHolder.getKey().intValue();
	}

	/**
	 * @desc 更新链路记录
	 *
	 * @author liuliang
	 *
	 * @param linkBO 链路参数BO
	 * @return int 受影响的记录数
	 * @throws Exception
	 */
	public int updateLink(LinkBO linkBO) throws Exception{
		String sql = "UPDATE t_link SET protocol_type = ?,stresstest_url = ?,request_type = ?,content_type = ?,charset_type = ?,testfile_path = ?,modify_time = ?,success_expression = ? WHERE link_id = ?";
		return jdbcTemplate.update(sql,new Object[]{linkBO.getProtocolType(),linkBO.getStresstestUrl(),linkBO.getRequestType(),linkBO.getContentType(),linkBO.getCharsetType(),linkBO.getTestfilePath(),System.currentTimeMillis(),linkBO.getSuccessExpression(),linkBO.getLinkId()});
	}

	/**
	 * @desc 删除链路记录
	 *
	 * @author liuliang
	 *
	 * @param idList 链路ID(多个ID以英文","隔开)
	 * @return int 受影响的记录数
	 * @throws Exception
	 */
	public int removeLink(String idList) throws Exception{
		String sql = "DELETE FROM t_link WHERE link_id IN (" + idList + ")";
		return jdbcTemplate.update(sql);
	}

	/**
	 * @desc 根据ID查询链路详情
	 *
	 * @author liuliang
	 *
	 * @param linkId 链路ID
	 * @return Link 链路实体
	 * @throws Exception
	 */
	public Link getLink(long linkId) throws Exception{
		String sql = "SELECT * FROM t_link WHERE link_id = ?";
		List<Link> dataList = jdbcTemplate.query(sql,new Object[]{linkId},linkMapper);
		if((null != dataList) && (0 < dataList.size())){
			return dataList.get(0);
		}else{
			return null;
		}
	}

	/**
	 * @desc 根据链路ID查询链路列表
	 *
	 * @author liuliang
	 *
	 * @param ids 链路ID (多个ID以英文","隔开)
	 * @return List<Link> 链路实体集合
	 * @throws Exception
	 */
	public List<Link> getLinkListByIds(String ids) throws Exception{
		final String sql = "SELECT * FROM t_link WHERE link_id IN (" + ids + ")";
        return jdbcTemplate.query(sql,linkMapper);
	}

	/**
	 * @desc 根据链路ID查询链路变量定义列表
	 *
	 * @author liuliang
	 *
	 * @param ids 链路ID (多个ID以英文","隔开)
	 * @return List<Link> 链路实体集合
	 * @throws Exception
	 */
	public List<LinkVariable> getLinkVariableByIds(String ids) throws Exception{
		final String sql = "SELECT * FROM t_link_variable WHERE link_id IN (" + ids + ")";
        return jdbcTemplate.query(sql,linkVariableMapper);
	}

	/**
	 * @desc 增加链路输出属性
	 *
	 * @throws Exception
	 */
	public int addLinkVariable(LinkVariable linkVarBO) throws Exception{
		String sql = "INSERT INTO t_link_variable(link_id,stresstest_url,var_name,var_expression,create_time,modify_time) VALUES(?,?,?,?,?,?)";
		return jdbcTemplate.update(sql,new Object[]{linkVarBO.getLinkId(),linkVarBO.getStresstestUrl(),linkVarBO.getVarName(),linkVarBO.getVarExpression(),System.currentTimeMillis(),System.currentTimeMillis()});
	}

	/**
	 * @desc 删除链路输出属性
	 *
	 * @throws Exception
	 */
	public int removeLinkVariableByLinkId(String ids) {
		return jdbcTemplate.update("DELETE FROM t_link_variable WHERE link_id in (" + ids + ")");
	}

	/**
	 * @desc 根据url查link
	 * @param url
	 * @return
	 */
	public List<Link> getLinkListByUrl(String url) {
		final String sql = "SELECT * FROM t_link WHERE stresstest_url = ?";
        return jdbcTemplate.query(sql,new Object[]{url},linkMapper);
	}
}
