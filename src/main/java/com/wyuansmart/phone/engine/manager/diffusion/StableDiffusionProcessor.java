package com.wyuansmart.phone.engine.manager.diffusion;

import com.wyuansmart.phone.common.core.entity.Image2Image;
import com.wyuansmart.phone.common.core.entity.Txt2Image;
import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.common.enums.EControlNetType;
import com.wyuansmart.phone.common.util.ObjectIdWorker;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.StableDiffusionHttp;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgApiResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgResponse;
import com.wyuansmart.phone.engine.config.DiffusionEngineConfig;
import com.wyuansmart.phone.engine.manager.storage.StorageManager;
import com.wyuansmart.phone.engine.service.ai.AuditingService;
import com.wyuansmart.phone.engine.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 绘画处理
 */
public class StableDiffusionProcessor {

    private static Logger logger = LoggerFactory.getLogger(StableDiffusionProcessor.class);

    private StableDiffusionHttp stableDiffusionHttp;
    private DiffusionEngineConfig config;

    private int id = 0;

    private ObjectIdWorker objectIdWorker;

    private StorageManager storageManager;

    private AuditingService auditingService;

    /**
     * 处理的任务ID，及加入MAP时间
     */
    private HashMap<String,DrawStatus> taskIdMap = new HashMap<>();

    /**
     * 所有的绘画请求
     */
    private ArrayList<Txt2ImageBase> queues = new ArrayList<>();

    private ReentrantLock queuesLock = new ReentrantLock();

    public StableDiffusionProcessor(StorageManager storageManager){
        this.storageManager = storageManager;
    }

    public AuditingService getAuditingService() {
        return auditingService;
    }

    public void setAuditingService(AuditingService auditingService) {
        this.auditingService = auditingService;
    }

