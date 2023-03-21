package com.wyuansmart.phone.engine.util;


import com.baidu.aip.util.Base64Util;
import com.idrsolutions.image.png.PngCompressor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.Resizer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/*********************
 *  分析服务图片及文件，字符处理公共类
 *  @author chaser.w
 * *******************/
public class ImageUtil {
	  private static Logger LOG = LogManager.getLogger(ImageUtil.class);

	  private static long seqId = 0;

	  private static String proxyUrl = "http://127.0.0.1";

	/**************************
	 *
	 * @param inputStream 文件输入流
	 * @param fileName 文件名
	 * @param localPath 本地存储路径
	 * @return
	 */
	public static boolean savePic(InputStream inputStream, String fileName, String localPath) {

		OutputStream os = null;
		try {
			String path = localPath;
			// 2K的数据缓冲
			byte[] bs = new byte[2048];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件

			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}


	/**************************
	 *
	 * @param bytes 文件二进制流
	 * @param fileName 文件名
	 * @param localPath 本地存储路径
	 * @return
	 */
	public static boolean savePicByBytes(byte[] bytes, String fileName, String localPath) {

		OutputStream os = null;
		InputStream inputStream = null;
		try {
			String path = localPath;
			// 2K的数据缓冲
			//byte[] bs = new byte[2048];
			inputStream = new ByteArrayInputStream(bytes);
			byte[] bs = new byte[2048];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件

			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}


	public static boolean saveBytes2File(byte[] bytes, String fileNamePath) {

		OutputStream os = null;
		InputStream inputStream = null;
		try {
			// 2K的数据缓冲
			//byte[] bs = new byte[2048];
			inputStream = new ByteArrayInputStream(bytes);
			byte[] bs = new byte[2048];
			// 读取到的数据长度
			int len;
			// 输出的文件流保存到本地文件
			os = new FileOutputStream(fileNamePath);
			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// 完毕，关闭所有链接
			try {
				os.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}


	/*********************************
	 * 获取一个随机的带时间搓的文件名
	 * @param filename 带后缀的文件名
	 * @return
	 */
	public static String getNewFileNameWithTimeStr(String filename)
	{
		String fileExt = FilenameUtils.getExtension(filename);
		String randomStr = String.valueOf(Math.round(Math.random() * 1000000));
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMMdd-HH-mm-ss.SSS").format(new Date()) +"_"+randomStr+ "." + fileExt;
		LOG.info("fileNameAppendix:" + fileNameAppendix);
		return fileNameAppendix;

	}

	/**临时使用 */
	public static String getUUIDFaceId()
	{
		seqId++;
		String randomStr = String.valueOf(Math.round(Math.random() * 100));
		String fileNameAppendix
				= "1" + new SimpleDateFormat("MMddHHmmssSSS").format(new Date()) + randomStr  + (seqId%100);
		LOG.info("fileNameAppendix:" + fileNameAppendix);
		return fileNameAppendix;

	}

	/*******************************
	 * 给路径追加一个时间路径
	 * @param filePath 文件路径
	 * @return
	 */
	public static String getPathWithTimeStr(String filePath,String typename)
	{
		String newpath = filePath;
		String filePathAppendix
				= new SimpleDateFormat("yyyyMMdd").format(new Date()) ;
		newpath = filePath + typename + "/" + filePathAppendix + "/";
		return newpath;

	}


	/**
	 * 获得指定文件的byte数组
	 */
	public static  byte[] getBytes(String filePath) throws IOException {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null != fis){
			    fis.close();
			}
			if(null != bos) {
				bos.close();
			}
		}
		return buffer;
	}


	/*********************************
	 * 获取当前的动态检测图像的表名
	 * @return
	 ********************************/
	public static String getCurrentObjectTableMonthName()
	{
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(new Date()) ;

		return "mars_collect.t_object_" + fileNameAppendix + "00";

	}


	public static String getTimeStdString(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) ;
		return str;
	}

	public static String getTimeString(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyyMMddHHmmss").format(date) ;
		return str;
	}

	public static String getTimeStdStringMonth(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyyMM").format(date) ;
		return str;
	}

	public static String getTimeStdFaceEsName(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyyMM").format(date) ;
		return "face_" + str;
	}

	public static String getRelationTableNameNoSchema(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyyMM").format(date) ;
		return "t_relation_" + str + "00";
	}

	public static String getObjectTableNameNoSchema(Date date)
	{
		if(null == date)
		{
			return null;
		}
		String str = new SimpleDateFormat("yyyyMM").format(date) ;
		return "t_object_" + str + "00";
	}


	public static String getNextObjectTableMonthName(int next)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH,next);
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(calendar.getTime()) ;

