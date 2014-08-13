package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.weixin.handler.WXMessageHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Servlet implementation class WXFileUploadServlet
 * ����ͼƬ�ϴ���
 */
public class WXFileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// test
//	private static final String filePath = "/Users/xianan/etc/wxUploadImage/";
	private static final String filePath = "/datadisk/tomcat/webapps/wxUploadImage/";
//	private static final String filePath = "/usr/share/tomcat6/webapps/wxUploadImage/";
	private static final String url = "/wxUploadImage/";
	private static String wxurl = "http://mp.weixin.qq.com/cgi-bin/uploadmaterial?cgi=uploadmaterial&type=0&t=iframe-uploadfile&lang=zh_CN&formId=null";
	private static Logger log = Logger.getLogger(WXFileUploadServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WXFileUploadServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response) ÿ������ֻ����1���ļ���
	 * ���Ƚ����ļ���Ȼ�󱣴���������ʱ�ļ���
	 * ֮���ϴ���΢�ŷ��������ϴ��ɹ��󣬷���fileid��΢�ŷ������ϵ��ļ�id����url����ͼƬ·����
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			// �������̹��������ù�����ʵ���ڴ����ݴ���������ʱ����·��
			DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 4,
					new File(filePath + "temp/"));
			// �������ֻ�������ڴ��д洢������,��λ:�ֽ�
			// factory.setSizeThreshold(4096);
			// �����ļ���ʱ�洢·��
			// factory.setRepository(new File("D:\\Temp"));
			// ����һ�µ��ļ��ϴ������ʽ
			ServletFileUpload upload = new ServletFileUpload(factory);
			// ����·�����ļ������ַ���
			upload.setHeaderEncoding("UTF-8");
			// ���������û��ϴ��ļ���С,��λ:�ֽ�
			upload.setSizeMax(1024 * 1024 * 100);
			// �������󣬿�ʼ��ȡ����
			// Iterator<FileItem> iter = (Iterator<FileItem>)
			// upload.getItemIterator(request);
			// �õ����еı�������Ŀǰ��������FileItem
			List<FileItem> fileItems = null;
			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String fileType = null;
			FileItem targetFile = null;
			// ���δ�������
			Iterator<FileItem> iter = fileItems.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					// ���item�������ı���
					String name = item.getFieldName();
					if (name != null && "type".equals(name)) {
						fileType = item.getString("UTF-8"); // ȡ�ϴ����ļ�����
					}
					// System.out.println("������Ϊ:"+name+"����ֵΪ:"+value);
				} else {
					targetFile = item;
					// ���item���ļ��ϴ�����
					// ����ļ�����·��
				}
			}
			if (fileType == null || "".equals(fileType)) {
				System.out.println("the parameter: filetype is null");
				String message = getMessage("false",
						"the parameter: filetype is null", "", "");
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}
			long l = System.currentTimeMillis();
			String fullFileName = null;
			String wxFileId = null;
			int key = 0;
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			if (targetFile != null) {
				String fileName = targetFile.getName();
				// �������ݿ����
				if (fileName != null) {
					// ����ļ��������ϴ�
					File fullFile = new File(targetFile.getName());
					fullFileName = l
							+ fullFile.getName().substring(
									fullFile.getName().lastIndexOf("."));
					File fileOnServer = new File(filePath + fullFileName);
					if (!fileOnServer.exists()) {
						fileOnServer.createNewFile();
					}
					try {
						targetFile.write(fileOnServer);
					} catch (Exception e) {
						String message = getMessage("false",
								"write wxuploadfile error", "", "");
						response.getOutputStream().write(
								message.getBytes("utf-8"));
						response.getOutputStream().flush();
						response.getOutputStream().close();
						e.printStackTrace();
						return;
					}
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					Map<String, String> entryMap = new HashMap<String, String>();
					entryMap.put("fileType", fileType);
					entryMap.put("fileName", fileName);
					entryMap.put("filePath", filePath + fullFileName);
					entryMap.put("fileUrl", url + fullFileName);
					entryMap.put("createDate", format.format(new Date()));
					try {
						key = (Integer) sqlMap.insert("insertwxuploadfile",
								entryMap);// ����
					} catch (SQLException e) {
						String message = getMessage("false",
								"insert wxuploadfile error", "", "");
						response.getOutputStream().write(
								message.getBytes("utf-8"));
						response.getOutputStream().flush();
						response.getOutputStream().close();
						e.printStackTrace();
						return;
					}
					System.out.println("�ļ�" + fileOnServer.getName() + "�ϴ��ɹ�");
				}
				// ����������ɺ󣬿�ʼ�ϴ�΢�ŷ�����
				WXInfoDownloader loader = new WXInfoDownloader();
				try {
					loader.loginToWX();
				} catch (Exception e) {
					System.out.println("login error");
					String message = getMessage("false", "login error", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					e.printStackTrace();
					return;
				}
				Map<String, String> cookies = loader.getCookies();
				String token = loader.getToken();
				System.out.println("token:" + token);
				System.out.println("cookies:" + cookies);
				HttpPostUploadUtil httppostutil = new HttpPostUploadUtil(token,
						cookies);
				// �ϴ���ַһ����΢�ŷ������Ǹ����ļ���ʽ���֡�
				wxurl += "&token=" + token;
				Map<String, String> textMap = new HashMap<String, String>();
				textMap.put("formId", "");
				Map<String, String> fileMap = new HashMap<String, String>();
				fileMap.put("uploadfile", filePath + fullFileName);
				String ret = httppostutil.formUpload(wxurl, textMap, fileMap);
				// �����Ƿ�ɹ���
				Pattern regex = Pattern.compile("formId, '(.*?)'");
				Matcher matcher = regex.matcher(ret);
				if (matcher.find()) {// �ϴ��ɹ�
					wxFileId = matcher.group(1);
				} else {// �ϴ�ʧ��
					System.out.println("file upload to weixin error.");
					String message = getMessage("false",
							"file upload to weixin error.", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					return;
				}
				// ��ʼ�������ݿ�
				Map<String, String> entryMap = new HashMap<String, String>();
				entryMap.put("sys_code", key + "");
				entryMap.put("wxFileId", wxFileId);
				try {
					sqlMap.update("updatewxuploadfile", entryMap);
				} catch (SQLException e) {
					System.out.println("update wxfileid error");
					String message = getMessage("false",
							"update wxfileid error", "", "");
					response.getOutputStream().write(message.getBytes("utf-8"));
					response.getOutputStream().flush();
					response.getOutputStream().close();
					e.printStackTrace();
					return;
				}
				// ��֯��������
				String message = getMessage("true", "success", wxFileId, url
						+ fullFileName);
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			} else {
				System.out.println("file upload is error. the file is null");
				String message = getMessage("false", url + fullFileName, "", "");
				response.getOutputStream().write(message.getBytes("utf-8"));
				response.getOutputStream().flush();
				response.getOutputStream().close();
				return;
			}
		}
	}

	/**
	 * ������ʾ
	 * 
	 * @param message
	 * @return
	 */
	private String getMessage(String status, String message, String fileId,
			String url) {
		StringBuffer sb = new StringBuffer("{\"status\":\"" + status
				+ "\",\"description\":\"" + message + "\" , \"fileid\":\""
				+ fileId + "\" ,\"url\":\"" + url + "\"}");
		return sb.toString();
	}

}
