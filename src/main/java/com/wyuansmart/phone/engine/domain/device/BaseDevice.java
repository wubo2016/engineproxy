package com.wyuansmart.phone.engine.domain.device;

import com.wyuansmart.phone.common.util.DateUtil;
import com.wyuansmart.phone.engine.domain.data.IDBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class BaseDevice extends IDBase {
	private static Logger logger=LoggerFactory.getLogger(BaseDevice.class);
	//设备访问地址
	private String url="";	
	//设备访问端口
	private int port;
	
    //
	private Date created = Calendar.getInstance(Locale.getDefault()).getTime();
	private Date updated = Calendar.getInstance(Locale.getDefault()).getTime();
	
	/**
	 * 0 不在线，1在线 其它值味知
	 */
	private int status = -1;
	
	public BaseDevice(){
		setLoadTime(Calendar.getInstance(Locale.getDefault()));
	}
	
	/**
	 * 加载时间
	 */
	private Calendar loadTime;


	public Calendar getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(Calendar loadTime) {
		this.loadTime = loadTime;
	}
	
	public boolean isOld(){
		Calendar cal = getLoadTime();
		Calendar creCalendar = Calendar.getInstance(Locale.getDefault());
		if (creCalendar.compareTo(cal) < 0) {
			//时间发生了修改
			setLoadTime(creCalendar);
			return false;
		}
		
		creCalendar.add(Calendar.SECOND, -600);
		if (cal.compareTo(creCalendar) < 0) {
			//加载已经超过10分钟
			return true;
		}else {
			return false;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url != null) {
			this.url = url;
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		if (port <= 0 || port > 65536) {
			return;
		}
		this.port = port;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		
		if (object == this) {
			return true;
		}
		
		if (object instanceof BaseDevice) {
			BaseDevice obj = (BaseDevice)object;
			if (getId() != null && !getId().equals(obj.getId())) {
				logger.info(getId()+ ",id different, 1:"+getId()+",2:"+obj.getId());
				return false;
			}else if (getId() == null && obj.getId() != null) {
				logger.info(getId()+ ",id different, 1:"+getId()+",2:"+obj.getId());
				return false;
			}else if (getName() != null && !getName().equals(obj.getName())) {
				logger.info(getId()+ ",Name different, 1:"+getName()+",2:"+obj.getName());
				return false;
			}else if (getName() == null && obj.getName() != null) {
				logger.info(getId()+ ",Name different, 1:null,2:"+obj.getName());
				return false;
			}else if (getUrl() != null && !getUrl().equals(obj.getUrl())) {
				logger.info(getId()+ ",url different, 1:"+getUrl()+",2:"+obj.getUrl());
				return false;
			}else if (getUrl() == null && obj.getUrl() != null) {
				logger.info(getId()+ ",Url different, 1:null,2:"+obj.getUrl());
				return false;
			}else if (getPort() != obj.getPort()) {
				logger.info(getId()+ ",Port different, 1:"+getPort()+",2:"+obj.getPort());
				return false;
			}else {
				return true;
			}
		}else{
			return false;
		}

	}
	
	public void set(BaseDevice device) {
		setName(device.getName());
		setId(device.getId());
		setLoadTime(device.getLoadTime());
		setPort(device.getPort());
		setUrl(device.getUrl());
	}
	
	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Id:"+getId());
		strBuffer.append("	Name:"+getName());
		strBuffer.append("	IP:"+getUrl());
		strBuffer.append(" Port:"+getPort());
		return strBuffer.toString();
	}

	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public void setCreated(String created) {
		try {
			this.created = DateUtil.convertStringToDate(created);
		} catch (Exception e) {
			// TODO: handle exception
			
		}
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getUpdated() {
		return updated;
	}
	
	public String getUpdatedString() {
		return DateUtil.convertDateToString(updated);
	}
	
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
	public String getgetCreatedString() {
		return DateUtil.convertDateToString(created);
	}
	
	public void setUpdated(String updated) {
		try {
			this.updated = DateUtil.convertStringToDate(updated);
		} catch (Exception e) {
			// TODO: handle exception
			
		}
	}
}
