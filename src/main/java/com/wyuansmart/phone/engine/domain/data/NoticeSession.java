package com.wyuansmart.phone.engine.domain.data;


import java.util.concurrent.locks.ReentrantLock;

import com.wyuansmart.phone.common.server.protobuf.NetMsgSession;
import com.wyuansmart.phone.common.server.protobuf.ProtobufMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 保存需要通知的 server id对应的session
 * @author wubo
 *
 */
public class NoticeSession {
    private String serverId="";
    private NetMsgSession session;
    private ReentrantLock sendLock = new ReentrantLock();
    
    public NoticeSession(String serverId,NetMsgSession session){
    	if (serverId != null) {
        	this.serverId = serverId;
		}
    	this.session = session;
    }

	public String getServerId() {
		return serverId;
	}
	

	public NetMsgSession getSession() {
		return session;
	}

	public void setSession(NetMsgSession session) {
		this.session = session;
	}
	
	/**
	 * 向该通知对像发送数据
	 * @param msg
	 */
	public void sendMsg(ProtobufMessage msg){
		try {
			msg.setSeq(0);
			if (!StringUtils.isEmpty(serverId)){
				msg.setTargetAddress(serverId);
			}
			if (session != null) {
				session.sendMessage(msg);
			}
		} finally {
			// TODO: handle finally clause
		}
	}
	
	/**
	 * 向该通知对像发送数据
	 * @param msg
	 */
	public ProtobufMessage sendWaitMsg(ProtobufMessage msg,int timeOut){
		try {
			msg.setSeq(0);
			msg.setTargetAddress(serverId);
			if (session != null && session.isConnected()) {
				return session.sendWaitNetMsg(msg,timeOut);
			}else {
				return null;
			}
		} finally {
			// TODO: handle finally clause
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		else if(object == null){
			return false;
		}
		else if (object instanceof NoticeSession) {
			NoticeSession temp = (NoticeSession) object;
			if (!getServerId().equals(temp.getServerId())) {
				return false;
			}else if (!getSession().equals(temp.getSession())) {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
	
	/**
	 * 通讯是否正常
	 * @return
	 */
	public boolean isConnected(){
		return session.isConnected();
	}
}
