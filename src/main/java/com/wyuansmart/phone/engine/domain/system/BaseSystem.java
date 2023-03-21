package com.wyuansmart.phone.engine.domain.system;

import com.wyuansmart.phone.common.server.protobuf.NetMsgSession;
import com.wyuansmart.phone.engine.domain.device.BaseDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSystem extends BaseDevice {
	private static Logger logger=LoggerFactory.getLogger(BaseSystem.class);
	
	/**
	 *系统类型 见SystemType
	 */
	private int type; 
	
	/**
	 *登录系统的用户名
	 */
	private String userName = "";
	
	/**
	 * 登录系统的密码
	 */
	private String password ="";
	
	
	private NetMsgSession session;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public NetMsgSession getTcpSession() {
		return session;
	}

	public void setTcpSession(NetMsgSession session) {
		this.session = session;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
        if (userName != null) {
    		this.userName = userName;	
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
        if (password != null) {
    		this.password = password;	
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		
		if (object == this) {
			return true;
		}
		
		if (!super.equals(object)) {
			return false;
		}
		
		if (object instanceof BaseSystem) {
			BaseSystem obj = (BaseSystem)object;
			if (getUserName() != null && !getUserName().equals(obj.getUserName())) {
				logger.info(getId()+ ",user_name different, 1:"+getUserName()+",2:"+obj.getUserName());
				return false;
			}else if (getUserName() == null && obj.getUserName() != null) {
				logger.info(getId()+ "user_name different, 1:null,2:"+obj.getUserName());
				return false;
			}else if (getPassword() != null && !getPassword().equals(obj.getPassword())) {
				logger.info(getId()+ "Password different, 1:"+getPassword()+",2:"+obj.getPassword());
				return false;
			}else if (getPassword() == null && obj.getPassword() != null) {
				logger.info(getId()+ "Password different, 1:null,2:"+obj.getPassword());
				return false;
			}else if (getType() != obj.getType()) {
				logger.info(getId()+ "Type different, 1:"+getType()+",2:"+obj.getType());
				return false;
			}else {
				return true;
			}
		}else{
			return false;
		}

	}

	public void set(BaseSystem info) {
		if (info == null) {
			return;
		}
		
		super.set(info);
		setCreated(info.getCreated());
		setUpdated(info.getUpdated());
		setPassword(info.getPassword());
		setType(info.getType());
		setUserName(info.getUserName());
	}

}
