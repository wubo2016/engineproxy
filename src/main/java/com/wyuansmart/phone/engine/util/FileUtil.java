package com.wyuansmart.phone.engine.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;




public class FileUtil {
	private static Log logger = LogFactory.getLog(FileUtil.class);
	private static final int BUFFER = 2048;
	private static int seqId = 1;
	private FileUtil() {
	}

	//
	public static void unzip(String file, String destFolder) throws IOException {
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(file);
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			if (entry.isDirectory()) {
				File f = new File(destFolder, entry.getName());
				f.mkdirs();
			} else {
				InputStream iis = zipfile.getInputStream(entry);
				is = new BufferedInputStream(iis);
				int count;
				byte data[] = new byte[BUFFER];
				File theFile = new File(destFolder, entry.getName());
				FileOutputStream fos = new FileOutputStream(theFile);
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
				iis.close();
			}
		}
		zipfile.close();
	}


	//
	public static File createEmptyDir(String path) {
		File tempDir = new File(path);
		if (tempDir.exists() && !tempDir.isDirectory()) {
			throw new RuntimeException("file:" + path + " already exists.");
		}
		if (tempDir.exists()) {
			if (!deleteDirectory(tempDir)) {
				throw new RuntimeException("can not delete old dir:" + path);
			}
		}
		if (!tempDir.mkdirs()) {
			throw new RuntimeException("can not create dir:" + path);
		}
		return tempDir;
	}

	//
	public static File createTemporaryDirectory(String prefix) {
		File tempDir = null;
		try {
			tempDir = File.createTempFile(prefix, "");
		} catch (IOException e) {
			throw new RuntimeException("could not create temporary file "
					+ prefix, e);
		}
		boolean success = tempDir.delete();
		if (!success) {
			throw new RuntimeException("could not delete temporary file "
					+ tempDir);
		}
		success = tempDir.mkdir();
		if (!success) {
			throw new RuntimeException("could not create temporary directory "
					+ tempDir);
		}
		return tempDir;
	}

	public static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					// noinspection ResultOfMethodCallIgnored
					file.delete();
				}
			}
		}
		return (directory.delete());
	}

	/**
	 * get content from file
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytes(String filePath) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtil.copy(new FileInputStream(filePath), bos);
		return bos.toByteArray();
	}

	/**
	 * get content from file
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String getContent(String filePath) throws IOException {
		return getContent(new File(filePath));
	}

	/**
	 * get content from file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getContent(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()),
				StandardCharsets.UTF_8);
	}

	//
	public static void saveContent(String content, File file)
			throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(
						content.getBytes())) {
			IOUtil.copy(bis, fos);
		}
	}

	public static void saveContent(byte bb[], String filePath){
		try {
			File file = new File(filePath);
			try (FileOutputStream fos = new FileOutputStream(file);
				 ByteArrayInputStream bis = new ByteArrayInputStream(bb)) {
				IOUtil.copy(bis, fos);
			}
		}catch (Exception e){
			logger.error("save file failed, path:"+filePath,e);
		}

	}

	public static void saveContent(byte bb[], File file) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(bb)) {
			IOUtil.copy(bis, fos);
		}
	}

	//
	public static void copyFileWithProgress(String sourceURI,
			String destFilePath) {
		try {
			logger.info("move file from:" + sourceURI + " to " + destFilePath);
			File destFile = new File(destFilePath);
			URL url = new URL(sourceURI);
			FileOutputStream fos = new FileOutputStream(destFile);
			URLConnection connection = url.openConnection();
			int total = connection.getContentLength();
			logger.info("connection opened,content size:" + getSize(total));
			InputStream is = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int count = 0;
			int n = 0;
			int oldPercent = -1;
			while (-1 != (n = is.read(buffer))) {
				fos.write(buffer, 0, n);
				count += n;
				if (total > 0) {
					int p = count * 10 / total;
					if (p != oldPercent) {
						logger.info("percent:" + p * 10 + "%,downloaded:"
								+ getSize(count) + "/total:" + getSize(total));
						oldPercent = p;
					}
				}
			}
			IOUtil.closeQuietly(is);
			IOUtil.closeQuietly(fos);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	//
	private static String getSize(int bytes) {
		if (bytes > 1024) {
			return bytes / (1024) + "KB";
		}
		return bytes + "b";
	}

	//
	public static long sizeOfPath(Path path) {
		final AtomicLong size = new AtomicLong(0);
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {

					size.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}
				//
				@Override
				public FileVisitResult visitFileFailed(Path file,
						IOException exc) {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException exc) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception e) {
			logger.error("",e);
		}

		return size.get();
	}
	/**
	 * 查找指定目录下指定后缀名的文件,无递归查询
	 *
	 * @param dirPath 目录
	 * @param nameEndsWith 扩展名
	 * @return
	 */
	public static List<String> getFiles(String dirPath, String nameEndsWith)
			throws Exception {
		List<String> fileNames = new ArrayList<String>();
		File directory = new File(dirPath);
		if (!directory.exists()) {
			return fileNames;
		}else if (directory.isFile()) {
			if (directory.getName().endsWith(nameEndsWith)) {
				fileNames.add(directory.getAbsolutePath());
			}
			return fileNames;
		} else {
			File[] children = directory.listFiles();
			for (File child : children) {
				if (child.getName().endsWith(nameEndsWith)) {
					fileNames.add(child.getAbsolutePath());
				}
			}
			return fileNames;
		}
	}

	/**
	 * 查找指定目录下以及子目录下指定后缀名的文件
	 *
	 * @param dirPath 目录
	 * @param nameKeywords 文件名称包含的关键字
	 * @return
	 */
	public static List<String> getAllFiles(String dirPath, String nameKeywords)
			throws Exception {
		List<String> fileNames = new ArrayList<String>();
		getAllFiles(fileNames,dirPath,nameKeywords,655350);
		return fileNames;
	}

	/**
	 * 查找指定目录下以及子目录下指定后缀名的文件
	 *
	 * @param dirPath 目录
	 * @param nameKeywords 文件名称包含的关键字
	 * @param maxSum 最多取多少个文件
	 * @return
	 */
	public static List<String> getAllFiles(List<String> outFileNames,String dirPath, String nameKeywords,long maxSum)
			throws Exception {
		if(outFileNames == null){
			outFileNames = new ArrayList<String>();
		}
		File directory = new File(dirPath);
		if (!directory.exists()) {
			return outFileNames;
		}else if (directory.isFile()) {
			if (!StringUtils.isEmpty(nameKeywords) && directory.getName().indexOf(nameKeywords) >= 0){
				outFileNames.add(directory.getAbsolutePath());
			}else if(StringUtils.isEmpty(nameKeywords)){
				outFileNames.add(directory.getAbsolutePath());
			}
			return outFileNames;
		} else {
			File[] children = directory.listFiles();
			for (File child : children) {
				if (child.isDirectory()){
					getAllFiles(outFileNames,child.getAbsolutePath(),nameKeywords,maxSum);
				}else if(StringUtils.isEmpty(nameKeywords)){
					outFileNames.add(child.getAbsolutePath());
				}else if(!StringUtils.isEmpty(nameKeywords)
						&& child.getName().indexOf(nameKeywords) >= 0){
					outFileNames.add(child.getAbsolutePath());
				}else {

				}

				if (outFileNames.size() >= maxSum) {
					//送到最大取文件数
					return outFileNames;
				}
			}
			return outFileNames;
		}
	}

	/**
	 * 查找指定目录下目录 文件名称（名称字符串比较大小 string compareTo）最大的文件
	 *
	 * @param dirPath 目录
	 * @return
	 */
	public static File getMaxNameFile(String dirPath)
			throws Exception {
		File directory = new File(dirPath);
		if (!directory.exists() || directory.isFile()) {
			return null;
		} else {
			File[] children = directory.listFiles();
			String strName = "";
			File file = null;
			for (File child : children) {
				if (child.getName().compareTo(strName) > 0) {
					strName = child.getName();
					file = child;
				}
			}
			return file;
		}
	}

	/**
	 * 查找指定目录下指定后缀名的文件,无递归查询
	 *
	 * @param dirPath 目录
	 * @param nameEndsWith 文件名最后包括的字符
	 * @param nameContains 名称包含的字符
	 * @return
	 */
	public static List<File> getFiles(String dirPath, String nameEndsWith,String nameContains)
			throws Exception {
		List<File> files = new ArrayList<File>();
		File directory = new File(dirPath);
		if (!directory.exists() || directory.isFile()) {
			return files;
		} else {
			File[] children = directory.listFiles();
			for (File child : children) {
				if (!StringUtils.isEmpty(nameContains) && !child.getName().contains(nameContains)) {
					continue;
				}

				if (!StringUtils.isEmpty(nameEndsWith) && !child.getName().endsWith(nameEndsWith)) {
					continue;
				}
				files.add(child);
			}
			return files;
		}
	}

	/**
	 * 将源文件复制到目标文件
	 *
	 * @param srcFile
	 *            源文件的完整路径
	 * @param destFile
	 *            目标文件的完整路径
	 * @return void
	 */
	public static void copyFile(String srcFile, String destFile)
			throws Exception {
		FileInputStream in = null;
		FileOutputStream out = null;
		BufferedInputStream bufferIn = null;
		BufferedOutputStream bufferOut = null;
		try {
			File f = new File(srcFile);
			in = new FileInputStream(f);
			File outputFile = new File(destFile);
			File outputFolder = outputFile.getParentFile();
			if (!outputFolder.exists()) {
				outputFolder.mkdirs();
			}
			out = new FileOutputStream(outputFile);
			bufferIn = new BufferedInputStream(in);
			bufferOut = new BufferedOutputStream(out);
			byte[] b = new byte[1024];
			int i = 0;
			while ((i = bufferIn.read(b)) != -1) {
				bufferOut.write(b, 0, i);
			}
			bufferOut.flush();
			bufferOut.close();
			out.close();
			bufferIn.close();
			in.close();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			if (bufferIn != null) {
				try {
					bufferIn.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			if (bufferOut != null) {
				try {
					bufferOut.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
	}

	/**
	 * 复制文件
	 *
	 * @param sourceFile
	 *            源文件
	 * @param targetFile
	 *            目标文件
	 * @throws IOException
	 *             产生了异常
	 */
	public static void copyFile(File sourceFile, File targetFile)
			throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null) {
				inBuff.close();
			}
			if (outBuff != null) {
				outBuff.close();
			}
		}
	}

	/**
	 * 复制文件夹
	 *
	 * @param sourceDir
	 * @param targetDir
	 * @throws IOException
	 */
	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {

		logger.info("copyDirectiory sourceDir:"+sourceDir+ " targetDir:"+ targetDir);

		File source = new File(sourceDir);
		if (!source.exists()) {
			return;
		}
		if (source.isFile()) {
			try {
				FileUtil.copyFile(sourceDir, targetDir+File.separator+source.getName());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("",e);
			}
			return;
		}

		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	/**
	 * 判断文件是否被占用
	 *
	 * @param filePath
	 * @return true:未被占用 ;false:已被占用
	 */
	public static boolean isFree(String filePath) throws Exception {
		File f = new File(filePath);
		if (!f.exists()) {
			throw new RuntimeException("The file" + filePath
					+ "is not existent!");
		}
		boolean result = f.renameTo(f);
		return result;
	}

	/**
	 * 剪切源文件到目标文件
	 *
	 * @param srcFile
	 * @param destFile
	 * @throws Exception
	 */
	public static void cutFile(String srcFile, String destFile)
			throws Exception {
		copyFile(srcFile, destFile);
		new File(srcFile).delete();
	}

	/**
	 * 得到文件的字节数组
	 *
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] getFileByteArray(String filePath) throws Exception {
		if (!new File(filePath).exists()) {
			throw new RuntimeException(
					"The file is not exist in specified file path!" + filePath);
		}
		if (new File(filePath).isDirectory()) {
			throw new RuntimeException("The directory path is specified!"
					+ filePath);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		byte[] result = new byte[0];
		try {
			in = new FileInputStream(filePath);
			byte[] b = new byte[1024];
			int i = 0;
			while ((i = in.read(b)) != -1) {
				out.write(b, 0, i);
			}
			result = out.toByteArray();
			in.close();
			out.close();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
		return result;
	}

	/**
	 * 得到文件的32位checksum
	 *
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static int getFileCheckSum(String filePath) throws Exception {
		if (!new File(filePath).exists()) {
			throw new RuntimeException(
					"The file is not exist in specified file path!" + filePath);
		}
		if (new File(filePath).isDirectory()) {
			throw new RuntimeException("The directory path is specified!"
					+ filePath);
		}
		//ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		int sum = 0;
		try {
			in = new FileInputStream(filePath);
			byte[] b = new byte[128*1024];
			int i = 0;
			while ((i = in.read(b)) != -1) {
				//out.write(b, 0, i);
				for (int j = 0; j < b.length; j++) {
					sum+=b[j];
				}
			}
			in.close();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
		return sum;
	}

	/**
	 * 将字节数组写入文件
	 *
	 * @param buffer
	 * @param filePath
	 * @throws Exception
	 */
	public static void writeByteArrayToFile(byte[] buffer, String filePath,
											boolean append) throws Exception {
		OutputStream out = null;
		BufferedOutputStream bufferOut = null;
		try {
			File file = new File(filePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			out = new FileOutputStream(file, append);
			bufferOut = new BufferedOutputStream(out);
			bufferOut.write(buffer);
			bufferOut.flush();
			bufferOut.close();
			out.close();
		} finally {
			if (bufferOut != null) {
				try {
					bufferOut.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
	}


	/**
	 * 删除文件夾
	 *
	 * @param folderPath
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	/**
	 * 删除目录下所有文件，
	 *
	 * @param path
	 *            路径
	 * @return false 目录不存在或非文件夹.
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 删除文件
	 *
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		boolean flag = false;
		File file = new File(filePath);
		if (!file.exists()) {
			return flag;
		} else {
			flag = file.delete();
			return flag;
		}
	}

	/**
	 * 创建目录文件夹
	 *
	 * @param destDirName
	 * @return
	 */
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			return false;
		}
		if (dir.mkdirs()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 创建目录文件夹
	 *
	 * @param destDirName lf add 20160321
	 * @return
	 */
	public static boolean createDir(String destDirName, String fileName) {
		logger.info("createDir destDirName:" + destDirName);
		File dir = new File(destDirName, fileName);

		if (dir.exists()) {
			return false;
		}
		// if (!destDirName.endsWith(File.separator)) {
		// destDirName += File.separator;
		// }
		if (dir.mkdirs()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean CreateFile(String destFileName) throws IOException {
		File file = new File(destFileName);
		if (file.exists()) {
			// System.out.println("创建单个文件" + destFileName + "失败，目标文件已存在！");
			throw new RuntimeException("The file has existed!");
		}
		if (destFileName.endsWith(File.separator)) {
			// System.out.println("创建单个文件" + destFileName + "失败，目标不能是目录！");
			throw new RuntimeException("Create File failure!");
		}
		if (!file.getParentFile().exists()) {
			// System.out.println("目标文件所在路径不存在，准备创建。。。");
			if (!file.getParentFile().mkdirs()) {
				// System.out.println("创建目录文件所在的目录失败！");
				return false;
			}
		}
		// 创建目标文件
		if (file.createNewFile()) {
			// System.out.println("创建单个文件" + destFileName + "成功！");
			return true;
		} else {
			throw new RuntimeException("Create File failure!");
		}
	}

	/**
	 * 得到当前jar包所在的目录
	 *
	 * @return 带文件分隔符后缀
	 */
	public static String getPath() {
		// 当前jar包路径
		String jarPath;
		try {
			jarPath = FileUtil.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath();
		} catch (URISyntaxException e1) {
			throw new RuntimeException("get the path of the common.jar wrong");
		}

		// 单独运行：路径是文件夹的路径
		if (jarPath.lastIndexOf(".ja") == -1) {
			return jarPath;
		} else {
			// 打成jar包路径是含有.jar
			// 当前jar文件
			File jarFile = new File(jarPath);
			// 上层目录
			String path = jarFile.getParent() + File.separator;
			return path;
		}
	}

	/**
	 * 目录下找某个文件，包括子目录下
	 * @param path 目录
	 * @param fileName 要查找的文件名
	 * @return 文件的完整路径
	 */
	public static String findFile(String path,String fileName) {
		try {
			File directory = new File(path);
			if (!directory.exists() || directory.isFile()) {
				return "";
			} else {
				File[] children = directory.listFiles();
				for (File child : children) {
					if (child.getName().equals(fileName)) {
						return child.getPath();
					}else if (child.isDirectory()) {
						//支子目录下找
						String nameString = findFile(child.getPath(),fileName);
						if (!StringUtils.isEmpty(nameString)) {
							return nameString;
						}
					}
				}
				return "";
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("",e);
			return "";
		}
	}

	/**
	 * 目录下找某个文件，不包括子目录下
	 * @param path 目录
	 * @param fileName 要查找的文件名
	 * @return 文件的完整路径
	 */
	public static String findDirectoryFile(String path,String fileName) {
		try {
			File directory = new File(path);
			if (!directory.exists() || directory.isFile()) {
				return "";
			} else {
				File[] children = directory.listFiles();
				for (File child : children) {
					if (child.getName().equals(fileName)) {
						return child.getPath();
					}else if (child.isDirectory()) {
						continue;
					}
				}
				return "";
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("",e);
			return "";
		}
	}

	/**
	 * 取得某个路径可用空间大小
	 * @param path
	 * @return
	 */
	public static long getPathFreeSpace(String path){
		File fileTemp = new File(path);
		if (!fileTemp.exists()) {
			return 0;
		}

		if (fileTemp.getFreeSpace() < 0) {
			return 0;
		}

		return fileTemp.getFreeSpace();
	}

	/**************************
	 * 将input流保存到文件
	 * @param inputStream 文件输入流
	 * @param fileName  目标文件名
	 * @param localPath 目标存储路径
	 * @return
	 */
	public static boolean save2File(InputStream inputStream, String fileName, String localPath) {

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
				return false;
			}
		}
		return true;
	}

	public  static  String getUUIDFileName()
	{
		seqId++;
		String randomStr = String.valueOf(Math.round(Math.random() * 10000));
		String fileNameAppendix
				=  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + randomStr  + (seqId%100);
		return fileNameAppendix;

	}
}
