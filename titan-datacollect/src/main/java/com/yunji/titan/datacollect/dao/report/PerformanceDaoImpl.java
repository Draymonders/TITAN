package com.yunji.titan.datacollect.dao.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.yunji.titan.datacollect.bean.po.PerformanceDetailPO;
import com.yunji.titan.datacollect.bean.po.PerformancePO;

@Repository
public class PerformanceDaoImpl implements PerformanceDao{

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Override
	public void insertPerformance(PerformancePO po) {
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "INSERT INTO t_performance(report_name,scene_id,start_time,stop_time,create_time) "
				+ "values(?,?,?,?,?)";
		jdbcTemplate.update(  
	            new PreparedStatementCreator() {  
	                public PreparedStatement createPreparedStatement(Connection con) throws SQLException  
	                {  
	                    PreparedStatement ps = jdbcTemplate.getDataSource()  
	                            .getConnection().prepareStatement(sql,new String[]{ "report_name" ,"scene_id",
	                            		"start_time","stop_time","create_time"});  
	                    ps.setString(1, po.getReportName());  
	                    ps.setInt(2, po.getSceneId());  
	                    ps.setLong(3, po.getStartTime());  
	                    ps.setLong(4, po.getStopTime());  
	                    ps.setLong(5, System.currentTimeMillis()); 
	                    return ps;  
	                }  
	            }, keyHolder);  

        po.setId(keyHolder.getKey().intValue());
        
	}

	@Override
	public void insertPerformanceDetail(PerformanceDetailPO po) {
		String sql = "INSERT INTO t_performance_detail(performance_id,start,stop,url,x_time,message,success) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(sql,
				new Object[] { po.getPerformanceId(),po.getStart(),po.getStop(),po.getUrl(),po.getX_time(),po.getMessage()
						,po.isSuccess()});
	}
}
