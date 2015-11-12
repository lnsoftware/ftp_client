package com.mysoft.ftp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * ==========================================
 * 描述：使用FtpClient作为FTP工具实现文件的上传和下载
 * ------------------------------------------
 * @author lnsoftware
 * ------------------------------------------
 * @date 2015年11月12日
 * ==========================================
 */
public class CuteFTP {
	
	/** 
	 * 方法描述: 向FTP服务器上传文件 
	 * 
	 * @param host FTP服务器hostname 
	 * @param port FTP服务器端口 
	 * @param account FTP登录账号 
	 * @param password FTP登录密码 
	 * @param ftpDir FTP服务器保存目录 
	 * @param fileName 上传到FTP服务器上的文件名 
	 * @param input 输入流 
	 * @return 成功返回true，否则返回false 
	 */  
	public static boolean uploadFile(String host, int port, String account,
			String password, String ftpDir, String fileName, InputStream input) {
		boolean flag = false;
		FTPClient ftp = new FTPClient();
		try {
			// 登录标示符
			boolean loginFlag = connect(ftp, host, port, account, password);
			if(!loginFlag){
				return false;
			}
			// 设置
			setting(ftp, ftpDir);
			// 上传
			flag = ftp.storeFile(fileName, input);
			// 退出登录
			ftp.logout();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接
				if (ftp.isConnected()) {
					ftp.disconnect();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return flag;
	}
	
	/** 
	 * 方法描述: 向FTP服务器上传文件(多个)
	 * 
	 * @param host FTP服务器hostname 
	 * @param port FTP服务器端口 
	 * @param account FTP登录账号 
	 * @param password FTP登录密码 
	 * @param ftpDir FTP服务器保存目录 
	 * @param files Map类型键为"文件名"值为"文件输入流"
	 * @return 成功返回true，否则返回false 
	 */  
	public static boolean uploadFile(String host, int port, String account,
			String password, String ftpDir, Map<String, InputStream> files) {
		boolean flag = false;
		FTPClient ftp = new FTPClient();
		try {
			// 登录标示符
			boolean loginFlag = connect(ftp, host, port, account, password);
			if(!loginFlag){
				return false;
			}
			// 设置
			setting(ftp, ftpDir);
			// 上传
			if(files != null && files.size() > 0){
				for(String fileName : files.keySet()){
					// 有一个失败就返回false
					flag = ftp.storeFile(fileName, files.get(fileName));
					if(!flag){
						break;
					}
				}
			}
			// 退出登录
			ftp.logout();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接
				if (ftp.isConnected()) {
					ftp.disconnect();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return flag;
	}
	
	/**
	* Description: 从FTP服务器下载文件
	* 
	* @param host FTP服务器hostname
	* @param port FTP服务器端口
	* @param account FTP登录账号
	* @param password FTP登录密码
	* @param ftpDir FTP服务器上的相对路径
	* @param fileName 要下载的文件名
	* @param localDir 下载后保存到本地的路径
	* @return 成功返回true，否则返回false 
	*/ 
	public static boolean downFile(String host, int port, String account,
			String password, String ftpDir, String fileName, String localDir) {
		boolean flag = false;
		FTPClient ftp = new FTPClient();
		try {
			// 登录标示符
			boolean loginFlag = connect(ftp, host, port, account, password);
			if (!loginFlag) {
				return false;
			}
			ftp.changeWorkingDirectory(ftpDir);
			// 列出FTP目录下的所有文件
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ff.getName().equals(fileName)) {
					File localFile = new File(localDir + "/" + ff.getName());

					OutputStream is = new FileOutputStream(localFile);
					ftp.retrieveFile(ff.getName(), is);
					is.close();
				}
			}
			// 退出登录
			ftp.logout();
			flag = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return flag;
	}
	
	/**
	* Description: 从FTP服务器下载文件(返回字节数组)
	* 
	* @param host FTP服务器hostname
	* @param port FTP服务器端口
	* @param account FTP登录账号
	* @param password FTP登录密码
	* @param ftpDir FTP服务器上的相对路径
	* @param fileName 要下载的文件名
	* @param localDir 下载后保存到本地的路径
	* @return 成功返回byte[]，否则返回null 
	*/ 
	public static byte[] getBytes(String host, int port, String account,
			String password, String ftpDir, String fileName) {
		FTPClient ftp = new FTPClient();
		try {
			// 登录标示符
			boolean loginFlag = connect(ftp, host, port, account, password);
			if (!loginFlag) {
				return null;
			}
			ftp.changeWorkingDirectory(ftpDir);
			// 列出FTP目录下的所有文件
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ftp.retrieveFile(fileName, os);
			if(os != null && os.toByteArray().length > 0){
				return os.toByteArray();
			}
			// 退出登录
			ftp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * FTP服务器设置
	 */
	private static void setting(FTPClient ftp, String directory) throws IOException{
		// 如果目录不存在, 则创建
		ftp.makeDirectory(directory);
		ftp.changeWorkingDirectory(directory);
		// 设置缓存大小
		ftp.setBufferSize(1024);
		// 设置编码
		ftp.setControlEncoding("UTF-8");
		// 设置文件类型二进制
		ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
	}
	
	/**
	 * 连接到FTP服务器
	 */
	private static boolean connect(FTPClient ftp, String host, int port, String account,
			String password) throws SocketException, IOException{
		int reply;
		// 连接到FTP服务器
		ftp.connect(host, port);
		// 根据用户和密码登录
		ftp.login(account, password);
		// 返回响应
		reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			return false;
		}
		return true;
	}
	
}
