package com.wyuansmart.phone.engine.domain.data;

import com.wyuansmart.phone.common.server.protobuf.NetMsgSession;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 注册管理类
 * @author wubo
 *
 */
public class RegisterBase {
	  /** 注册了某种消息类型的MAP，KEY为消息类型，值为：注册了该消息的 */
    protected Map<String, List<NoticeSession>> registerMap = new HashMap<String, List<NoticeSession>>();

    /** 注册了所有类型消息的 list */
    protected List<NoticeSession> allList = new ArrayList<NoticeSession>();

    protected ReentrantLock lock = new ReentrantLock();

    /**
     * 注册，把noticeSession保存起来，如果type为空保存到 allList中，不为空 保存到registerMap中。
     * 注意：noticeSession在allList有，在registerMap中的一定要删除。
     * 同一个list中只能出现一次。避免一个消息给同一个对象发送多次的情况
     * 
     * @param noticeSession 通知对像
     * @param deviceId 消息类型(设备ID)  deviceId
     * @return
     */
    public void register(NoticeSession noticeSession, String deviceId) {
        if (noticeSession == null) {
            return ;
        }

        try {
        	lock.lock();
            if (StringUtils.isEmpty(deviceId)) {
                if (!find(allList, noticeSession)) {
                    allList.add(noticeSession);
                }
                // /删除map中read
                removeRegisterMap(noticeSession);
            } else {
                // /如果已经加了全部消息，不再添加具体类型
                if (!find(allList, noticeSession)) {
                    List<NoticeSession> list = findRegisterMap(deviceId,true);
                    if (!find(list, noticeSession)) {
                        list.add(noticeSession);
                    }
                }
            }
		} finally {
			// TODO: handle finally clause
            lock.unlock();
		} 
        return ;
    }

    /**
     * 取消注册。type为空取消对所有消息的订阅，从allList中删除。
     * 
     * @param noticeSession
     * @param type
     * @return
     */
    public void unRegister(NoticeSession noticeSession, String type) {
        try {
        	lock.lock();
            // /清除指定ID的noticeSession；
            remove(type, noticeSession);

            // /删除MAP中的noticeSession；
            if (StringUtils.isEmpty(type)) {
                removeRegisterMap(noticeSession);
            }
		} finally {
			// TODO: handle finally clause
            lock.unlock();
		}

        return ;
    }
    
    /**
     * 取消该通讯对像的所有注册。
     * 
     * @param session 
     * @return
     */
    public void unRegister(NetMsgSession session) {
    	if (session == null) {
			return ;
		}
        try {
        	lock.lock();
        	removeSession(allList,session);
            for (List<NoticeSession> list : registerMap.values()) {
				removeSession(list, session);
			}
		} finally {
			// TODO: handle finally clause
			 lock.unlock();
		}

        return;
    }
    
    /**
     * 取消该发布对像的所有注册。
     * 
     * @param session 
     * @return
     */
    public void unRegister(NoticeSession session) {
    	if (session == null) {
			return ;
		}
        try {
        	lock.lock();
        	allList.remove(session);
            for (List<NoticeSession> list : registerMap.values()) {
            	list.remove(session);
			}
		} finally {
			// TODO: handle finally clause
            lock.unlock();
		}

        return ;
    }
    
    /**
     * 从list 移除 session对象
     * @param list
     * @param session
     */
    private void removeSession(List<NoticeSession> list,NetMsgSession session){
    	List<NoticeSession> removeList = new ArrayList<>();
        for (NoticeSession noticeSession2 : list) {
        	if(session.equals(noticeSession2.getSession())){
        		removeList.add(noticeSession2);
        	}
		}
        
        for (NoticeSession noticeSession2 : removeList) {
        	list.remove(noticeSession2);
		}
    }
    /**
     * 取得该类型类型消息所有的订阅者
     * @param type
     * @return
     */
    public List<NoticeSession> getRegisters(String type){
		List<NoticeSession> list = new ArrayList<>();
    	if (StringUtils.isEmpty(type)) {
    		try {
				lock.lock();
				list.addAll(allList);
				return list;
			} finally {
				// TODO: handle finally clause
                lock.unlock();
			}
		}else {
			try {
				lock.lock();
				List<NoticeSession> sessions = findRegisterMap(type, false);
				if (sessions != null) {
					list.addAll(sessions);
				}
				return list;
			} finally {
                lock.unlock();
			}
		}
    }

    /**
     * 查找session是否在list中。.
     * 
     * @param list
     * @param session
     * @return 存在返回真,不存在返回假
     */
    protected boolean find(List<NoticeSession> list, NoticeSession session) {
    	for (NoticeSession noticeSession : list) {
			if (session.equals(noticeSession)) {
				return true;
			}
		}
        return false;
    }

    /**
     * 查找该类型消息所在的list,从MAP中找。查到了返回
     * 
     * @param type
     * @param addList 为true没有找到增中list节点 返回
     * @return list
     */
    protected List<NoticeSession> findRegisterMap(String type,boolean addList) {
        if (StringUtils.isEmpty(type)) {
            return allList;
        }

        List<NoticeSession> list;
        list = registerMap.get(type);
        if (null == list) {
        	if (addList) {
                list = new ArrayList<>();
                registerMap.put(type, list);
			}
        }
        return list;
    }

    /**
     * 删除某种消息类型的，订阅。如果type为null，从allList删除。否则从registerMap中删除。
     * 
     * @param type
     * @param session
     * @return
     */
    protected void remove(String type, NoticeSession session) {
        if (StringUtils.isEmpty(type)) {
            allList.remove(session);
        } else {
            List<NoticeSession> list = findRegisterMap(type,false);
            if (null != list) {
                list.remove(session);
            }
        }
        return ;
    }

    /**
     * 从map的所有list中删除pRead.
     * 
     * @param session
     * @return
     */
    protected int removeRegisterMap(NoticeSession session) {
    	for (List<NoticeSession> list : registerMap.values()) {
    		list.remove(session);
		}
        return 0;
    }

    /**
     * 删除所有
     * 
     * @return
     */
    protected int removeAll() {
    	try {
    		lock.lock();
            allList.clear();
            registerMap.clear();
		} finally {
			// TODO: handle finally clause
			lock.unlock();
		}
        return 0;
    }
}
