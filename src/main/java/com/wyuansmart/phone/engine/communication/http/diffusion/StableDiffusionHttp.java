package com.wyuansmart.phone.engine.communication.http.diffusion;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyuansmart.phone.common.core.entity.Image2Image;
import com.wyuansmart.phone.common.core.entity.Txt2Image;
import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.common.enums.EControlNetType;
import com.wyuansmart.phone.engine.communication.http.BaseClientHttp;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgApiResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgResponse;
import com.wyuansmart.phone.engine.config.ControlNetModelConfig;
import com.wyuansmart.phone.engine.config.DiffusionEngineConfig;
import com.wyuansmart.phone.engine.manager.diffusion.DrawStatus;
import com.wyuansmart.phone.engine.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class StableDiffusionHttp extends BaseClientHttp {

    private static Logger logger = LoggerFactory.getLogger(StableDiffusionHttp.class);

    /**
     * 上一次测检状态时间
     */
    private Calendar lastCheckTime = null;

    private boolean serviceInvalid = false;

    /**
     * 当前支持的模型信息
     */
    private ArrayList<ModelCheckpoint> modelCheckpoints = new ArrayList<>();

    /**
     * controlNet模型
     */
    private HashMap<String, ModelCheckpoint> controlNetMap = new HashMap<>();

    /**
     * 当前使用的模型
     */
    private ModelCheckpoint checkpoint;



    private DiffusionEngineConfig config;

    public boolean isServiceInvalid() {
        return serviceInvalid;
    }

    public void setServiceInvalid(boolean serviceInvalid) {
        this.serviceInvalid = serviceInvalid;
    }

    public ArrayList<ModelCheckpoint> getModelCheckpoints() {
        return modelCheckpoints;
    }

    public void setModelCheckpoints(ArrayList<ModelCheckpoint> modelCheckpoints) {
        this.modelCheckpoints = modelCheckpoints;
    }

    public ModelCheckpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(ModelCheckpoint checkpoint) {
        this.checkpoint = checkpoint;
    }

    public DiffusionEngineConfig getConfig() {
        return config;
    }

    public void setConfig(DiffusionEngineConfig config) {
        this.config = config;
    }


    public void checkModel(){
        Calendar now  = Calendar.getInstance();
        if (lastCheckTime != null){
            long timeInfo = now.getTimeInMillis() - lastCheckTime.getTimeInMillis();
            if(Math.abs(timeInfo) < 60000){
                return;
            }
        }

        lastCheckTime = Calendar.getInstance();
        ArrayList<ModelCheckpoint> list = getEngineModelCheckpoints();
        if(list.size() > 0){
            serviceInvalid = false;
            modelCheckpoints = list;
        }else{
            serviceInvalid = true;
            modelCheckpoints.clear();
            controlNetMap.clear();
            return;
        }
        controlNetMap.clear();
        ArrayList<ModelCheckpoint> controlNetModels = getControlNetModels();
        if(list.size() > 0){
            for (ModelCheckpoint modelCheckpoint : controlNetModels){
                controlNetMap.put(modelCheckpoint.getFilename(),modelCheckpoint);
            }
        }
    }

    /**
     * 初始化设置模型模型
     * @param checkpointName
     */
    public void init(String checkpointName){
        checkModel();
        setModel(checkpointName);
    }


    /**
     * 设置当前使用的模型
     * @param checkpointName
     */
    public void setModel(String checkpointName){
        if(StringUtils.isEmpty(checkpointName)){
            return;
        }
        //if(this.checkpoint != null && checkpointName.equals(checkpoint.getModelName())){
        //    return;
        //}

        ModelCheckpoint model = null;
        for (ModelCheckpoint modelCheckpoint : modelCheckpoints){
            if(checkpointName.equals(modelCheckpoint.getModelName())){
                model = modelCheckpoint;
                break;
            }
        }
        if(model == null){
            logger.error("没有找到要设置的模型"  + checkpointName);
            return;
        }

        if(setEngineModelApi(model)){
            this.checkpoint = model;
        }
    }

    /**
     * 获取当前使用的模型名称
     * @return
     */
    public String getModel(){
        if(checkpoint != null ){
            return checkpoint.getModelName();
        }else {
            return "";
        }
    }

    /**
     * 设置当前使用的棤型
     * @param model
     * @return
     */
    private boolean setEngineModelCheckpoint(ModelCheckpoint model){
        logger.debug("设置引擎" + getServiceAddress() +",当前使用的模型为:" + model.getTitle());
        String path = getServiceAddress() + "/run/predict/";
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("fn_index", 365);
        jsonObject.put("session_hash", "zqi3ph55554llq");
        JSONArray jsonArray = new JSONArray();
        jsonArray.set(0,model.getTitle());
        jsonObject.put("data",jsonArray);

        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401) {
                return false;
            }
            else {
                return false;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("IOException",e);
            return false;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject resultObject = JSONObject.parseObject(result);
        if (resultObject != null){
            return true;
        }
        return false;
    }

    /**
     * 设置当前使用的模型
     * @param model
     * @return
     */
    private boolean setEngineModelApi(ModelCheckpoint model){
        String path = getServiceAddress() + "/sdapi/v1/reload-checkpoints";
        JSONObject jsonObject = new JSONObject();
        logger.debug("设置引擎" + getServiceAddress() +",当前使用的模型为:" + model.getTitle());
        jsonObject.put("model_title", model.getTitle());
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401) {
                return false;
            }
            else {
                return false;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("IOException",e);
            return false;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject resultObject = JSONObject.parseObject(result);
        if (resultObject != null){
            return true;
        }
        return false;
    }

    /**
     * 获取前的支持的模型
     * @return
     */
    public ArrayList<ModelCheckpoint> getEngineModelCheckpoints(){
        ArrayList<ModelCheckpoint> list = new ArrayList<>();
        String path = getServiceAddress() + "/sdapi/v1/sd-models";
        HttpGet httpGet = new HttpGet(path);
        httpGet.addHeader("Content-Type","application/json;charset=UTF-8");
        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httpGet);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401) {
                return list;
            }
            else {
                return list;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("IOException",e);
            return list;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONArray jArray = JSONArray.parseArray(result);
        if(jArray == null || jArray.size() == 0 ){
            return list;
        }
        for (int i = 0; i < jArray.size(); i++){
            JSONObject fileJson = jArray.getJSONObject(i);
            ModelCheckpoint modelCheckpoint = JSONObject.toJavaObject(fileJson,ModelCheckpoint.class);
            logger.debug(getServiceAddress() + ",支持的模型有:" + modelCheckpoint.toString());
            list.add(modelCheckpoint);
        }
        return list;
    }

    /**
     * 获取前的Control支持的模型
     * @return
     */
    public ArrayList<ModelCheckpoint> getControlNetModels(){
        ArrayList<ModelCheckpoint> list = new ArrayList<>();
        String path = getServiceAddress() + "/controlnet/model_list";
        HttpGet httpGet = new HttpGet(path);
        httpGet.addHeader("Content-Type","application/json;charset=UTF-8");
        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httpGet);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401) {
                return list;
            }
            else {
                return list;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("IOException",e);
            return list;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject jsonObject = JSONObject.parseObject(result);
        if(jsonObject == null){
            return list;
        }
        JSONArray jArray = jsonObject.getJSONArray("model_list");
        if(jArray == null || jArray.size() == 0){
            return list;
        }
        ArrayList<ControlNetModelConfig> controlNetModelConfigs = config.getControlNet().getModels();
        for (int i = 0; i < jArray.size(); i++){
            ModelCheckpoint modelCheckpoint = new ModelCheckpoint();
            String title = jArray.getString(i);
            String modelName = title;
            modelCheckpoint.setTitle(title);
            for (ControlNetModelConfig controlNetModelConfig : controlNetModelConfigs){
                if(title.indexOf(controlNetModelConfig.getModel()) >= 0){
                    modelCheckpoint.setModelName(controlNetModelConfig.getModel());
                    modelCheckpoint.setFilename(controlNetModelConfig.getName());
                    break;
                }
            }
            list.add(modelCheckpoint);
            logger.debug(getServiceAddress() + ",支持的controlNet 模型有:" + modelCheckpoint.toString());
        }
        return list;
    }

    /**
     * 请求文本作画
     * @return
     */
    public Txt2imgResponse predictTxt2img(Txt2Image txt2Image, String idTask) {
        setModel(txt2Image.getSdModelCheckpoint());

        Txt2imgResponse response = null;
        String path = getServiceAddress() + "/run/predict/";
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("fn_index", 121);
        jsonObject.put("session_hash", idTask);
        JSONArray jsonArray = toTxt2imgData(txt2Image,idTask);
        jsonObject.put("data",jsonArray);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);
        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401) {
                return response;
            }
            else {
                return response;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("IOException",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        response = new Txt2imgResponse();
        JSONObject resultObject = JSONObject.parseObject(result);
        response.setIs_generating(resultObject.getBooleanValue("is_generating"));
        response.setAverage_duration(resultObject.getFloat("average_duration"));
        response.setDuration(resultObject.getFloat("duration"));

        JSONArray jArray = resultObject.getJSONArray("data");
        if(jArray.size() >= 2 ){
            String info = jArray.getString(1);
            response.setInfo(info);

            JSONArray fileJArray = jArray.getJSONArray(0);
            for(int i =0; i < fileJArray.size(); i++){
                JSONObject fileJson = fileJArray.getJSONObject(i);
                if (fileJson != null){
                    String filePath = fileJson.getString("name");
                    if (StringUtils.isEmpty(filePath)){
                        continue;
                    }

                    if(filePath.indexOf("-rids") > 0){
                        continue;
                    }

                    String fileUrl = getServiceAddress() + "/file=" + filePath;
                    response.addImageUrl(fileUrl);
                }
            }
        }
        return response;
    }

    /**
     * 请求文本作画 调用API接口
     * @return
     */
    public Txt2imgApiResponse predictTxt2imgApi(Txt2Image txt2Image, String idTask, DrawStatus status) {

        if(status != null){
            status.setStatus(DrawStatus.INIT_STATUS);
        }
        setModel(txt2Image.getSdModelCheckpoint());

        Txt2imgApiResponse response = null;
        String path = getServiceAddress() + "/sdapi/v1/txt2img";
        JSONObject jsonObject = toTxt2ImgAPI(txt2Image,idTask);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            if(status != null){
                status.setStatus(DrawStatus.RUN_STATUS);
            }
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            } else {
                logger.error("txt2img api error" + httpResponse.toString());
                return response;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("txt2img api IOException",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        response = JSONObject.toJavaObject(jsonObjectRep,Txt2imgApiResponse.class);
        return response;
    }

    /**
     * controlnet 请求文本作画 调用API接口
     * @return
     */
    public Txt2imgApiResponse controlNetTxt2imgApi(Txt2Image txt2Image,String idTask, DrawStatus status) {
        if(status != null){
            status.setStatus(DrawStatus.INIT_STATUS);
        }
        setModel(txt2Image.getSdModelCheckpoint());

        Txt2imgApiResponse response = null;
        String path = getServiceAddress() + "/controlnet/txt2img";
        JSONObject jsonObject = toControlNetTxt2ImgAPI(txt2Image,idTask);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            if(status != null){
                status.setStatus(DrawStatus.RUN_STATUS);
            }
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            } else {
                logger.error("txt2img api error" + httpResponse.toString());
                return response;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("txt2img api IOException",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        response = JSONObject.toJavaObject(jsonObjectRep,Txt2imgApiResponse.class);
        return response;
    }


    /**
     * 请求图像文本作画 调用API接口
     * @return
     */
    public Txt2imgApiResponse predictImage2imgApi(Image2Image image2Image, String idTask, DrawStatus status) {
        if(status != null){
            status.setStatus(DrawStatus.INIT_STATUS);
        }
        setModel(image2Image.getSdModelCheckpoint());
        Txt2imgApiResponse response = null;
        String path = getServiceAddress() + "/sdapi/v1/img2img";
        JSONObject jsonObject = toImg2ImgAPI(image2Image,idTask);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            if(status != null){
                status.setStatus(DrawStatus.RUN_STATUS);
            }
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            } else {
                logger.error("img2img api error" + httpResponse.toString());
                return response;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error(" img2img api IOException",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        response = JSONObject.toJavaObject(jsonObjectRep,Txt2imgApiResponse.class);
        return response;
    }

    /**
     * 请求controlNet 图像文本作画 调用API接口
     * @return
     */
    public Txt2imgApiResponse controlNetImage2imgApi(Image2Image image2Image, String idTask, DrawStatus status) {
        if(status != null){
            status.setStatus(DrawStatus.INIT_STATUS);
        }
        setModel(image2Image.getSdModelCheckpoint());

        Txt2imgApiResponse response = null;
        String path = getServiceAddress() + "/controlnet/img2img";
        JSONObject jsonObject = toControlNetImg2ImgAPI(image2Image,idTask);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            if(status != null){
                status.setStatus(DrawStatus.RUN_STATUS);
            }
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            } else {
                logger.error("img2img api error" + httpResponse.toString());
                return response;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error(" img2img api IOException",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("IOException",e);
                }
            }
        }

        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        response = JSONObject.toJavaObject(jsonObjectRep,Txt2imgApiResponse.class);
        return response;
    }

    private JSONObject toTxt2ImgAPI(Txt2Image txt2Image, String taskId){
        JSONObject jsonObject = new JSONObject();
        String id = "task(" + taskId + ")";
        jsonObject.put("task_id", id);
        jsonObject.put("prompt", txt2Image.getPrompt());
        jsonObject.put("negative_prompt",txt2Image.getNegativePrompt());
        jsonObject.put("seed",txt2Image.getSeed());
        jsonObject.put("denoising_strength",0.7f);
        setImageSize(txt2Image,jsonObject,false);
        jsonObject.put("sampler_name",txt2Image.getSamplingMethod());
        jsonObject.put("steps",txt2Image.getSteps());
        jsonObject.put("cfg_scale",txt2Image.getCfgScale());
        jsonObject.put("restore_faces",txt2Image.isRestoreFaces());
        jsonObject.put("batch_size",txt2Image.getBatchSize());
        jsonObject.put("n_iter",txt2Image.getBatchCount());
        jsonObject.put("tiling",txt2Image.isTiling());
        return jsonObject;
    }

    private JSONObject toImg2ImgAPI(Image2Image image2Image,String taskId){
        JSONObject jsonObject = new JSONObject();
        String id = "task(" + taskId + ")";
        jsonObject.put("task_id", id);
        jsonObject.put("prompt", image2Image.getPrompt());
        jsonObject.put("negative_prompt",image2Image.getNegativePrompt());
        jsonObject.put("seed",image2Image.getSeed());
        setImageSize(image2Image,jsonObject,false);
        jsonObject.put("sampler_name",image2Image.getSamplingMethod());
        jsonObject.put("steps",image2Image.getSteps());
        jsonObject.put("cfg_scale",image2Image.getCfgScale());
        jsonObject.put("restore_faces",image2Image.isRestoreFaces());
        jsonObject.put("batch_size",image2Image.getBatchSize());
        jsonObject.put("n_iter",image2Image.getBatchCount());
        jsonObject.put("tiling",image2Image.isTiling());
        float denoisingStrength = image2Image.getDenoisingStrength();
        if (denoisingStrength < 0.05f){
            denoisingStrength = 0.7f;
        }
        jsonObject.put("denoising_strength",denoisingStrength);
        jsonObject.put("mask_blur",image2Image.getMaskBlur());
        JSONArray jsonArray = new JSONArray();
        byte[] imageData = image2Image.getInitImage();
        byte[] newData = imageData;

        String imageBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(newData);
        jsonArray.add(0,imageBase64);
        jsonObject.put("init_images",jsonArray);
        return jsonObject;
    }

    private JSONObject toControlNetTxt2ImgAPI(Txt2Image txt2Image,String taskId){
        JSONObject jsonObject = new JSONObject();
        String id = "task(" + taskId + ")";
        jsonObject.put("task_id", id);
        jsonObject.put("prompt", txt2Image.getPrompt());
        jsonObject.put("negative_prompt",txt2Image.getNegativePrompt());
        jsonObject.put("seed",txt2Image.getSeed());
        setImageSize(txt2Image,jsonObject,true);
        jsonObject.put("sampler_name",txt2Image.getSamplingMethod());
        jsonObject.put("steps",txt2Image.getSteps());
        jsonObject.put("cfg_scale",txt2Image.getCfgScale());
        jsonObject.put("restore_faces",txt2Image.isRestoreFaces());
        jsonObject.put("batch_size",txt2Image.getBatchSize());
        jsonObject.put("n_iter",txt2Image.getBatchCount());
        jsonObject.put("tiling",txt2Image.isTiling());
        jsonObject.put("denoising_strength",0.7f);

        JSONObject controlObject = new JSONObject();
        byte[] imageData = txt2Image.getControlNetImage();
        byte[] newData = imageData;

        String imageBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(newData);
        controlObject.put("input_image",imageBase64);
        setControlNetParameter(controlObject,txt2Image);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(0,controlObject);
        jsonObject.put("controlnet_units",jsonArray);
        return jsonObject;
    }

    private JSONObject toControlNetImg2ImgAPI(Image2Image image2Image,String taskId){
        JSONObject jsonObject = new JSONObject();
        String id = "task(" + taskId + ")";
        jsonObject.put("task_id", id);
        jsonObject.put("prompt", image2Image.getPrompt());
        jsonObject.put("negative_prompt",image2Image.getNegativePrompt());
        jsonObject.put("seed",image2Image.getSeed());
        setImageSize(image2Image,jsonObject,true);
        jsonObject.put("sampler_name",image2Image.getSamplingMethod());
        jsonObject.put("steps",image2Image.getSteps());
        jsonObject.put("cfg_scale",image2Image.getCfgScale());
        jsonObject.put("restore_faces",image2Image.isRestoreFaces());
        jsonObject.put("batch_size",image2Image.getBatchSize());
        jsonObject.put("n_iter",image2Image.getBatchCount());
        jsonObject.put("tiling",image2Image.isTiling());
        float denoisingStrength = image2Image.getDenoisingStrength();
        if (denoisingStrength < 0.05f){
            denoisingStrength = 0.7f;
        }
        jsonObject.put("denoising_strength",denoisingStrength);
        jsonObject.put("mask_blur",image2Image.getMaskBlur());
        JSONArray jsonArray = new JSONArray();
        byte[] imageData = image2Image.getInitImage();
        byte[] newData = imageData;
        String imageBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(newData);
        jsonArray.add(0,imageBase64);
        jsonObject.put("init_images",jsonArray);

        JSONObject controlObject = new JSONObject();
        if(image2Image.getControlNetImage() != null && image2Image.getControlNetImage().length > 64){
            byte[] netImageData = image2Image.getControlNetImage();
            byte[] newNetData = netImageData;
            String inputImageBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(newNetData);
            controlObject.put("input_image",inputImageBase64);
        }
        setControlNetParameter(controlObject,image2Image);

        JSONArray unitsArray = new JSONArray();
        unitsArray.add(0,controlObject);
        jsonObject.put("controlnet_units",unitsArray);
        return jsonObject;
    }

    private void setImageSize(Txt2ImageBase baseImage,JSONObject jsonObject,boolean ControlNet){
        int max = Math.max(baseImage.getWidth(),baseImage.getHeight());
        int min = Math.min(baseImage.getWidth(),baseImage.getHeight());
        if (ControlNet){
            //ControlNet控制 方式 不能用enable_hr 会导致结果不受控，
            if (max > 768){
                int NewMax = 768;
                int NewMin = (int)(min *((float)768/max));
                NewMin = (NewMin/8) * 8;
                if(baseImage.getWidth() > baseImage.getHeight()){
                    jsonObject.put("width",NewMax);
                    jsonObject.put("height",NewMin);
                }else {
                    jsonObject.put("width",NewMin);
                    jsonObject.put("height",NewMax);
                }
            }else {
                jsonObject.put("width",baseImage.getWidth());
                jsonObject.put("height",baseImage.getHeight());
            }
        }else {
            if(max > 800){
                float scan = 2f;
                int h,w;
                if (max == min){
                    //强行为4：3
                    h = (max / 16 ) * 8;
                    w = ((int)(max * 0.75) / 16) * 8;
                }else {
                    h = ((baseImage.getHeight() / 2) / 8 ) * 8;
                    w = ((baseImage.getWidth() / 2) / 8 ) * 8;
                }

                jsonObject.put("height",h);
                jsonObject.put("width",w);
                jsonObject.put("enable_hr",true);
                jsonObject.put("hr_upscaler","Latent (bicubic antialiased)");
                jsonObject.put("hr_scale",scan);
            }else {
                jsonObject.put("width",baseImage.getWidth());
                jsonObject.put("height",baseImage.getHeight());
            }
        }

    }

    private void setControlNetParameter(JSONObject controlJson, Txt2ImageBase base){
        ModelCheckpoint modelCheckpoint = getControlNetModel(base.getImageControlNetType());
        controlJson.put("module",modelCheckpoint.getFilename());
        controlJson.put("model",modelCheckpoint.getTitle());
        controlJson.put("weight",base.getControlNetWeight());
        controlJson.put("resize_mode", "Scale to Fit (Inner Fit)");
        controlJson.put("lowvram",false);
        int processor_res = 512;
        int threshold_a = 64;
        int threshold_b = 64;
        switch (base.getImageControlNetType()){
            case 0:
                processor_res = 512;
                threshold_a = 100;
                threshold_b = 200;
                break;
            case 1:
                processor_res = 512;
                threshold_a = 64;
                threshold_b = 64;
                break;
            case 2: //depth
                processor_res = 384;
                threshold_a = 64;
                threshold_b = 64;
                break;
            case 3: //normal_map
                processor_res = 512;
                //threshold_a = 0.4;
                threshold_b = 64;
                break;
            case 4: //hed
                processor_res = 512;
                threshold_a = 64;
                threshold_b = 64;
                break;
        }
        controlJson.put("processor_res",processor_res);
        if(base.getImageControlNetType() == 3){
            controlJson.put("threshold_a", 0.4f);
        }else {
            controlJson.put("threshold_a", threshold_a);
        }
        controlJson.put("threshold_b",threshold_b);
        controlJson.put("guidance", 1.0f);
        controlJson.put("guidance_start",0);
        controlJson.put("guidance_end", 1);
        controlJson.put("guessmode", false);
    }

    /**
     * 获取controlNetType 类型对应的ControlNet模型信息
     * @param controlNetType
     * @return
     */
    public ModelCheckpoint getControlNetModel(int controlNetType){
        EControlNetType type = EControlNetType.getControlNetType(controlNetType);
        ModelCheckpoint modelCheckpoint = controlNetMap.get(type.getName());
        return modelCheckpoint;
    }

    /**
     * 查询进度
     * @return
     */
    public ProgressResponse queryProgress(String idTask) {
        ProgressResponse response = null;
        String path = getServiceAddress() + "/internal/progress";
        JSONObject jsonObject = new JSONObject();
        String id = "task(" + idTask + ")";
        jsonObject.put("id_task", id);
        jsonObject.put("id_live_preview", -1);
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);

            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 401){
                return response;
            }
            else {
                return response;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.warn("IOException ",e);
            return response;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        response = JSONObject.toJavaObject(jsonObjectRep,ProgressResponse.class);
        return response;
    }

    private JSONArray toTxt2imgData(Txt2Image txt2Image,String idTask){
        JSONArray jsonArray = new JSONArray();

        String id = "task(" + idTask + ")";
        jsonArray.add(0,id);  //id_task: str,
        jsonArray.add(1,txt2Image.getPrompt()); // prompt: str,
        jsonArray.add(2,txt2Image.getNegativePrompt());// negative_prompt: str,

        JSONArray jsonStylesArray = new JSONArray();
        jsonArray.add(3,jsonStylesArray);  //prompt_styles,
        jsonArray.add(4,txt2Image.getSteps()); // steps: int,
        jsonArray.add(5,txt2Image.getSamplingMethod());// sampler_index: int,
        jsonArray.add(6,txt2Image.isRestoreFaces());//restore_faces: bool,
        jsonArray.add(7,txt2Image.isTiling());        //tiling: bool,
        jsonArray.add(8,txt2Image.getBatchCount());//        n_iter: int,
        jsonArray.add(9,txt2Image.getBatchSize()); // batch_size: int,
        jsonArray.add(10,txt2Image.getCfgScale());// cfg_scale: float,
        jsonArray.add(11,txt2Image.getSeed()); //seed: int,
        jsonArray.add(12,txt2Image.getSubseed()); //subseed: int,
        jsonArray.add(13,txt2Image.getSubseedStrength());// subseed_strength: float,
        jsonArray.add(14,txt2Image.getSeedResizeFromH()); //seed_resize_from_h: int,
        jsonArray.add(15,txt2Image.getSeedResizeFromW()); //seed_resize_from_w: int,
        jsonArray.add(16,txt2Image.isSeedEnableExtras());// seed_enable_extras: bool,
        jsonArray.add(17,txt2Image.getHeight());        //height: int,
        jsonArray.add(18,txt2Image.getWidth());// width: int,
        jsonArray.add(19,txt2Image.isEnableHr());//enable_hr: bool,
        jsonArray.add(20,0.7);//        denoising_strength: float,
        jsonArray.add(21,txt2Image.getHrScale()); //hr_scale: float,
        jsonArray.add(22,txt2Image.getHrUpscaler());// hr_upscaler: str,
        jsonArray.add(23,txt2Image.getHrSecondPassSteps());        //hr_second_pass_steps: int,
        jsonArray.add(24,txt2Image.getHrResizeX()); //hr_resize_x: int,
        jsonArray.add(25,txt2Image.getHrResizeY());//hr_resize_y: int,
        jsonArray.add(26,jsonStylesArray);  //prompt_styles,

        jsonArray.add(27,"None");//"None",
        jsonArray.add(28,false);//false,
        jsonArray.add(29,false);// false,
        jsonArray.add(30,"LoRA");// "LoRA",
        jsonArray.add(31,"None");// "None",
        jsonArray.add(32,0);// 0,
        jsonArray.add(33,0);// 0,//
        jsonArray.add(34,"LoRA");// "LoRA",//
        jsonArray.add(35,"None");// "None",//
        jsonArray.add(36,0);// 0,//
        jsonArray.add(37,0);// 0,//
        jsonArray.add(38,"LoRA");// "LoRA",//
        jsonArray.add(39,"None");// "None",//
        jsonArray.add(40,0);// 0,//
        jsonArray.add(41,0);// 0,
        jsonArray.add(42,"LoRA");// "LoRA",//
        jsonArray.add(43,"None");// "None",//
        jsonArray.add(44,0);// 0,//
        jsonArray.add(45,0);// 0,
        jsonArray.add(46,"LoRA");// "LoRA",//
        jsonArray.add(47,"None");// "None",//
        jsonArray.add(48,0);// 0,//
        jsonArray.add(49,0);// 0,
        jsonArray.add(50,"Refresh models");// "Refresh models",
        jsonArray.add(51,false);// false,
        jsonArray.add(52,false);// false,
        jsonArray.add(53,"positive");// "positive",
        jsonArray.add(54,"comma");// "comma",
        jsonArray.add(55,0);// 0,
        jsonArray.add(56,false);// false,
        jsonArray.add(57,false);// false,
        jsonArray.add(58,"");// "",
        jsonArray.add(59,"Seed");// "Seed",
        jsonArray.add(60,"");// "",
        jsonArray.add(61,"Nothing");// "Nothing",
        jsonArray.add(62,"");// "",
        jsonArray.add(63,"Nothing");// "Nothing",
        jsonArray.add(64,"");// "",
        jsonArray.add(65,true);// true,
        jsonArray.add(66,false);// false,
        jsonArray.add(67,false);// false,
        jsonArray.add(68,false);// false,
        jsonArray.add(69,0);// 0
        return jsonArray;
    }
}
