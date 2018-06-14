package com.yunji.titan.manager.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.yunji.titan.manager.bo.LinkBO;
import com.yunji.titan.manager.utils.FileUtil;
import com.yunji.titan.utils.ftp.FtpUtils;

@Service
public class HarLinkFileResolver implements ILinkFileResolver {
	

	/**
	 * ftp工具类
	 */
	@Resource
	private FtpUtils ftpUtils;
	
	private static final Integer DEFAULT_CHARSET_TYPE = 0;
	private static final String SUCCESS_EXPRESSION = "^.+$";

	@Override
	public List<LinkBO> resolve(File file) {
		String linksStr = FileUtil.readToString(file);
		String tempStr = JSON.parseObject(linksStr).getJSONObject("log").getString("entries");
		List<HarLink> harLinks = JSON.parseArray(tempStr, HarLink.class);
		
		List<LinkBO> result = new ArrayList<LinkBO>();
		for(HarLink hl : harLinks){
			LinkBO linkBO = new LinkBO();
			String url = hl.getRequest().getUrl();
			linkBO.setLinkName(url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("/") + 10));
			linkBO.setProtocolType("http".equals(url.substring(0,url.indexOf(":")))?0:1);
			linkBO.setStresstestUrl(url);
			linkBO.setSuccessExpression(SUCCESS_EXPRESSION);
			linkBO.setRequestType("GET".equals(hl.getRequest().getMethod().toUpperCase())?0:1);
			linkBO.setCharsetType(DEFAULT_CHARSET_TYPE);
			String contentTypeStr = JSON.parseObject(hl.getRequest().getPostData()).getString("mimeType").toLowerCase();
			linkBO.setContentType(ContentTypeEnum.getContentType(contentTypeStr).getCode());
			
			String params = JSON.parseObject(hl.getRequest().getPostData()).getString("params");
//			List<Map> parseArray = JSON.parseArray(params, Map.class);

			String fileName = genTestFile(params);
			linkBO.setTestfilePath(fileName);
			
			result.add(linkBO);
		}
		return result;
	}

	private String genTestFile(String params) {
		String fileName = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()) + ".xls";
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellValue(params);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			workbook.write(os);
			ftpUtils.uploadFile(new File(fileName),os.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				os.close();
				workbook.close();
			} catch (Exception e2) {
			}
		}
		return fileName;
	}

	public static void main(String[] args) {
		HarLinkFileResolver harLinkFileResolver = new HarLinkFileResolver();
		harLinkFileResolver.resolve(new File("C:\\Users\\戴文冠\\Desktop\\login.har"));
	}
}
