package com.efreight.weixin.handler;

import org.dom4j.Document;

import com.efreight.weixin.WXMessageLogHelper;

/**
 * �����û����͹���λ����Ϣ�����󣬼̳�WXMessageHandler��
 * @author xianan
 *
 */
public class LOCATIONMessage extends WXMessageHandler {
	

	/**
	 * Ψһ���췽��
	 * @param doc ΢�����������xmlת����dom4j��Document���͡�
	 * @param url 
	 */
	public LOCATIONMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	
	/**
	 * ��¼�û��ύ��Ϣ
	 */
	public String process() {
		//������־��locationΪ�ر�����
		new Thread(new WXMessageLogHelper(doc, false, "true","LOCATION")).start();//������־
		return null;
	}

}
