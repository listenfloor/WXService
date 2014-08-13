package com.efreight.weixin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import com.efreight.SQL.InsertMessageLog;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.CommUtil;
import com.efreight.commons.DocumentHelper;
import com.efreight.commons.PropertiesUtils;
import com.efreight.subscribe.MessagePushOperator;
import com.efreight.weixin.process.TRACEProcess;
import com.efreight.weixin.process.WXProcessHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * ��Ϣ������
 * @author xianan
 *
 */
public class WXAPIServiceProcess {
	private Document serviceDoc;
	private static WXInfoDownloader instance;
	private static Logger log = Logger.getLogger(WXAPIServiceProcess.class);
	static {
		if (instance == null) {
			WXInfoDownloader instance1 = new WXInfoDownloader();
			Thread thread = new Thread(instance1);
			thread.start();
			instance = instance1;
		}
	}

	public WXAPIServiceProcess() {
		if (instance == null) {
			WXInfoDownloader instance1 = new WXInfoDownloader();
			instance = instance1;
		}
	}

	/**
	 * ͳһ���÷���������xml��ͨ��<ServiceAction>�ڵ��е�ֵȥ��̬�ĵ��ò�ͬ����
	 * @param xml
	 * @return String
	 */
	public String process(String xml) {
		try {
			log.info(xml);
			this.serviceDoc = org.dom4j.DocumentHelper.parseText(xml);
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String methodName = DocumentHelper.getNodeText(this.serviceDoc,
				"//ServiceAction");
		try {
			Method method = this.getClass().getMethod(methodName);
			String returnMessage = (String) method.invoke(this);
			return returnMessage;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * �����û�
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String transaction() throws Exception {
		String fakeId = DocumentHelper.getNodeText(this.serviceDoc, "//fakeid");
		String nickName = DocumentHelper.getNodeText(this.serviceDoc,
				"//nickname");
		String wxid = DocumentHelper.getNodeText(this.serviceDoc, "//wxid");
		String openid = DocumentHelper.getNodeText(this.serviceDoc, "//openid");
		String country = "";
		String province = "";
		String city = "";
		String headimg = "";
		String language = "";
		String lastupdate = "";
		String subscribe = "";
		try{
			 country = DocumentHelper.getNodeText(this.serviceDoc, "//country");
			 province = DocumentHelper.getNodeText(this.serviceDoc, "//province");
			 city = DocumentHelper.getNodeText(this.serviceDoc, "//city");
			 headimg = DocumentHelper.getNodeText(this.serviceDoc, "//headimg");
			 language = DocumentHelper.getNodeText(this.serviceDoc, "//language");
			 lastupdate = DocumentHelper.getNodeText(this.serviceDoc, "//lastupdate");
			 subscribe = "subscribe";//DocumentHelper.getNodeText(this.serviceDoc, "//userstatus");
		}catch(Exception e){
		}
		System.out.println(openid);
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		WXUserinfo userinfo = (WXUserinfo) sqlMap.queryForObject(
				"getwxuserinfobyfakeid", fakeId);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (userinfo == null || userinfo.getFakeid() == null
				|| "".equals(userinfo.getFakeid())) {
			// ����
			Map<String, String> map = new HashMap<String, String>();
			map.put("FAKEID", fakeId);
			map.put("NICKNAME", nickName);
			map.put("WXID", wxid);
			map.put("CREATEDATE", format.format(new Date()));
			map.put("OPENID", openid);
			map.put("COUNTRY", country);
			map.put("PROVINCE", province);
			map.put("CITY", city);
			map.put("HEADIMG", headimg);
			map.put("LANGUAGE", language);
			map.put("LASTUPDATE", lastupdate);
			map.put("USERSTATUS", subscribe);
			sqlMap.insert("insertuserinfo", map);
		} else {
			// ����
			Map<String, String> map = new HashMap<String, String>();
			map.put("NICKNAME", nickName);
			map.put("WXID", wxid);
			if(openid!=null&&!"".equals(openid)&&!"null".equals(openid)){
				map.put("OPENID", openid);
			}
			map.put("FAKEID",fakeId);
			map.put("COUNTRY", country);
			map.put("PROVINCE", province);
			map.put("CITY", city);
			map.put("HEADIMG", headimg);
			map.put("LANGUAGE", language);
			map.put("LASTUPDATE", lastupdate);
			map.put("USERSTATUS", subscribe);
			sqlMap.update("updateuserinfobyfakeid", map);
		}
		return "";
	}

	/**
	 * ������Ϣ
	 * @return String
	 * @throws Exception
	 */
	public String SendWXMessage() throws Exception {
		String message = DocumentHelper.getNodeText(this.serviceDoc,
				"//message");
		String wxfakeid = DocumentHelper.getNodeText(this.serviceDoc,
				"//wxfakeid");
		String fileid = DocumentHelper.getNodeText(this.serviceDoc, "//fileid");
		String type = DocumentHelper.getNodeText(this.serviceDoc, "//type");
		String result = "";
		if (type != null) {
			if ("1".equals(type)) {
				result = instance.SendWXTextMessage(wxfakeid, message);
			} else if ("2".equals(type) || "3".equals(type) ||"4".equals(type)) {// ���﷢�͵����ļ�
				result = instance.SendWXFileMessage(wxfakeid, type, fileid);
			}
		}
		// ���̷߳�log �������Ϣmsgtype Ϊreply ����˼���˹��ظ�����Ϣ
		new Thread(new WXMessageLogHelper(
				org.dom4j.DocumentHelper.parseText(DocumentHelper
						.asXML(this.serviceDoc)), true, result, "SERVICE"))
				.start();
		return (result!=null&&(result.contains("\"msg\":\"ok\"")||result.contains("preview send success"))) ? "���ͳɹ�" : "����ʧ��";
	}

	/**
	 * �����ı���Ϣ
	 * @return String
	 * @throws Exception
	 */
	public String SendWXTextMessage() throws Exception {
		String wxid = DocumentHelper.getNodeText(this.serviceDoc, "//openid");
		String message = DocumentHelper.getNodeText(this.serviceDoc,
				"//message");
		String result = instance.SendWXTextMessageWithCustomAPI(wxid, message);
		boolean success = result!=null&&result.contains("\"errmsg\":\"ok\"");
		ResendResult(success);
		// WXAPIService.instance.GetWXIdByFakeId(wxid);
		// ���̷߳�log
		new Thread(new WXMessageLogHelper(
				org.dom4j.DocumentHelper.parseText(DocumentHelper
						.asXML(this.serviceDoc)), true, result, null)).start();
		return success? "���ͳɹ�" : "����ʧ��";
	}

	private void ResendResult(boolean result) throws SQLException {
		String messageid = DocumentHelper.getNodeText(this.serviceDoc, "//MessageId");
		if(messageid != null && !"".equals(messageid) && result) {
			System.out.println(messageid);
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			
			sqlMap.update("resendsuccess",Integer.parseInt(messageid));
		}
	}

	/**
	 * ����ID�����ı���ͼƬ��Ϣ
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String SendWXImageAndTextMessageWithID() throws Exception {
		String msgid = DocumentHelper.getNodeText(this.serviceDoc, "//msgid");
		String wxid = DocumentHelper.getNodeText(this.serviceDoc, "//wxfakeid");
		String result = instance.SendWXImageAndTextMessageWithId(msgid, wxid);
		boolean success = result!=null&&result.contains("\"msg\":\"ok\"");
		ResendResult(success);
		new Thread(new WXMessageLogHelper(this.serviceDoc, true, result, null))
				.start();
		return success ? "���ͳɹ�" : "����ʧ��";
	}

	/**
	 * ����ͼƬ���ı���Ϣ
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String SendWXImageAndTextMessage() throws Exception {
		String wxid = DocumentHelper.getNodeText(this.serviceDoc, "//wxfakeid");
		List<Node> messages = DocumentHelper.getNodeList(this.serviceDoc,
				"//messageitem");
		String messageid = DocumentHelper.getNodeText(this.serviceDoc,
				"//messageid");
		List<Map<String, String>> contents = new ArrayList<Map<String, String>>();
		for (int i = 0; i < messages.size(); i++) {
			Node node = messages.get(i);

			String title = DocumentHelper.getNodeText(node, "title");
			String content = new String(
					(new sun.misc.BASE64Decoder()).decodeBuffer(DocumentHelper
							.getNodeText(node, "content")), "UTF-8");
			String digest = DocumentHelper.getNodeText(node, "digest");
			String fileid = DocumentHelper.getNodeText(node, "fileid");
			Map<String, String> detail = new HashMap<String, String>();
			detail.put("title", title);
			detail.put("content", content);
			detail.put("fileid", fileid);
			detail.put("digest", digest);
			contents.add(detail);
		}
		String result = instance.SendWXImageAndTextMessage(contents, wxid,
				messageid);
		boolean success = result!=null&&result.contains("preview send success");
		ResendResult(success);
		new Thread(new WXMessageLogHelper(this.serviceDoc, true, result, null))
				.start();
		return success? "���ͳɹ�" : "����ʧ��";
	}
	
	public String SendWXImageMessageWithCustomAPI() throws Exception {
		String wxid = DocumentHelper.getNodeText(this.serviceDoc, "//openid");
		String message = DocumentHelper.getNodeText(this.serviceDoc,
				"//message");
		String result = instance.SendWXImageMessageWithCustomAPI(wxid, message);
		boolean success = result!=null&&result.contains("\"errmsg\":\"ok\"");
		ResendResult(success);
		// WXAPIService.instance.GetWXIdByFakeId(wxid);
		// ���̷߳�log
		new Thread(new WXMessageLogHelper(
				org.dom4j.DocumentHelper.parseText(DocumentHelper
						.asXML(this.serviceDoc)), true, result, null)).start();
		return success? "���ͳɹ�" : "����ʧ��";
	}
	
	public String SendWXImageAndTextMessageWithCustomAPI() throws Exception {
		String openid = DocumentHelper.getNodeText(this.serviceDoc, "//openid");
		List<Node> messages = DocumentHelper.getNodeList(this.serviceDoc,
				"//messageitem");
		List<Map<String, String>> contents = new ArrayList<Map<String, String>>();
		for (int i = 0; i < messages.size(); i++) {
			Node node = messages.get(i);

			String title = DocumentHelper.getNodeText(node, "title");
			String content = "";
			if(content != null && !"".equals(content))
				content = new String(
					(new sun.misc.BASE64Decoder()).decodeBuffer(DocumentHelper
							.getNodeText(node, "content")), "UTF-8");
			String digest = DocumentHelper.getNodeText(node, "digest");
			Map<String, String> detail = new HashMap<String, String>();
			detail.put("title", title);
			detail.put("content", content);
			detail.put("digest", digest);
			detail.put("url", DocumentHelper.getNodeText(node, "url"));
			detail.put("imgurl", DocumentHelper.getNodeText(node, "imgurl"));
			contents.add(detail);
		}
		String result = instance.SendWXImageAndTextMessageWithCustomAPI(contents, openid);
		boolean success = result!=null&&result.contains("\"errmsg\":\"ok\"");
		ResendResult(success);
		new Thread(new WXMessageLogHelper(this.serviceDoc, true, result, null))
				.start();
		return success? "���ͳɹ�" : "����ʧ��";


	}
	
	public String SendTracePushMessageWithTemplateAPI() throws Exception {
		String openid = WXInfoDownloader.userWithFakeId.get(DocumentHelper.getNodeText(this.serviceDoc, "//wxfakeid")).openid;
		String awbcode = DocumentHelper.getNodeText(this.serviceDoc, "//awb_code");
		String stardard_data = DocumentHelper.getNodeText(this.serviceDoc, "//stardard_data").replace("\"", "");
		String messagedata = "{\"awbcode\":\""+awbcode+"\"";
//		�����ĵ��˵�{{awbcode.DATA}}���µĹ켣��{{date.DATA}}{{summary.DATA}}{{cargocode.DATA}}{{datasource.DATA}}{{warning.DATA}}{{description.DATA}}{{comment1.DATA}}{{comment2.DATA}}
//		�ظ�"��ϸ"��ֱ�ӻظ��˵��Ų鿴�˵��켣��ϸ��
		int length = stardard_data.getBytes().length;
//		int paraLength = 1;
		if(length > 20) {
//			paraLength = length%20==0?length/20:length/20 +1;
//			paraLength = paraLength >8?8:paraLength;
			int currentPosition = 0;
			int strLength = 0;
			char[] data = stardard_data.toCharArray();
			boolean full = false;
			List<Character> tempData = new ArrayList<Character>();
			for (int i = 0; i < data.length; i++) {
				if(strLength < 20) {
					if((int)data[i]>256 && strLength < 19) {
						strLength += 2;
						tempData.add(data[i]);
					}
					else if((int)data[i] <=256){
						strLength ++;
						tempData.add(data[i]);
					}
					else 
						full = true;
				}else
					full = true;
				
				
				if(full || i == data.length-1) {
					full = false;
					if(i != data.length-1)
					i--;
					String tmp = "";
					for (int j = 0; j < tempData.size(); j++) {
						tmp += tempData.get(j);
					}
					tempData.clear();
					strLength = 0;
					switch (currentPosition) {
					case 0:
						messagedata += ",\"date\":\""+tmp+"\"";
						break;
					case 1:
						messagedata += ",\"summary\":\""+tmp+"\"";
						break;
					case 2:
						messagedata += ",\"cargocode\":\""+tmp+"\"";
						break;
					case 3:
						messagedata += ",\"datasource\":\""+tmp+"\"";
						break;
					case 4:
						messagedata += ",\"warning\":\""+tmp+"\"";
						break;
					case 5:
						messagedata += ",\"description\":\""+tmp+"\"";
						break;
					case 6:
						messagedata += ",\"comment1\":\""+tmp+"\"";
						break;
					case 7:
						messagedata += ",\"comment2\":\""+tmp+"\"";
						break;
	
					}
					currentPosition++;
				}
			}
		}
		messagedata +="}";
		String result = instance.SendWXMessageWithTemplateAPI(openid, messagedata, PropertiesUtils.readProductValue("", "PushMessageTemplateID"));
		boolean success = result != null && result.contains("\"errmsg\":\"ok\"");
		new Thread(new WXMessageLogHelper(this.serviceDoc, true, result, null))
		.start();
		return success?"���ͳɹ�":"����ʧ��";
	}

	/**
	 * ͨ��openid��ѯ�û���wxuserinfo��tostring����������һ��xml
	 * @return String
	 * @throws Exception
	 */
	public String GetWXUserinfoWithOpenid() throws Exception {
		String openid = DocumentHelper.getNodeText(this.serviceDoc, "//openid");
		WXUserinfo result = instance.userWithOpenId.get(openid);
		if(result != null)
			return result.toString();
		String content = DocumentHelper.getNodeText(this.serviceDoc,
				"//content");
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		result = (WXUserinfo) sqlMap.queryForObject(
				"getwxuserinfobyopenid", openid);
		if (result == null) {
//			result = instance.GetWXUserinfoByOpenId(openid, content);
			result = instance.GetWXUserinfoByOpenId(openid);
			if (result != null) {
				result = instance.userWithFakeId.get(result.fakeid);
			}
		}
		return result != null ? result.toString() : "";
	}

	/**
	 * �鿴����״̬
	 * @return String
	 * @throws Exception
	 */
	public String CheckServiceStatus() throws Exception {
		boolean service = instance.GetIndexPage();
		return service ? "������������" : "���������쳣";
	}

	/**
	 * �������� 
	 * @return String
	 * @throws Exception
	 */
	public String ResetWXService() throws Exception {
		boolean resetSuccess = instance.loginToWX();
		return resetSuccess ? "���÷���ɹ�" : "���÷���ʧ��";
	}
	
	/**
	 * ����ʵʱ�����˵���Ϣ��
	 * @return String
	 * @throws Exception
	 */
	public String PushSyncTraceResult() throws Exception {
		System.out.println(this.serviceDoc.asXML());
		Node messageitemNode = this.serviceDoc.selectSingleNode("//messageitem");
		Node wxfakeidNode = this.serviceDoc.selectSingleNode("//wxfakeid");
		String wxfakeid = wxfakeidNode.getText().trim();
		String awbcode = this.serviceDoc.selectSingleNode("//awb_code").getText();
		Node isError = this.serviceDoc.selectSingleNode("//isError");
		String wxMsgId  = "";
		try{
			wxMsgId= this.serviceDoc.selectSingleNode("//wxMsgId").getText();
		}catch(Exception e){
		}
		if(isError!=null&&isError.getText()!=null&&"1".equals(isError.getText())){
			//��������¼
			System.out.println("is error message");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String datetime = format.format(new Date());
			Map<String,String> map = new HashMap<String,String>();
			map.put("datetime", datetime);
			map.put("xml", this.serviceDoc.asXML());
			InsertMessageLog isl = new InsertMessageLog();
			isl.insertErrorMessageLog(map);
			this.serviceDoc.selectSingleNode("//ErrorMsgType").setText("ERROR");
			new Thread(new WXMessageLogHelper(org.dom4j.DocumentHelper.parseText(this.serviceDoc.asXML()), true, "true", null)).start();
			return "";
		}
		this.serviceDoc.selectSingleNode("//ServiceAction").setText("SendWXTextMessage");
		String message = null;
		Node messageNode = null;
		try{
			messageNode = this.serviceDoc.selectSingleNode("//message");
			message = messageNode.getText();
			message = message.trim();
		}catch(Exception e){
		}

		String respStr="";

		String openid = WXInfoDownloader.userWithFakeId.get(wxfakeid).openid;
		if(message.indexOf("���޹켣")>=0){
			this.serviceDoc.selectSingleNode("//openid").setText(openid);
			
			respStr = this.serviceDoc.asXML().replace("PushSyncTraceResult", "SendWXTextMessage");
		}else{
			respStr = WXProcessHandler.GetAwbTraceHtmlData(serviceDoc.asXML(), wxfakeid, openid, wxMsgId, "AWBTRACE",false);
			
			Date inactiveDate = WXInfoDownloader.userActiveDate.get(openid);
			if(inactiveDate != null && new Date().before(inactiveDate)){
				respStr = respStr.replace("SendWXImageAndTextMessage", "SendWXImageAndTextMessageWithCustomAPI");
			}
		}

		if(WXProcessHandler.getAWBMsgTime(wxMsgId)>180000){//�����ʱ���ʹ���־��
			this.serviceDoc.selectSingleNode("//ErrorMsgType").setText("TIMEOUT");
			new Thread(new WXMessageLogHelper(org.dom4j.DocumentHelper.parseText(this.serviceDoc.asXML()), true, "true", null)).start();
			WXProcessHandler.removeAWBMsgTime(wxMsgId);
		}else{
			this.process(respStr);
			TRACEProcess traceprocess = new TRACEProcess();
			
			traceprocess.AddCommand(awbcode, instance.userWithFakeId.get(wxfakeid).getOpenid(), wxfakeid);
		}
		return "";
	}
	
	/**
	 * ����ʵʱ�����˵���Ϣ��
	 * @return String
	 * @throws Exception
	 */
	public String PushSubscribeTraceResult() throws Exception {
		Node messageitemNode = this.serviceDoc.selectSingleNode("//messageitem");
		Node wxfakeidNode = this.serviceDoc.selectSingleNode("//wxfakeid");
		String awbcode = this.serviceDoc.selectSingleNode("//awb_code").getText();;
		MessagePushOperator operator = new MessagePushOperator();
		Map<String, String> dayAndTime = operator.getPushDayAndTime(wxfakeidNode.getText(), awbcode, "WEIXIN");
		boolean avliable = operator.isTimeAvailable(dayAndTime);
		if(!avliable) {
			Date date = operator.getNextAvailableDate(dayAndTime);
			operator.addPushInfoToQueue(awbcode, "WEIXIN", this.serviceDoc.asXML(), date);
			return "";
		}
		this.serviceDoc.selectSingleNode("//ServiceAction").setText("SendWXTextMessage");
		String message = null;
		Node messageNode = null;
		try{
			messageNode = this.serviceDoc.selectSingleNode("//message");
			message = messageNode.getText();
			message.trim();
		}catch(Exception e){
		}
		String wxMsgId="";

		String respStr = WXProcessHandler.GetAwbTraceHtmlData(serviceDoc.asXML(), this.serviceDoc.selectSingleNode("//wxfakeid").getText(), "", wxMsgId, "AWBTRACE",true);

		String openid = WXInfoDownloader.userWithFakeId.get(wxfakeidNode.getText()).openid;

		respStr = respStr.replace("SendWXImageAndTextMessage", "SendTracePushMessageWithTemplateAPI");
		TRACEProcess.updateLastPushAwbCode(openid, awbcode);
		try{
			Document doc = org.dom4j.DocumentHelper.parseText(respStr);
			doc.selectSingleNode("//MsgType").setText("SUBSCRIBE");
			doc.selectSingleNode("//ErrorMsgType").setText("PUSH");
//			System.out.println(doc.selectSingleNode("//MsgType").getText()+"      "+doc.selectSingleNode("//ErrorMsgType").getText());
			this.process(doc.asXML());
		}catch(Exception e){
			
		}
		return "";
	}
	
	public String GetFailureMessages() throws Exception {
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		List<Map> results = sqlMap.queryForList("getfailureMessages",format.format(calendar.getTime()));
		String resultStr = "<root>";
		for (int i = 0; i < results.size(); i++) {
			Map result = results.get(i);
			OPAQUE opaque = (OPAQUE) result.get("XML");
			XMLType data = null;
			if(opaque instanceof XMLType) {
			 data = (XMLType)opaque;
			}else{
				data = XMLType.createXML(opaque);
			}
				 
			String base64Data = CommUtil.getBASE64(data.getStringVal());
			resultStr += "<Message><Content>";
			resultStr += base64Data;
			resultStr += "</Content>";
			resultStr += "<MessageId>";
			resultStr += result.get("MESSAGEID");
			resultStr += "</MessageId></Message>";
		}
		
		resultStr += "</root>";
		return resultStr;
	}
	public String GetAwbQueryException() throws Exception {
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		String messageid = this.serviceDoc.selectSingleNode("//LastMessageId").getText();
		
//		messageid,datetime,receiver,content
		List<Map> results = sqlMap.queryForList("getawbqueryexception",Integer.parseInt(messageid));
		String resultStr = "<root>";
		for (int i = 0; i < results.size(); i++) {
			Map result = results.get(i);
			resultStr += "<Message><messageid>";
			resultStr += result.get("MESSAGEID").toString();
			resultStr += "</messageid>";
			resultStr += "<content>";
			resultStr += result.get("CONTENT").toString();
			resultStr += "</content>";
			resultStr += "<datetime>";
			resultStr += result.get("DATETIME").toString();
			resultStr += "</datetime>";
			resultStr += "<wxnikename>";
			resultStr += WXInfoDownloader.userWithOpenId.get(result.get("RECEIVER")).nickname;
			resultStr += "</wxnikename>";
			resultStr += "</Message>";
		}
		
		resultStr += "</root>";
		return resultStr;
	}
	public String GetNoResponseMessages() throws Exception {
		int minute = Integer.parseInt(this.serviceDoc.selectSingleNode("//NoResponseMinute").getText());

		String messageid = this.serviceDoc.selectSingleNode("//LastMessageId").getText();
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 0-minute);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String,String> args = new HashMap<String, String>();
		args.put("DATETIME", format.format(calendar.getTime()));
		args.put("MESSAGEID", messageid);
		
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		System.out.println("========" + format.format(calendar.getTime()));
		args.put("YESTODAY", format.format(calendar.getTime()));
		List<Map> results = sqlMap.queryForList("GetNoResponseMessages",args);
		String resultStr = "<root>";
		for (int i = 0; i < results.size(); i++) {
			Map result = results.get(i);
			
			resultStr += "<Message><messageid>";
			resultStr += result.get("MESSAGEID").toString();
			resultStr += "</messageid>";
			resultStr += "<awb_code>";
			resultStr += result.get("CONTENT").toString();
			resultStr += "</awb_code>";
			resultStr += "<wxfakeid>";
			resultStr += WXInfoDownloader.userWithOpenId.get(result.get("SENDER")).fakeid;
			resultStr += "</wxfakeid>";
			resultStr += "<wxnikename>";
			resultStr += WXInfoDownloader.userWithOpenId.get(result.get("SENDER")).nickname;
			resultStr += "</wxnikename>";
			resultStr += "<wxopenid>";
			resultStr += result.get("SENDER");
			resultStr += "</wxopenid>";
			resultStr += "<wxmsgid>";
			resultStr += result.get("WXMSGID");
			resultStr += "</wxmsgid>";
			resultStr += "<datetime>";
			resultStr += result.get("DATETIME");
			resultStr += "</datetime>";
			resultStr += "<messageid>";
			resultStr += result.get("MESSAGEID");
			resultStr += "</messageid></Message>";
		}

		resultStr += "</root>";
		return resultStr;
	}
	
	private String GetClearCode(String code) {
		code = code.replaceAll("һ", "1").replaceAll("Ҽ", "1")
				.replaceAll("��", "2").replaceAll("��", "2").replaceAll("��", "3")
				.replaceAll("��", "3").replaceAll("��", "4").replaceAll("��", "4")
				.replaceAll("��", "5").replaceAll("��", "5").replaceAll("��", "6")
				.replaceAll("½", "6").replaceAll("��", "7").replaceAll("��", "7")
				.replaceAll("��", "8").replaceAll("��", "8").replaceAll("��", "9")
				.replaceAll("��", "9").replaceAll("��", "0");
		Pattern regex = Pattern.compile("\\D");
		Matcher matcher = regex.matcher(code);
		code = matcher.replaceAll("");
		return code.substring(0, 3) + "-" + code.substring(3);
	}

	public static void main(String[] args) throws Exception {
//		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL>"
//				+ "<ServiceAction>transaction</ServiceAction>"
//				+ "<ServiceData><WXUserInfo><fakeid>3660475</fakeid><nickname>���ܲ�</nickname>"
//				+ "<wxid>weiweiluke</wxid><openid>omQjGji2ryvc9pFo2TRWT5RCoU9M</openid>"
//				+ "</WXUserInfo></ServiceData></eFreightService>";
		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL>"
				+ "<ServiceAction>SendWXTextMessage</ServiceAction>"
				+ "<ServiceData><WXUserInfo><fakeid>omQjGjtUmFZtgQoQg8KrmYUfct_E</fakeid>"
				+ "<openid>omQjGjtUmFZtgQoQg8KrmYUfct_E</openid>"
				+ "<message>aaaaaa</message>"
				+ "</WXUserInfo></ServiceData></eFreightService>";
//		Document doc = org.dom4j.DocumentHelper.parseText(xml);
		WXAPIServiceProcess pro = new WXAPIServiceProcess();
//		pro.serviceDoc = doc;
		pro.process(xml);
	}
}
