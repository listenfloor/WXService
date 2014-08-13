package com.efreight.weixin.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXUserinfo;

public class WELCOMEProcess extends WXProcess {

	/**
	 * �û�Ψһ��ʾ��
	 */
	private String wxfakeid;
	/**
	 * ���췽����
	 * @param doc
	 */
	public WELCOMEProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}
	
	@Override
	public String snycProcess() {
		WXUserinfo user = WXInfoDownloader.userWithOpenId.get(openId);
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		Map<String,String> wel = new HashMap<String, String>();
		wel.put("title","Hi~ "+user.getNickname()+" ��ӭʹ��ָ����ˣ����������ҳ");
		wel.put("content", "��ӭʹ��ָ����ˣ����������ҳ");
		wel.put("picUrl","http://192.168.0.232/helpdoc/image/plan_mini.jpg");
		wel.put("url", "http://192.168.0.232/index.html");
		Map<String,String> eplant = new HashMap<String, String>();
		eplant.put("title","�˵���ѯ�������˵��Ų�ѯ�켣");
		eplant.put("content", "�˵���ѯ�������˵��Ų�ѯ�켣");
		eplant.put("picUrl","http://192.168.0.232/helpdoc/image/plan_mini_02.jpg");
		eplant.put("url", "http://192.168.0.232/helpdoc/index.html");
		Map<String,String> orderSubcribe = new HashMap<String, String>();
		orderSubcribe.put("title","�����˵�������sub���˵��Ŷ����˵������ǽ���һʱ��Ϊ�������˵��켣���");
		orderSubcribe.put("content", "�����˵�������sub���˵��Ŷ����˵������ǽ���һʱ��Ϊ�������˵��켣���");
		orderSubcribe.put("picUrl","http://sssssss.ssss.com/image/abc.jpg");
		orderSubcribe.put("url", "http://www.baidu.com");
		Map<String,String> tact = new HashMap<String, String>();
		tact.put("title","�˼۲�ѯ������tact���ۿ�/���������룬Ϊ����ѯ�ӱ����ϣ��㣬�ɶ�ʼ����Ŀ�ĵ��˼�");
		tact.put("content", "�˼۲�ѯ������tact���ۿ�/���������룬Ϊ����ѯ�ӱ����ϣ��㣬�ɶ�ʼ����Ŀ�ĵ��˼�");
		tact.put("picUrl","http://sssssss.ssss.com/image/abc.jpg");
		tact.put("url", "http://www.baidu.com");
		messageList.add(wel);
		messageList.add(eplant);
		messageList.add(orderSubcribe);
		messageList.add(tact);
		//�ж϶�ʱ��
		return WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"WELCOME","NORMAL");
	}

	@Override
	public void asnycProcess() {
		// TODO Auto-generated method stub

	}

}
