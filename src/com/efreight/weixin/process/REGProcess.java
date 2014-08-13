package com.efreight.weixin.process;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.efreight.commons.PropertiesUtils;
import com.efreight.weixin.WXMessageLogHelper;

public class REGProcess extends WXProcess {
		/**
		 * ���췽��
		 * 
		 * @param doc
		 */
		public REGProcess(Document doc) {
			System.out.println("��ʼ��REGProcess");
			this.doc = doc;
			this.openId = doc.selectSingleNode("//FromUserName").getText();
			this.message = doc.selectSingleNode("//Content").getText();
			this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
		}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	public String snycProcess() {
		new Thread(new WXMessageLogHelper(doc, false, "true", "REGISTRATION")).start();// ������־
		String response = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), "�����˵�������ƽ̨�ƽ��-���磬����2013��7��30�����廪��ѧ�����þ��У���<a href=\"http://www.esinotrans.com/campaign/?entry=wxmsg\">�������</a>����");
		// TODO Auto-generated method stub
		
		try {
			new Thread(new WXMessageLogHelper(DocumentHelper.parseText(response), true, "true", "REGISTRATION","NORMAL",this.wxMsgId)).start();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// ������־
		return response;
	}

}
