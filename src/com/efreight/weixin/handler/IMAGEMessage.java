package com.efreight.weixin.handler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;

import com.efreight.weixin.WXMessageLogHelper;

/**
 * �����û�����ͼƬ����
 * @author xianan
 *
 */
public class IMAGEMessage extends WXMessageHandler {

	//test
//	private static String imageDir = "D:/tomcat/webapps/wximage/";
//	private static String imageDir = "/usr/share/tomcat6/webapps/wximage/";
	/**
	 * �����û��ϴ�ͼƬ������·��
	 */
	private static String imageDir = "/datadisk/tomcat/webapps/wximage/";
//	private static String imageDir = "/etc/";
	/**
	 * Ψһ���췽��
	 * @param doc ΢�����������xmlת����dom4j��Document���͡�
	 * @param url 
	 */
	public IMAGEMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	/**
	 * ����ͼƬ��Ϣ 
	 * Ŀ¼�ṹ
	 * ��--
	 * 	 ��|--
	 *    ��|--
	 *    	  |--�ļ�1
	 *    	  |--�ļ�2
	 *    	  |--......	 
	 * @return String
	 */
	public String process(){
		String imageUrl = doc.selectSingleNode("//PicUrl").getText();
		String openId= doc.selectSingleNode("//FromUserName").getText();
		String msgId = doc.selectSingleNode("//MsgId").getText();
		if(imageUrl!=null&&!"".equals(imageUrl)){
			URL url;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date nowDate = new Date();
			String now = format.format(nowDate);
			try {
				url = new URL(imageUrl);
				DataInputStream dis = new DataInputStream(url.openStream());
				//�����ļ���
				String dir = imageDir+now.substring(0,4)+"/"+now.substring(5,7)+"/"+now.substring(8,10)+"/";
				File dirf = new File(dir);
				if (!dirf.exists()){
					dirf.mkdirs();
				}
				// ����һ���µ��ļ�
				File f = new File(dir+openId+"_"+msgId+".jpg");
				if(!f.exists()){
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f);
				byte[] buffer = new byte[1024];
				int length;
				// ��ʼ�������
				while ((length = dis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}
				dis.close();
				fos.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//������Ϣ��־��image ��ͼƬ����
		new Thread(new WXMessageLogHelper(doc, false, "true","IMAGE")).start();//������־
		return null;
	}
}
