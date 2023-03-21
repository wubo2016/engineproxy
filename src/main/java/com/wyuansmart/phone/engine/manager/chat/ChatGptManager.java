package com.wyuansmart.phone.engine.manager.chat;

import com.wyuansmart.phone.common.core.chat.ChatRequest;
import com.wyuansmart.phone.common.util.ObjectIdWorker;
import com.wyuansmart.phone.engine.common.ApplicationException;
import com.wyuansmart.phone.engine.communication.http.chat.ChatGptHttp;
import com.wyuansmart.phone.engine.communication.http.chat.vo.ChatResponse;
import com.wyuansmart.phone.engine.config.ChatGptConfig;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ChatGptManager {
    private static Logger logger = LoggerFactory.getLogger(ChatGptManager.class);

    @Autowired
    private ChatGptConfig chatGptConfig;

    private ScheduledExecutorService scheduledExecutorService;

    private ChatGptHttp chatGptHttp;

    private ObjectIdWorker objectIdWorker;

    private long LastRunTime = 20000;

    private int MaxQueueSize = 20;

    /**
     * 等待会话队列
     */
    private ArrayList<ChatRequest> waitQueue = new ArrayList<>();
    private ReentrantLock queueLock = new ReentrantLock();

    /**
     * 状任状态
     */
    private HashMap<String, ChatGptStatus> taskStatusMap = new HashMap<>();
    private ReentrantLock mapLock = new ReentrantLock();

    @PostConstruct
    public void init() {
        if (chatGptHttp != null){
            return;
        }

        objectIdWorker = new ObjectIdWorker(100);
        chatGptHttp = new ChatGptHttp();
        chatGptHttp.setKey(chatGptConfig.getKey());
        chatGptHttp.setServiceAddress(chatGptConfig.getServiceAddress());
        chatGptHttp.setToken(chatGptConfig.getToken());

        scheduledExecutorService = new ScheduledThreadPoolExecutor(chatGptConfig.getThreads(),
                new BasicThreadFactory.Builder().namingPattern("chatgpt-schedule-pool-%d").daemon(true).build());
        scheduledExecutorService.scheduleAtFixedRate(new ChatGptManager.fetchChatTask(),1000,10, TimeUnit.MILLISECONDS);
    }

    /**
     * 增加会话任务请求
     * @param request
     * @return
     * @throws ApplicationException
     */
    public String addChatTask(ChatRequest request) throws ApplicationException {
        try {
            queueLock.lock();
            if (MaxQueueSize < waitQueue.size()){
                ApplicationException applicationException = new ApplicationException(-10000,"he system is busy");
                throw applicationException;
            }

            Long id = objectIdWorker.nextId();
            request.setIdTask(id.toString());
            waitQueue.add(request);
            return id.toString();
        }finally {
            queueLock.unlock();
        }
    }

    private ChatRequest getChatTask(){
        try {
            ChatRequest request = null;
            queueLock.lock();
            if (waitQueue.size() <= 0){
                return request;
            }

            request = waitQueue.remove(0);
            addStartMap(request);
            return request;
        }finally {
            queueLock.unlock();
        }
    }

    private void addStartMap(ChatRequest request){
        try {
            mapLock.lock();
            ChatGptStatus status = new ChatGptStatus();
            status.setExpectedTime(LastRunTime);
            status.setUser(request.getUser());
            taskStatusMap.put(request.getIdTask(),status);
        }finally {
            mapLock.unlock();
        }
    }

    /**
     * 处理会话任务
     */
    class fetchChatTask implements Runnable {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            try {
                processorChat();
            }catch (Exception e){
                logger.warn("Exception",e);
            }
        }
    }

    /**
     * 处理会话
     */
    protected void processorChat(){
        ChatRequest chatRequest = getChatTask();
        if (chatRequest == null){
            return;
        }
        Calendar startTime = Calendar.getInstance();
        ChatGptStatus status = getMapChatStatus(chatRequest.getIdTask());
        if(status != null){
            status.setStatus(ChatGptStatus.RUN_STATUS);
        }
        ChatResponse response = chatGptHttp.chat(chatRequest);
        Calendar endTime = Calendar.getInstance();
        if (status != null){
            status.setChatResponse(response);
            status.setEndTime(Calendar.getInstance());
            status.setStatus(ChatGptStatus.END_STATUS);
        }
        if (response != null){
            int code = response.getCode();
            if(code == 0 || code == 200){
                LastRunTime = endTime.getTimeInMillis() - startTime.getTimeInMillis();
            }else {
                logger.warn("请求chatgpt会话异常" + response.toString());
            }
        }
    }


    protected ChatGptStatus getMapChatStatus(String idTask){
        try {
            mapLock.lock();
            ChatGptStatus status =taskStatusMap.get(idTask);
            return status;
        }finally {
            mapLock.unlock();
        }
    }

    /**
     * 查询会话状态
     * @param taskId
     * @return
     */
    public ChatGptStatus queryStatus(String taskId){
        ChatGptStatus status = getMapChatStatus(taskId);
        if (status != null){
            return status;
        }

        int index = getQueueIndex(taskId);
        if( index >= 0){
            status = new ChatGptStatus();
            status.setQueueIndex(index+1);
            status.setStartTime(null);
            status.setStatus(ChatGptStatus.WAIT_RUN_STATUS);
            return status;
        }
        return status;
    }

    /**
     * 获取还要等多久执行
     * @param taskId
     * @return
     */
    protected int getQueueIndex(String taskId){
        try {
            queueLock.lock();
            for (int i = 0; i < waitQueue.size(); i++){
                ChatRequest base = waitQueue.get(i);
                if (taskId.equals(base.getIdTask())){
                    return i;
                }
            }
            return -1;
        }finally {
            queueLock.unlock();
        }
    }

    /**
     * 获取当前等持队列大小
     * @return
     */
    public int getTaskQueueSize(){
        try {
            queueLock.lock();
            return waitQueue.size();
        }finally {
            queueLock.unlock();
        }
    }


    /**
     * 获取并发线程数
     * @return
     */
    public int getCapacity(){
        return chatGptConfig.getThreads();
    }


}
