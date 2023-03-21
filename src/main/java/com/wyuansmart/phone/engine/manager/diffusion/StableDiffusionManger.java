package com.wyuansmart.phone.engine.manager.diffusion;

import com.wyuansmart.phone.common.core.entity.Image2Image;
import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.common.enums.EControlNetType;
import com.wyuansmart.phone.common.util.ObjectIdWorker;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.config.DiffusionEngineConfig;
import com.wyuansmart.phone.engine.config.StableDiffusionConfig;
import com.wyuansmart.phone.engine.manager.storage.StorageManager;
import com.wyuansmart.phone.engine.service.ai.AuditingService;
import com.wyuansmart.phone.engine.service.ai.TranslateService;
import com.wyuansmart.phone.engine.util.StringParseUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class StableDiffusionManger {

    private static Logger logger = LoggerFactory.getLogger(StableDiffusionManger.class);

    @Autowired
    private StableDiffusionConfig stableDiffusionConfig;

    @Autowired
    private StorageManager storageManager;

    @Autowired
    private AuditingService auditingService;

    @Autowired
    private TranslateService translateService;

    private ObjectIdWorker objectIdWorker;

    private ReentrantLock processorsLock = new ReentrantLock();
    private ArrayList<StableDiffusionProcessor> processors = new ArrayList<>();

    private HashMap<String,ImageCache> imageCacheHashMap = new HashMap<>();

    private ReentrantLock cacheLock = new ReentrantLock();
    private int lastIndex = -1;

    @PostConstruct
    public void init() {
        List<DiffusionEngineConfig> list = stableDiffusionConfig.getEngines();
        if(list == null){
            logger.error("没有配置绘画引擎");
            return;
        }

        objectIdWorker = new ObjectIdWorker(1);
        int i = 1;
        for (DiffusionEngineConfig config : list){
            if(!config.isEnable()){
                continue;
            }
            StableDiffusionProcessor processor = new StableDiffusionProcessor(storageManager);
            processor.init(i,config);
            processor.setAuditingService(auditingService);
            processorsLock.lock();
            processors.add(processor);
            processorsLock.unlock();
            i++;
        }
    }

    public int getTaskQueueSize(){
        try {
            processorsLock.lock();
            int size = 0;
            for (StableDiffusionProcessor temp : processors){
                size += temp.getQueuseSize();
            }
            return size;
        }finally {
            processorsLock.unlock();
        }
    }


    public int getCapacity(){
        return processors.size();
    }

    /**
     * 异步方式请求 绘画
     * @param txt2Image
     * @return 任务ID
     */
    public String txt2Image(Txt2ImageBase txt2Image){
        StableDiffusionProcessor processor = getIdleProcessor(txt2Image.getSdModelCheckpoint());
        if (processor == null){
            logger.error("没有配置绘画引擎");
            return "";
        }
        if (txt2Image.getImageControlNetType() > EControlNetType.not_enable.getValue()){
            addImageCache(txt2Image.getControlNetImageUrl(),txt2Image.getControlNetImage());
        }

        if(txt2Image instanceof Image2Image){
            addImageCache(((Image2Image)txt2Image).getInitImageUrl(),((Image2Image)txt2Image).getInitImage());
        }
        if(StringParseUtil.checkIfContainCH(txt2Image.getPrompt())){
            logger.info("检测到输入的绘画词有中文 需要翻译," + txt2Image.getPrompt());
            String text = textTranslateToEn(txt2Image.getPrompt(),"zh");
            logger.info("翻译结果:" + text);
            txt2Image.setPrompt(text);
        }
        if(StringParseUtil.checkIfContainCH(txt2Image.getNegativePrompt())){
            logger.info("检测到输入的反向提示词有中文，需要翻译，" + txt2Image.getNegativePrompt());
            String text = textTranslateToEn(txt2Image.getNegativePrompt(),"zh");
            logger.info("翻译结果:" + text);
            txt2Image.setNegativePrompt(text);
        }
        return processor.addTxt2Image(txt2Image);
    }


    /**
     * 翻译成英文
     * @param text
     * @return
     */
    private String textTranslateToEn(String text,String source){
        try {
            String string = translateService.textTranslate(source,text,"en");
            return string;
        }catch (Exception e){
            logger.error("翻译异常：" + e.getMessage(),e);
            return text;
        }
    }

    /**
     * 查询进度
     * @param taskId
     * @return
     */
    public ProgressResponse queryTaskProgress(String taskId){
        long taskIdLong = Long.parseLong(taskId);
        int id = (int)ObjectIdWorker.getWorkerId(taskIdLong);
        StableDiffusionProcessor processor = getProcessor(id);
        if (processor != null){
            return processor.queryTaskProgress(taskId);
        }
        else {
            return null;
        }
    }

    private StableDiffusionProcessor getProcessor(int id){
        try {
            processorsLock.lock();
            StableDiffusionProcessor processor = null;
            for (int i = 0; i < processors.size(); i++){
                StableDiffusionProcessor temp = processors.get(i);
                if(temp.getId() == id){
                    return temp;
                }
            }
            return processor;
        }finally {
            processorsLock.unlock();
        }
    }

    private StableDiffusionProcessor getIdleProcessor(String model){
        try {
            processorsLock.lock();
            int size = -1;
            int index = -1;
            StableDiffusionProcessor processor = null;
            int n = processors.size();
            if (lastIndex < 0 || lastIndex >= (n- 1) ){
                lastIndex = -1;
            }
            index = lastIndex+1;
            int retIndex = -1;
            for (; n > 0; n--,index++){
                if (index >= processors.size()){
                    index = 0;
                }

                StableDiffusionProcessor temp = processors.get(index);
                if(!temp.isServiceValid()){
                    continue;
                }

                if(size < 0){
                    processor = temp;
                    retIndex = index;
                    size = processor.getQueuseSize();
                }else if(size > temp.getQueuseSize()){
                    processor = temp;
                    retIndex = index;
                    size = processor.getQueuseSize();
                }else if(size == temp.getQueuseSize()){
                    if (!StringUtils.isEmpty(model)
                            && model.equals(temp.getNowModel())){
                        processor = temp;
                        retIndex = index;
                        size = processor.getQueuseSize();
                    }
                }
            }
            lastIndex = retIndex;
            return processor;
        }finally {
            processorsLock.unlock();
        }
    }

    /**
     * 图片加入到缓存
     * @param url
     * @param imageData
     */
    private void addImageCache(String url,byte[] imageData){
        if (imageData == null || imageData.length < 16){
            return;
        }

        if (StringUtils.isEmpty(url)){
            return;
        }

        try {
            cacheLock.lock();
            ImageCache imageCache = imageCacheHashMap.get(url);
            if (imageCache == null){
                imageCache = new ImageCache();
                imageCache.setImageData(imageData);
                imageCache.setUrl(url);
                imageCacheHashMap.put(url,imageCache);
            }else {
                imageCache.setCalendar(Calendar.getInstance());
            }
        }finally {
            cacheLock.unlock();
        }

        removeOldCacheData();
    }

    private void removeOldCacheData(){
        try {
            cacheLock.lock();
            ArrayList<String> delKey =new ArrayList<>();
            Calendar now = Calendar.getInstance();
            imageCacheHashMap.forEach((key, value) -> {
                if(value != null){
                    long diff = Math.abs(now.getTimeInMillis() - value.getCalendar().getTimeInMillis());
                    if(diff > stableDiffusionConfig.getImageCacheTime() * 60 * 1000){
                        //删除几分钟前的数据
                        delKey.add(key);
                    }
                }
            });
            for (String key : delKey){
                imageCacheHashMap.remove(key);
            }
        }finally {
            cacheLock.unlock();
        }
    }

    /**
     * 获取URL对应的图片缓存数据
     * @param url
     * @return
     */
    public byte[] getUrlImageCacheData(String url){
        try {
            cacheLock.lock();
            ImageCache imageCache = imageCacheHashMap.get(url);
            if(imageCache != null){
                imageCache.setCalendar(Calendar.getInstance());
                return imageCache.getImageData();
            }
        }finally {
            cacheLock.unlock();
        }

        byte[] bytes = storageManager.downloadFile(url);
        if(bytes == null){
            logger.warn("下载图片失败 " + url);
        }
        return bytes;
    }


}
