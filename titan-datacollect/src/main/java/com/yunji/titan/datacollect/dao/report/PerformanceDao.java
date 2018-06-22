package com.yunji.titan.datacollect.dao.report;

import com.yunji.titan.datacollect.bean.po.PerformanceDetailPO;
import com.yunji.titan.datacollect.bean.po.PerformancePO;

public interface PerformanceDao {

	/**
	 * 新增压测性能信息
	 * 
	 * @author gaoxianglong
	 * 
	 * @param reportPO
	 *            报表实体Bean
	 * 
	 * @return void
	 */
	public void insertPerformance(PerformancePO performancePO);

	/**
	 * 新增压测性能清单
	 * 
	 * @author gaoxianglong
	 * 
	 * @param reportPO
	 *            报表实体Bean
	 * 
	 * @return void
	 */
	public void insertPerformanceDetail(PerformanceDetailPO performanceDetailPO);
}
