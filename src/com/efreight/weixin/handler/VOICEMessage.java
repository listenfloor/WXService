package com.efreight.weixin.handler;

import org.dom4j.Document;

import com.efreight.weixin.WXMessageLogHelper;

/**
 * �����û����͹���������Ϣ�����󣬼̳�WXMessageHandler��
 * @author xianan
 *
 */
public class VOICEMessage extends WXMessageHandler{

	/**
	 * Ψһ���췽��
	 * @param doc ΢�����������xmlת����dom4j��Document���͡�
	 * @param url 
	 */
	public VOICEMessage(Document doc,String url){
		this.doc = doc;
		this.url=url;
	}
	
	
	/**
	 * Ŀǰδ���ҵ���Ƶ�ļ���΢�ŷ������϶�Ӧ��ϵ�������޷����������ء�
	 */
	public String process(){
		//������־��voice Ϊ��������
		new Thread(new WXMessageLogHelper(this.doc, false, "true","VOICE")).start();//������־
		return null;
	}
	
}
