package com.wyuansmart.phone.engine.manager.storage;

import com.wyuansmart.phone.common.core.cloud.AbsCloud;
import com.wyuansmart.phone.common.core.cloud.CloudFactory;
import com.wyuansmart.phone.common.util.DateUtil;
import com.wyuansmart.phone.common.util.ImageUtil;
import com.wyuansmart.phone.engine.config.CloudAccountConfig;
import com.wyuansmart.phone.engine.config.CloudConfig;
import com.wyuansmart.phone.engine.util.FileUtil;
import com.wyuansmart.phone.engine.config.FileConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.LongAdder;

@Component
public class StorageManager {
    private static Logger logger = LoggerFactory.getLogger(StorageManager.class);

    @Autowired
    private CloudConfig cloudConfig;

    @Autowired
    private FileConfig fileConfig;

    private LongAdder longSeq = new LongAdder();

    private AbsCloud cloudStorage = null;

    @PostConstruct
    public void init() {
        CloudAccountConfig accountConfig = cloudConfig.getStorageAccount();
        if (accountConfig == null){
            return;
        }

        cloudStorage = CloudFactory.createCloudStorage(accountConfig.getType());
        cloudStorage.setServerAddress(accountConfig.getEndpoint());
        cloudStorage.setAccessKey(accountConfig.getAccessKeyId());
        cloudStorage.setSecretKey(accountConfig.getAccessKeySecret());
        cloudStorage.setBucketName(accountConfig.getBucketName());
        cloudStorage.setPort(accountConfig.getPort());
        cloudStorage.setDownloadPort(accountConfig.getDownloadPort());

        int ret = cloudStorage.initClient();
    }

    /**
     * 将一个文件URL存储到本地或云端
     * @param fileUrl 文件URL
     * @param type 保存的种类
     * @param storagePath 存储的路径
     * @param fileName 存储的文件名
     * @return 新的URL
     */
    public String storageByUrl(String fileUrl,String type,String storagePath,String fileName,String fileExt ) {
        try {
            byte[] fileData = downloadFile(fileUrl);
            if ( cloudStorage != null){
                if(StringUtils.isEmpty(storagePath)){
                    storagePath = getSavePath(type);
                }
                if (StringUtils.isEmpty(fileName)){
                    fileName = getSaveFileName();
                    if (!StringUtils.isEmpty(fileExt)){
                        fileName += fileExt;
                    }else{
                        fileName += ".jpg";
                    }
                }
                return cloudStorage.uploadFile(fileData,storagePath,fileName);
            }else {
                return saveFile(fileData,type,fileExt);
            }
        } catch (IOException e) {
            logger.warn("storageByUrl： 下载文件" + fileUrl + "异常，" + e.getMessage(),e);
            return fileUrl;
        } catch (Exception e) {
            logger.warn("storageByUrl： 保存失败 文件" + fileUrl + "异常，" + e.getMessage(),e);
            return fileUrl;
        }
    }

    /**
     * 保存文件来理文件二进制数组
     * @param fileData
     * @param type
     * @param storagePath
     * @param fileName
     * @param fileExt
     * @return
     */
    public String storageByData(byte[] fileData,String type,String storagePath,String fileName,String fileExt ) {
        try {
            if (cloudStorage != null){
                if(StringUtils.isEmpty(storagePath)){
                    storagePath = getSavePath(type);
                }
                if (StringUtils.isEmpty(fileName)){
                    fileName = getSaveFileName();
                    if (!StringUtils.isEmpty(fileExt)){
                        fileName += fileExt;
                    }else{
                        fileName += ".jpg";
                    }
                }
                return cloudStorage.uploadFile(fileData,storagePath,fileName);
            }else {
                return saveFile(fileData,type,fileExt);
            }
        } catch (Exception e) {
            logger.warn("storageByUrl： 保存失败 文件" + "异常，" + e.getMessage(),e);
            return "";
        }
    }

    /**
     * 下载图片
     * @param url
     * @return
     */
    public byte[] downloadFile(String url){
        byte[] imageData = null;
        if(cloudStorage != null && cloudStorage.isCloudStorage(url)){
            try {
                imageData = cloudStorage.downloadImage(url);
            }catch (IOException e) {
                logger.warn("云下载图片异常，" + e.getMessage(),e);
            }
        }
        try {
            imageData = ImageUtil.urlTobyte(url);
        }catch (IOException e) {
            logger.warn("下载图片异常，" + e.getMessage(),e);
        }
        return imageData;
    }

    /**
     * 取得保存文件的自增长流水号
     * @return
     */
    private long getSaveSeq() {
        longSeq.increment();
        return longSeq.longValue();
    }


    /**
     * 取得保存的文件路径
     * @return
     */
    private String getSavePath(String type){
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar date = Calendar.getInstance(Locale.getDefault());
        String dateStr = sdf.format(date.getTime());
        String path = String.format("%s/%s/%s",fileConfig.getAppName(),type,dateStr);
        try {
            FileUtil.createDir(fileConfig.getLocalPath()+"/"+path);
        }catch (Exception e){
            logger.error("create Dir failed,path:"+path,e);
            return "";
        }
        return path;
    }

    private String getSaveFileName(){
        String path = DateUtil.getLocalTime("yyyyMMddHHmmss");
        path+="_";
        path+=getSaveSeq();
        return path;
    }

    /**
     * 保存到本地文件
     * @param imageByte
     * @param fileType
     * @param fileExt 文件扩展名
     * @return
     */
    protected String saveFile(byte [] imageByte,String fileType,String fileExt){
        String filePath = getSavePath(fileType);
        filePath +="/" + getSaveFileName();
        if(!StringUtils.isEmpty(fileExt)){
            filePath+=fileExt;
        }else{
            filePath+=".jpg";
        }
        try {
            FileUtil.writeByteArrayToFile(imageByte,fileConfig.getLocalPath()+"/"+filePath,false);
            return fileConfig.getUrlPath()+filePath;
        }catch (Exception e){
            logger.warn("save file exception:"+e.getMessage(),e);
        }
        return null;
    }

    /**
     * 删除存储图片
     * @param url
     * @return
     */
    public boolean deleteFile(String url){
        if(cloudStorage != null && cloudStorage.isCloudStorage(url)){
            cloudStorage.deleteFile(url);
        }
        return true;
    }

}
