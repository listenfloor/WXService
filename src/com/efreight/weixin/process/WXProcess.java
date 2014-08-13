package com.efreight.weixin.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXMessageLogHelper;

/**
 * �����࣬�����ı���Ϣ��������Ϊͬ�����첽����
 * @author xianan
 *
 */
public abstract class WXProcess {

	/**
	 * dom4j��Document��
	 */
	protected Document doc;
	
	protected int timerMil=3500;
//	protected final String WXSystemCode="freight_in_hand";
	/**
	 * wx��������ʾ��
	 */
	protected final String WXSystemCode=PropertiesUtils.readProductValue(null, "username");
	/**
	 * �û���openid�����û���ƽ̨����Ψһid����Դ�û�����
	 */
	protected String openId;//openid
	/**
	 * ��Ϣ��
	 */
	protected String message;//��Ϣ��
	/**
	 * ΢��ϵͳΨһ��Ϣ��ʾ��
	 */
	protected String wxMsgId;//WX��ϢID
	/**
	 * ������Ϣ��
	 */
	protected String errorMsgType="";//������Ϣ����
	/**
	 * ��Ϣ���ͣ�Ϊ��־ͳ����
	 */
	protected String msgType="";//��Ϣ����
	/**
	 * ת������Ϣ��
	 */
	protected String clearMessage = "";
	public String command = "";
	
	//ͬ������
	/**
	 * ͬ������
	 * @return String
	 */
	public abstract String snycProcess();
	//�첽����
	/**
	 * �첽����
	 */
	public abstract void asnycProcess();
	public void setClearMessage(String clearMessage) {
		this.clearMessage = clearMessage;
	}
	/**
	 * ���������Ϣ
	 * ���û����Ͱ�����Ϣ
	 */
	public String errorProcess(){
		//��ʼ��ʱ��
			List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
			Map<String,String> guide = new HashMap<String, String>();
			guide.put("title",WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
			guide.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "help_title", null));
			guide.put("picUrl","http://m.eft.cn/helpdoc/images/plane.jpg");
			guide.put("url", WeixinI18nUtil.getMessageWithOpenid(openId, "help_url", null));
			messageList.add(guide);
			String responseXml = WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"UNKNOW","NORMAL");
			return responseXml;
	}
}