		return "mars_collect.t_object_" + fileNameAppendix + "00";

	}

	/*********************************
	 * 获取当前的动态检测图像的表名
	 * @return
	 ********************************/
	public static String getObjectTableMonthNameByTime(Date time)
	{
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(time) ;

		return "mars_collect.t_object_" + fileNameAppendix + "00";

	}


    public static String getDateByTime(Date time)
    {
        String fileNameAppendix
                = new SimpleDateFormat("yyyyMMdd").format(time) ;

        return fileNameAppendix;

    }


	public static String getNextRelationTableMonthName(int next)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH,next);
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(calendar.getTime()) ;

		return "mars_collect.t_relation_" + fileNameAppendix + "00";

	}

	/*********************************
	 * 获取当前的动态检测图像的表名
	 * @return
	 ********************************/
	public static String getCurrentFaceIndexName()
	{
		return getFaceIndexNameBytime(new Date()) ;
	}


	/*********************************
	 * 获取下个月
	 * @return
	 ********************************/
	public static String getNextMonthFaceIndexName()
	{
		Calendar cal= Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return getFaceIndexNameBytime(cal.getTime()) ;

	}


	public static String getFaceIndexNameBytime(Date date)
	{
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(date) ;
		return "face_" + fileNameAppendix ;

	}




	/*********************************
	 * 获取当前的动态检测的关系表的表名
	 * @return
	 ********************************/
	public static String getCurrentRelationTableMonthName()
	{
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(new Date()) ;

		return "mars_collect.t_relation_" + fileNameAppendix + "00";

	}


	public static String getRelationTableMonthNameByTime(Date time)
	{
		String fileNameAppendix
				= new SimpleDateFormat("yyyyMM").format(time) ;

		return "mars_collect.t_relation_" + fileNameAppendix + "00";

	}


	public static List<String> getRelationTablesTables(Date startTime, Date endTime )
	{
		List<String> tableNameList = new ArrayList<>();

		String tableName = ImageUtil.getRelationTableMonthNameByTime(startTime);
		tableNameList.add(tableName);
		tableName = ImageUtil.getRelationTableMonthNameByTime(endTime);
		if(!tableNameList.contains(tableName))
		{
			tableNameList.add(tableName);
		}

		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startTime);
			while(calendar.getTime().before(endTime)){
				tableName = ImageUtil.getRelationTableMonthNameByTime(calendar.getTime());
				if(!tableNameList.contains(tableName))
				{
					tableNameList.add(tableName);
				}
				calendar.add(Calendar.MONTH, 1);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return tableNameList;
	}

	public static  List<String> buildFaceIndices(Date startTime,Date endTime )
	{
		List<String> indexNameList = new ArrayList<>();
		String indexName = ImageUtil.getFaceIndexNameBytime(startTime);
		indexNameList.add(indexName);
		indexName = ImageUtil.getFaceIndexNameBytime(endTime);
		if(!indexNameList.contains(indexName))
		{
			indexNameList.add(indexName);
		}

		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startTime);
			while(calendar.getTime().before(endTime)){
				indexName = ImageUtil.getFaceIndexNameBytime(calendar.getTime());
				if(!indexNameList.contains(indexName))
				{
					indexNameList.add(indexName);
				}
				calendar.add(Calendar.MONTH, 1);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return indexNameList;
	}


	//base64字符串转byte[]
	public static byte[] base64String2ByteFun(String base64Str){
		return Base64.decodeBase64(base64Str);
	}


	public static InputStream getinputurlstream(String photo)
	{
		InputStream instream = null;
		try
		{
			URL url = new URL(photo);
			if(photo.startsWith("https")) {
				HTTPSTrustManager.retrieveResponseFromServer(photo);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko");
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(2500);
				conn.setReadTimeout(2500);
				int code = conn.getResponseCode();
				if (200 == code) {
					instream = conn.getInputStream();
				}
			}else{
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				//conn.setUseCaches();
				//conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(3000);
				conn.setReadTimeout(3000);
				int code = conn.getResponseCode();
				if (200 == code) {
					instream = conn.getInputStream();
				}
			}
		}
		catch(Exception e)
		{
			LOG.warn("getinputurlstream photo:" + photo);
			LOG.warn("getinputurlstream exception:",e);
			return null;
		}
		return instream;
	}


	public static byte[] urlTobyte(String localUrl) throws MalformedURLException {
		String url = getInvalidUrl(localUrl);
		BufferedInputStream in = null;
		ByteArrayOutputStream out = null;
		InputStream inputStream = null;
		byte[] content = null;
		try {
			inputStream = getinputurlstream(url);
			if(null == inputStream){
				LOG.warn("util download url failed:" + url);
				return null;
			}
			in = new BufferedInputStream(inputStream);
			out = new ByteArrayOutputStream(1024);
			byte[] temp = new byte[1024];
			int size = 0;
			while ((size = in.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
			content = out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("urlTobyte exception:" ,e);

		} finally {
				if(null != in) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(null != out) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(null != inputStream) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

		}
		return content;
	}

	public static void setProxyUrl(String url){
		if(!url.startsWith("http")){
			proxyUrl = "http://" + url;
		}else {
			proxyUrl = url;
		}
	}
	public static String getProxyUrl(){
		return proxyUrl;
	}

	public static String getInvalidUrl(String url){
		String respUrl = url;
		if(!url.startsWith("http")){
			respUrl = proxyUrl + url;
		}
		return respUrl;
	}

	/**
	 * 取得图片数据
	 * @param url
	 * @return
	 */
	public static byte[] getImageData(String url){
		try {
			String imgUrl = getInvalidUrl(url);
			byte[] imgBin = ImageUtil.urlTobyte(imgUrl);
			return imgBin;
		}
		catch (Exception ex){
			ex.printStackTrace();
			LOG.error(ex);
		}
		return null;
	}


	/***********
	 * 剪切图片
	 * @param srcpath
	 * @param subpath
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public static void cut(String srcpath,String subpath,int x,int y,int width,int height) throws IOException{

		FileInputStream is = null ;
		ImageInputStream iis =null ;

		try{
			//读取图片文件
			is = new FileInputStream(srcpath);

			/**//*
			 * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader
			 * 声称能够解码指定格式。参数：formatName - 包含非正式格式名称 .
			 *（例如 "jpeg" 或 "tiff"）等。
			 */
			String suffix = srcpath.substring(srcpath.lastIndexOf(".")+1);


			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(suffix);
			ImageReader reader = it.next();
			//获取图片流
			iis = ImageIO.createImageInputStream(is);

			/**//*
			 * <p>iis:读取源.true:只向前搜索 </p>.将它标记为‘只向前搜索’。
			 * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader
			 *  避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
			 */
			reader.setInput(iis,true) ;

			/**//*
			 * <p>描述如何对流进行解码的类<p>.用于指定如何在输入时从 Java Image I/O
			 * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件
			 * 将从其 ImageReader 实现的 getDefaultReadParam 方法中返回
			 * ImageReadParam 的实例。
			 */
			ImageReadParam param = reader.getDefaultReadParam();

			/**//*
			 * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
			 * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
			 */
			Rectangle rect = new Rectangle(x, y, width, height);


			//提供一个 BufferedImage，将其用作解码像素数据的目标。
			param.setSourceRegion(rect);

			/**//*
			 * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将
			 * 它作为一个完整的 BufferedImage 返回。
			 */
			BufferedImage bi = reader.read(0,param);

			//保存新图片
			ImageIO.write(bi, "jpg", new File(subpath));
		}

		finally{
			if(is!=null) {
				is.close();
			}
			if(iis!=null) {
				iis.close();
			}
		}

	}


	/***
	 * 将url转成base64
	 * @param sUrl
	 * @return
	 */
	public static String getImgUrlBase64Body(String sUrl){
		String imgUrl = getInvalidUrl(sUrl);
		URL url = null;
		InputStream is = null;
		ByteArrayOutputStream outStream = null;
		HttpURLConnection httpUrl = null;
		try{
			url = new URL(imgUrl);
			httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			httpUrl.getInputStream();
			is = httpUrl.getInputStream();
			outStream = new ByteArrayOutputStream();
			//创建一个Buffer字符串
			byte[] buffer = new byte[1024];
			//每次读取的字符串长度，如果为-1，代表全部读取完毕
			int len = 0;
			//使用一个输入流从buffer里把数据读取出来
			while( (len=is.read(buffer)) != -1 ){
				//用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
				outStream.write(buffer, 0, len);
			}
			// 对字节数组Base64编码
			return Base64Util.encode(outStream.toByteArray());
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("get imgUrl body failed:"+ e.getMessage());
		}

		finally{
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(httpUrl != null) {
				httpUrl.disconnect();
			}
		}
		return imgUrl;

	}

	/**
	 * 图片压缩
	 * @param imageData 原图片数据
	 * @param scale 缩放比例
	 * @param quality 输出的质量
	 * @return
	 * @throws IOException
	 */
	public static byte[] imageCompress(byte[] imageData,float scale, float quality) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(inputStream).scale(scale).outputQuality(quality).outputFormat("jpg").toOutputStream(outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * 图片调整大小
	 * @param imageData 原始图片
	 * @param resizeH 调整后的高
	 * @param resizeW 调整后的宽
	 * @return
	 * @throws IOException
	 */
	public static byte[] imageResize(byte[] imageData,int resizeH,int resizeW) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BufferedImage sourceImg = ImageIO.read(inputStream);
		int width = sourceImg.getWidth();
		int height = sourceImg.getHeight();
		if (width <= resizeW && height <= resizeH){
			//比较小，不用resize大小
			return imageData;
		}

		float scaleH = (float) resizeH / height;
		float scaleW = (float) resizeW / width;
		float scale = Math.min(scaleH,scaleW);
		ByteArrayInputStream inputStream2 = new ByteArrayInputStream(imageData);
		Thumbnails.of(inputStream2).scale(scale).toOutputStream(outputStream);
		return outputStream.toByteArray();
	}

	public static byte[] pngCompress(byte[] imageData) throws IOException {
		/*** bufferedImage 转化为文件* @param bufferedImage* @param ext 图片的格式：png*
		 *
		 */
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PngCompressor.compress(inputStream,outputStream);
		return outputStream.toByteArray();
	}


	public static void main(String[] args) throws ClientProtocolException, IOException {

		byte[] bytes = base64String2ByteFun("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");
		File file = new File("d:/data/mars/picdata/2.jpg");
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(bytes,0,bytes.length);
		fileOutputStream.close();
		System.out.println("****welcome use demo******");
	}
}