    public void init(int id, DiffusionEngineConfig config){
        if(stableDiffusionHttp != null){
            return;
        }
        objectIdWorker =  new ObjectIdWorker(id);
        this.config = config;
        this.id = id;
        stableDiffusionHttp = new StableDiffusionHttp();
        stableDiffusionHttp.setConfig(config);
        stableDiffusionHttp.setRequestTimeout(120000);
        stableDiffusionHttp.setSocketTimeout(120000);
        stableDiffusionHttp.setServiceAddress(config.getServiceAddress());
        stableDiffusionHttp.init(config.getCheckpoint());
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("StableDiffusion-schedule-pool-%d").daemon(true).build());
        scheduledExecutorService.scheduleAtFixedRate(new StableDiffusionProcessor.fetchNovelaiTask(),1000,10,TimeUnit.MILLISECONDS);
    }

    private void clearingOldMap(){
        ArrayList<String> delKey =new ArrayList<>();
        Calendar now = Calendar.getInstance();
        taskIdMap.forEach((key, value) -> {
            if(value != null){
                long diff = Math.abs(now.getTimeInMillis() - value.getCreateTime().getTimeInMillis());
                if(diff > 120 * 1000){
                    //二分钟之前的
                    delKey.add(key);
                }
            }
        });

        for (String key : delKey){
            taskIdMap.remove(key);
        }
    }



    /**
     * 处理绘画任务
     */
    class fetchNovelaiTask implements Runnable {

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            try {
                processorTask();
            }catch (Exception e){
                logger.warn("Exception",e);
            }
        }
    }

    /**
     * 服务是否有效
     * @return
     */
    public boolean isServiceValid(){
        return !stableDiffusionHttp.isServiceInvalid();
    }

    /**
     * 处理绘画任务
     */
    protected void processorTask(){
        Txt2ImageBase base = getTxt2Image();
        if(base == null){
            stableDiffusionHttp.checkModel();
            return;
        }
        stableDiffusionHttp.checkModel();
        logger.info("draw task: " + base.toString());
        repairParameter(base);
        Txt2imgApiResponse apiResponse = null;
        DrawStatus status = new DrawStatus();
        taskIdMap.put(base.getTaskId(),status);
        if (base instanceof Txt2Image){
            Txt2Image txt2Image = (Txt2Image) base;
            apiResponse = txt2img(txt2Image,txt2Image.getTaskId(),status);
        }else if(base instanceof Image2Image){
            Image2Image image2Image = (Image2Image)base;
            apiResponse = image2img(image2Image, base.getTaskId(),status);
        }
        if(apiResponse == null){
            Txt2imgResponse response = new Txt2imgResponse();
            response.setCompleted(true);
            response.setQueued(false);
            response.setProgress(1.0f);
            response.setTextinfo("请求会话异常");
            response.setInfo("请求会话异常");
            status.setResponse(response);
            //taskIdMap.put(base.getTaskId(),response);
        }else {
            Txt2imgResponse response = new Txt2imgResponse();
            saveImage(apiResponse,response);
            response.setActive(true);
            response.setInfo(apiResponse.getInfo());
            response.setTextinfo("finish");
            response.setCompleted(true);
            response.setAestheticScore(apiResponse.getAestheticScore());
            response.setProgress(1.0f);
            if (base.isAuditing()){
                AuditingResponse auditingResponse = auditingService.imageAuditing(response.getImageUrl());
                if (auditingResponse != null){
                    response.setAuditingResponse(auditingResponse);
                }
            }
            //taskIdMap.put(base.getTaskId(),response);
            status.setResponse(response);
        }
        status.setStatus(DrawStatus.END_STATUS);
        clearingOldMap();
    }

    /**
     * 修正异常参数
     * @param base
     */
    private void repairParameter(Txt2ImageBase base){
        if(base == null){
            return;
        }

        if (base.getSteps() <= 0 || base.getSteps() >= 40){
            base.setSteps(20);
        }

        int w = base.getWidth();
        int h = base.getHeight();
        w = (w / 8) * 8;
        h = (h / 8) * 8;
        if(w <= 16 || w > 1700){
            w = 1600;
        }
        if(h <= 16 || h > 1700){
            h = 1600;
        }
        base.setWidth(w);
        base.setHeight(h);
        if (base.getImageControlNetType() > EControlNetType.not_enable.getValue()){
            float weight = base.getControlNetWeight();
            if (weight < 0.01f || weight > 2.0f){
                weight = 1.0f;
            }
            base.setControlNetWeight(weight);
        }
    }

    /**
     * 处理文本作画
     * @param txt2Image
     * @param idTask
     * @return
     */
    private Txt2imgApiResponse txt2img(Txt2Image txt2Image,String idTask,DrawStatus status){
        if (txt2Image.getImageControlNetType() <= EControlNetType.not_enable.getValue()){
            return stableDiffusionHttp.predictTxt2imgApi(txt2Image,idTask,status);
        }else {
            return stableDiffusionHttp.controlNetTxt2imgApi(txt2Image,idTask,status);
        }
    }

    /**
     * 处理图片作画
     * @param image2Image
     * @param idTask
     * @return
     */
    private Txt2imgApiResponse image2img(Image2Image image2Image,String idTask,DrawStatus status){
        if (image2Image.getImageControlNetType() <= EControlNetType.not_enable.getValue()){
            return stableDiffusionHttp.predictImage2imgApi(image2Image,idTask,status);
        }else {
            return stableDiffusionHttp.controlNetImage2imgApi(image2Image,idTask,status);
        }
    }

    private void saveImage(Txt2imgApiResponse apiResponse,Txt2imgResponse response){
        ArrayList<String> list = apiResponse.getImages();
        ArrayList<String> newList = new ArrayList<>();
        for (String imageBase64 : list){
            byte[] imageData = ImageUtil.base64String2ByteFun(imageBase64);
            byte[] newData = imageData;
            try {
                Calendar start = Calendar.getInstance();
                //newData = ImageUtil.pngCompress(imageData);
                if(imageData.length > 100000){
                    newData = ImageUtil.imageCompress(imageData,1.0f,0.98f);
                }
                Calendar end = Calendar.getInstance();
                logger.info("png压缩用时:" + (end.getTimeInMillis() - start.getTimeInMillis()) + "MS" + ",原大小:" + imageData.length + ",压缩后:" + newData.length);
                if (newData.length > imageData.length){
                    //压缩后变大了，用原来的图片
                    newData = imageData;
                }
            } catch (IOException e) {
                logger.warn("imageCompress IOException" + e.getMessage());
            }

            String newUrl = storageManager.storageByData(newData,"txt2img","","",".png");
            newList.add(newUrl);
            if (StringUtils.isEmpty(response.getImageUrl())){
                response.setImageUrl(newUrl);
            }
        }
        response.setImageUrls(newList);
    }
    private void saveImage(Txt2imgResponse response){
        ArrayList<String> list = response.getImageUrls();
        ArrayList<String> newList = new ArrayList<>();
        for (String url : list){
            String newUrl = storageManager.storageByUrl(response.getImageUrl(),"txt2img","","",".png");
            newList.add(newUrl);
            response.setImageUrl(newUrl);
        }
        response.setImageUrls(newList);
    }

    /**
     * 查询进度
     * @param taskId
     * @return
     */
    public ProgressResponse queryTaskProgress(String taskId){
        ProgressResponse progressInfo = null;
        if(taskIdMap.containsKey(taskId)){
            DrawStatus status = taskIdMap.get(taskId);
            Txt2imgResponse response = status.getResponse();
            if (response == null){
                if(status.getStatus() < DrawStatus.RUN_STATUS){
                    ProgressResponse progressResponse = new ProgressResponse();
                    progressResponse.setActive(true);
                    progressResponse.setCompleted(false);
                    progressResponse.setTextinfo("init...");
                    progressResponse.setQueued(false);
                    progressResponse.setQueueIndex(0);
                    progressResponse.setProgress(0.0f);
                    progressInfo = progressResponse;
                }else{
                    ProgressResponse progressResponse = stableDiffusionHttp.queryProgress(taskId);
                    Float progress = null;
                    if(progressResponse != null){
                        if(progressResponse.getProgress() != null) {
                            status.setLastProgress(progressResponse.getProgress());
                            progress = progressResponse.getProgress();
                        }else {
                            progress = status.getLastProgress();
                            progressResponse.setProgress(progress);
                        }
                    }else {
                        progress = status.getLastProgress();
                        progressResponse = new ProgressResponse();
                        progressResponse.setActive(true);
                        progressResponse.setCompleted(false);
                        progressResponse.setTextinfo("");
                        progressResponse.setQueued(false);
                        progressResponse.setQueueIndex(0);
                        progressResponse.setProgress(progress);
                    }

                    progressResponse.setCompleted(false);
                    progressInfo = progressResponse;
                }
            }else {
                progressInfo = response;
            }
        }else{
            int index = queryQueuedIndex(taskId);
            if(index >= 0){
                ProgressResponse progressResponse = new ProgressResponse();
                progressResponse.setActive(false);
                progressResponse.setCompleted(false);
                progressResponse.setTextinfo("Waiting...");
                progressResponse.setQueued(true);
                progressResponse.setQueueIndex(index + 1);
                progressInfo = progressResponse;
            }else{
                ProgressResponse progressResponse = new ProgressResponse();
                progressResponse.setActive(false);
                progressResponse.setCompleted(false);
                progressResponse.setTextinfo("Waiting...");
                progressResponse.setQueued(true);
                progressResponse.setQueueIndex(1);
                progressInfo = progressResponse;
            }
        }
        if (progressInfo != null &&  progressInfo.getAestheticScore() != null && progressInfo.getAestheticScore() > 0.1){
            if(progressInfo.getProgress() == null){
                progressInfo.setProgress(0.99f);
            }
        }
        if(progressInfo != null){
            logger.info("taskId" + taskId + ",progress:" + progressInfo.toString());
        }
        return progressInfo;
    }

    /**
     * 查询任务ID排在第几个
     * @param taskId
     * @return
     */
    private int queryQueuedIndex(String taskId){
        try {
            queuesLock.lock();
            for (int i = 0; i < queues.size(); i++){
                Txt2ImageBase base = queues.get(i);
                if (taskId.equals(base.getTaskId())){
                    return i;
                }
            }
            return -1;
        }finally {
            queuesLock.unlock();
        }
    }


    /**
     * 增求绘画请求
     * @param txt2Image
     * @return
     */
    public String addTxt2Image(Txt2ImageBase txt2Image){
        try {
            queuesLock.lock();
            Long taskId = objectIdWorker.nextId();
            txt2Image.setTaskId(taskId.toString());
            queues.add(txt2Image);
            return taskId.toString();
        }finally {
            queuesLock.unlock();
        }
    }

    public int getQueuseSize(){
        try {
            queuesLock.lock();
            return queues.size();
        }finally {
            queuesLock.unlock();
        }
    }


    private Txt2ImageBase getTxt2Image(){
        try {
            queuesLock.lock();
            if(queues.size() <= 0){
                return null;
            }
            return queues.remove(0);
        }finally {
            queuesLock.unlock();
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取当前的模型名称
     * @return
     */
    public String getNowModel(){
        return stableDiffusionHttp.getModel();
    }
}
