package com.efreight.weixin.process;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * ����ȡ��������Ϣ��WXProcess��
 * @author xianan
 *
 */
public class USUBProcess extends WXProcess {
	// key:openid,value{key:Y+openid,value:{ʱ��������ı�ʾ���˵���}}������

	/**
	 * ���췽��
	 * @param doc
	 */
	public USUBProcess(Document doc) {
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}

	public String snycProcess() {
		String responseXml = "";
		//�����ж��Ƿ��ǻظ�������Ϣ
		message = message.trim();
		new Thread(new WXMessageLogHelper(doc, false, "true", "USUBSCRIBE")).start();// ������־
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid û�в鵽");
			return "";
		}
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "").replaceAll("USUB", "").replaceAll("ȡ��", "").replaceAll("����", "").trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(message);  
		if(m.matches()){
			try {
				WXProcessHandler.USubAwbTraceData(message.substring(0, 3) + '-' + message.substring(3), wxfakeid, false, this.openId, this.wxMsgId, "USUBSCRIBE");
				responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,  "�˵�" + message + "�ѳɹ�ȡ�����ġ�", openId, this.wxMsgId, "USUBSCRIBE","NORMAL");
//				responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "�˵�" + message + "�ѳɹ�ȡ�����ġ�");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,  "������˵�������", openId, this.wxMsgId, "USUBSCRIBE","NORMAL");
//			responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "�����˵�������");
		}
		return responseXml;
	}

	/**
	 * �첽����
	 * �����ж���Ϣ���Ƿ���y����m�����ڶ�ǰһ�β�ѯ�˵���Ķ��ģ���yΪ΢����Ϣ���ģ�mΪ�ʼ����ġ�
	 * �ж���Ϣ���Ƿ�֮ǰ�������������������ͨ��SubscribeAwb��������֮ǰ����Ϣ��
	 * �������y��m���ж��Ƿ���sub����'����'ָ�� �� 11λ�˵��ţ�ͨ��SubscribeAwb�������ж�ĳ�˵��Ķ��ġ���������򷵻ش���İ�����Ϣ��
	 */
	public void asnycProcess() {
		//�����ж��Ƿ��ǻظ�������Ϣ
		message = message.trim();
		String commandForChains = "";
		
		new Thread(new WXMessageLogHelper(doc, false, "true", "USUBSCRIBE")).start();// ������־
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid û�в鵽");
			return;
		}
		WXUserinfo user = null;
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		try{
			user = WXInfoDownloader.userWithOpenId.get(openId);
		}catch(Exception e){
			try {
				user = (WXUserinfo)sqlMap.queryForObject("getwxuserinfobyopenid",openId);
				WXInfoDownloader.userWithOpenId.put(openId, user);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		WXProcessHandler.getAircompanyList();
		Map<String, Object> command = WXProcessHandler.commandChains.get(commandForChains);
		message = message.toUpperCase().replaceAll(" ", "").replaceAll("-", "").replaceAll("USUB", "").replaceAll("ȡ��", "").replaceAll("����", "").trim();
		Pattern p = Pattern.compile("\\d{11}");  
		Matcher m = p.matcher(message);  
		if(m.matches()){
			try {
				this.USubscribeAwb(openId, message.substring(0, 3) + '-' + message.substring(3), wxfakeid);
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "�����˵�������", openId,this.wxMsgId,"USUBSCRIBE","USUBFAIL");
			WXAPIServiceProcess service = new WXAPIServiceProcess();
			boolean success = service.process(responseXml).contains("���ͳɹ�");
		}
	}

	/**
	 * ����
	 * 
	 * @param openid �û���ƽ̨��΢�ŵ�Ψһ��Ӧ��ϵ
	 * @param message ������
	 * @param wxfakeid �û�Ψһid
	 * @throws Exception
	 * @throws DocumentException
	 */
	private void USubscribeAwb(String openid, String message, String wxfakeid) throws Exception, DocumentException {
		String responseXml = "";
		if (message.length() == 12) {
			WXProcessHandler.GetAwbTraceData(message, wxfakeid, false, openid, this.wxMsgId, "USUBSCRIBE",null);
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "�˵�" + message + "�ѳɹ�ȡ�����ġ�", openid,
					this.wxMsgId, "SUBSCRIBE", "SUCCESS");
		} else {
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "ȡ������ʧ�ܣ���������˵��Ų�����Ч���˵���", openid, this.wxMsgId,
					"SUBSCRIBE", "SUBFAIL");
		}
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		boolean success = service.process(responseXml).contains("���ͳɹ�");
	}

}
