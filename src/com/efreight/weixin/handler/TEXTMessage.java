package com.efreight.weixin.handler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.process.HELPProcess;
import com.efreight.weixin.process.WXProcess;
import com.efreight.weixin.process.WXProcessHandler;
import com.sun.xml.internal.fastinfoset.sax.Properties;

/**
 * �����û����͹������ı���Ϣ���̳�WXMessageHandler��
 * @author xianan
 *
 */
public class TEXTMessage extends WXMessageHandler {
	/**
	 * code���ԣ��ж��û�����ָ����������ɺ����Ϣ�����˵�ʹ�ã�
	 */
	private String code = "";

	/**
	 * Ψһ���췽��
	 * @param doc ΢�����������xmlת����dom4j��Document���͡�
	 * @param url 
	 */
	public TEXTMessage(Document doc, String url) {
		this.doc = doc;
		this.url = url;
	}

	/**
	 * ����Ϊ��
	 * 1����checkTextMessageSynOrAsyn�������ж�ָ�
	 * 2����ָ����ò�ͬ��Ĳ�ͬ������
	 * 3�������ָ���ȷ������Ϊ��ָ����Ϣ���ظ������ĵ���
	 * @return String
	 */
	public String process() {
//		long timeStart = System.currentTimeMillis();
		System.out.println("!!!===1");
		try {
			this.response.setContentType("text/html; charset=utf-8");
			this.response.setCharacterEncoding("UTF-8");
			this.response.getOutputStream().write(" ".getBytes("UTF-8"));
			this.response.getOutputStream().flush();
			this.response.getOutputStream().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String message = "";
		String messagetype = doc.selectSingleNode("//MsgType").getText();
		if(messagetype.equalsIgnoreCase("voice")){
			message = doc.selectSingleNode("//Recognition").getText();
			WXInfoDownloader util = new WXInfoDownloader();
			String openid= doc.selectSingleNode("//FromUserName").getText();
			try {
				util.SendWXTextMessageWithCustomAPI(openid, "����������Ϣ����Ϊ��" + message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			message = doc.selectSingleNode("//Content").getText();
		try{
			message=message.trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("!!!===2");
		String responseXML = null;
		// ��ͨ�������ļ��ж���ͬ�������첽
		List<String> clearMessage = new ArrayList<String>(); 
		String result = this.checkTextMessageSynOrAsyn(message,clearMessage);// �жϷ�������Ӧ������ĳ��
		System.out.println("!!!===3" + result);
		if (result != null && !"".equals(result) && result.indexOf('|') > 0) {
			String[] cm = result.split("\\|");
			Class<WXProcess> target = null;
			try {
				target = (Class<WXProcess>) Class
						.forName("com.efreight.weixin.process."
								+ cm[0].toUpperCase() + "Process");
				Constructor<?> con = target.getConstructor(Document.class);
				WXProcess handler = (WXProcess) con.newInstance(doc);
				if(clearMessage!=null&&clearMessage.size()>0){
					if(clearMessage.get(0)!=null&&!"".equals(clearMessage.get(0))){
						handler.setClearMessage(clearMessage.get(0));
					}
				}
				handler.command = result;
				if (cm[1].equals("snycProcess")) {// ͬ����Ϣ
					
					responseXML = handler.snycProcess();
				} else {// �첽��Ϣ
					handler.asnycProcess();
				}
			} catch (Exception e) {
				System.out.println("create Handler error.");
//				new Thread(new WXMessageLogHelper(doc, false, true, "UNKNOW")).start();// ������־δ֪��Ϣ����
				e.printStackTrace();
			}
			//���ﱣ�������Ƶ��ڲ�
		}else{
			new Thread(new WXMessageLogHelper(doc, false, "true", "UNKNOW")).start();// ������־δ֪��Ϣ����
			HELPProcess helpProcess = new HELPProcess(doc);
			helpProcess.setErrorMsgType("NORMAL");
			helpProcess.setMsgType("UNKNOW");
//			responseXML = helpProcess.errorProcess();
		}
		System.out.println("*************************");
		System.out.println(responseXML);
		if(responseXML != null && !"".equals(responseXML)){
			WXAPIServiceProcess process = new WXAPIServiceProcess();
			process.process(responseXML);
		}
		return "";
	}

	/**
	 * �����Ƚ�����Ϣת��������������ת���ɰ��������֡�
	 * �ж���Ϣָ���Ƿ��ɿո��֡��ո�ǰΪָ�ָ����com.efreight.commons�����command.properties��Ŀǰһ����tact help sub y m ���� ���� �⼸��ָ���
	 * ���û��ָ����ж���Ϣ���Ƿ���11λ���֣������11λ���֣����ж�Ϊ�˵�/������ѯ��
	 * @param message ΢���û��ϴ�����Ϣ��
	 * @param clearMessage ����ת�������ָ�
	 * @return ���ش���ָ���ʽΪxxxx|yyyyyy|zzzz   x����ת��ĳ����ȥ���� y����x��y��������z��������ĳ����������     
	 */
	private String checkTextMessageSynOrAsyn(String message, List<String> clearMessage) {// ����ָ�����String
																// ��ʽΪ
																// className|methodName
		String msg = message.toUpperCase();
		Enumeration<Object> keys = PropertiesUtils.commandProperty.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if(msg.startsWith(key)) {
				return PropertiesUtils.readValue("", key);
			}
				
		}

		Pattern shippingReg = Pattern.compile("^.[a-zA-Z]{3,}");
		Matcher shippingMatcher = shippingReg.matcher(msg);
		if(shippingMatcher.find()) {
			return "SHIPPING|snycProcess|SHIPPING";
		}
		
		String returnMessage = "";
		returnMessage = this.expressInfo(msg);
		if(returnMessage != null && !"".equals(returnMessage)) {
			return returnMessage;
		}
		code = message.replaceAll("һ", "1").replaceAll("Ҽ", "1")
				.replaceAll("��", "2").replaceAll("��", "2").replaceAll("��", "3")
				.replaceAll("��", "3").replaceAll("��", "4").replaceAll("��", "4")
				.replaceAll("��", "5").replaceAll("��", "5").replaceAll("��", "6")
				.replaceAll("½", "6").replaceAll("��", "7").replaceAll("��", "7")
				.replaceAll("��", "8").replaceAll("��", "8").replaceAll("��", "9")
				.replaceAll("��", "9").replaceAll("��", "0");
		Pattern regex = Pattern.compile("\\D");
		Matcher matcher = regex.matcher(code);
		code = matcher.replaceAll("");
		message = message.trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(code);  
		if (message != null) {
//			String key = null;
//			if (message.indexOf(" ") > 0) {// ˵������ָ��
//				key = message.substring(0, message.indexOf(" ")).toUpperCase();
//			} else {
//				key = message.toUpperCase();
//			}
//			returnMessage = PropertiesUtils.readValue("", key);
//			if (returnMessage != null && !"".equals(returnMessage)) {
//				return returnMessage;
//			}
			if (m.matches()) {// �����ж�11λ�˵��ţ��������� ��ʽΪ��xxx-xxxxxxxx����Ϊxxxxxxxxxxx
				returnMessage = "TRACE|asnycProcess|TRACE";
				clearMessage.add(code);
			} else {
				returnMessage = "";
			}
			System.out.println("start airport");
			p = Pattern.compile("^[a-zA-Z]{3}$"); 
			m = p.matcher(message);  
			if(m.matches()){// �����ж��Ƿ��Ǹۿ�
				System.out.println("airport is true");
				returnMessage = "AIRPROTANDCITY|snycProcess|TACT";
			}
		}
		return returnMessage;
	}

	private static String expressInfo(String msg) {
		Pattern regex = Pattern.compile("[\u4E00-\u9FA5]*");
		Matcher matcher = regex.matcher(msg);
		String m = "";
		String expkey = "";
		if (matcher.find()) {
			m = matcher.group(0);
			if(!m.equals("")){
				expkey = msg.substring(msg.indexOf(m)+m.length());
			}else {
				int size = 3;
				if(msg.toLowerCase().contains("fedex")){
					size=5;
				}else if (msg.toLowerCase().contains("usps")) {
					size =4;
				}
			
					m = msg.substring(0,size);
					expkey = msg.substring(size);
			}
		}
		m = m.replaceAll("���|����|����|���|�ٵ�|����|����|����", "");
		if (m.length() < 2)
			return null;
		Enumeration<Object> keys = PropertiesUtils.expressProperty.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.toLowerCase().contains(m.toLowerCase()))
				return "EXPRESS|snycProcess|EXPRESS|"
						+ PropertiesUtils.expressProperty.getProperty(key)
						+ "|" + key + "|"
						+ expkey;
		}
	
		return null;
	}
	public static void main(String[] args) {
//		String s = "dhl394720348";
//		System.out.println(expressInfo(s));

		
	}
}
