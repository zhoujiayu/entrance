package com.ytsp.entrance.service.v5_0;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ExceptionLogDao;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.ExceptionLog;
import com.ytsp.entrance.command.base.CommandContext;

@Service("exceptionLogService")
@Transactional
public class ExceptionLogService {

	@Resource(name = "exceptionLogDao")
	private ExceptionLogDao exceptionLogDao;
	
	public void saveExceptionLog(ExceptionLog el) throws Exception{
		exceptionLogDao.save(el);
	}
	
	public List<ExceptionLog> getExceptionLog(){
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ytsp_exception_log ");
		sql.append(" ORDER BY exceptionTime desc ");
		return exceptionLogDao.sqlFetch(sql.toString(), ExceptionLog.class, 0, 20);
	}
	

}
