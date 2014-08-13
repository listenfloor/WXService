package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.sql.CLOB;

import org.dom4j.Document;
import org.dom4j.Node;

import com.efreight.SQL.InsertMessageLog;
import com.efreight.SQL.WXUploadFile;
import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * ��Ϣ��־������
 * @author xianan
 *
 */
public class WXMessageLogHelper implements Runnable {

//	private static final String imageUrl = "/wximage/";
	/**
	 * ����ͼƬ�洢·��
	 */
	private static final String imageUrl = "/wximage";
	/**
	 * ������ͼƬ·��
	 */
	private static final String servicePath = "/WeixinService";
	/**
	 * xml��document
	 */
	private Document data;
	/**
	 * �Ƿ��Ƿ���
	 */
	private boolean isSend;
	/**
	 * �Ƿ��ͳɹ�
	 */
	private boolean success;
	/**
	 * ���ͽ��
	 */
	private String result;
	/**
	 * ��Ϣ����.ֻ�н�����Ϣ����Ϣ���ͣ�������Ϣ��û����Ϣ���͡�
	 */
	private String msgType;
	
	/**
	 * ���ش������͡�
	 */
	private String msgResult;
	
	/**
	 * ΢����Ϣid
	 */
	private String wxMsgId;
	
	public WXMessageLogHelper(Document doc, boolean send, String result,String messageType) {
		this.data = doc;
		this.isSend = send;
		this.result = result;
		this.msgType = messageType;
	}
	
	public WXMessageLogHelper(Document doc, boolean send, String result,String messageType,String msgResult,String wxMsgId) {
		this.data = doc;
		this.isSend = send;
		this.result = result;
		this.msgType = messageType;
		this.msgResult = msgResult;
		this.wxMsgId = wxMsgId;
	}

	@Override
	/**
	 * ��־��¼��Ϊ���֣�һ�ֽ��յģ�һ�ַ��͵ġ�
	 * ����<xml>�ڵ������֡�����У�˵���ǽ��ջ�ͬ�����صģ����û��˵���Ƿ��͵ġ�
	 * ������Ϣ�У����ݲ�ͬ����Ϣ���ͣ����в�ͬ����־��������ͼƬ��Ҫ���ӱ���ͼƬ�鿴���ӡ��ر����ӵ�ͼ�鿴���ӡ�
	 * ������Ϣ����¼���ͽ����
	 */
	public void run() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();

			String datetime = format.format(new Date());
			Node node = data.selectSingleNode("/xml");
			String sender = "";
			String receiver = "";
			String messagetype = "";
			String content = "";
			String wxMsgId=null;
			String msgResult = "NORMAL";
			try{
				msgResult = data.selectSingleNode("//ErrorMsgType").getText();
				System.out.println(msgResult);
			}catch(Exception e){
			}
			if (node != null) {//��xml�ģ���ʾΪ΢��������ͬ������log
//				<xml><ToUserName><![CDATA[gh_50738c3e80e3]]></ToUserName><FromUserName><![CDATA[omQjGji2ryvc9pFo2TRWT5RCoU9M]]></FromUserName><CreateTime>1378366997</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[CLICK]]></Event><EventKey><![CDATA[MENU_TRACE_CLICK]]></EventKey></xml>
				String openid = isSend ? data.selectSingleNode("//ToUserName").getText() : data.selectSingleNode(
						"//FromUserName").getText();
				//isSendΪ�棬��ϵͳ�����û����٣����û�����ϵͳ				//��Ϣ��־��Ϊ���֣� 1 ���յ�����Ϣ���ж��Ƿ��ǽ�����Ϣ�� 2 ������Ϣ
				//ϵͳ������Ϣlog ����졣
				String msgType = data.selectSingleNode("//MsgType").getText();
				String msgId = "";
				try{
					msgId = data.selectSingleNode("//MsgId").getText();
				}catch(Exception e){
				}
				if (isSend) {
					receiver = openid;
					sender = "gh_50738c3e80e3";
					//�������΢�ŷ��������������ϢID
					try{
						if(this.wxMsgId!=null&&!"".equals(this.wxMsgId)){
							wxMsgId = this.wxMsgId;
						}
						if(this.msgResult!=null&&!"".equals(this.msgResult)){
							msgResult = this.msgResult;
						}
						//�����ж��Ƿ���ͬ�����ص���Ϣ�ı�����Ϊ��ͼ�ģ���ͼ�ģ��ı���
					}catch(Exception e){
					}
				} else {
					sender = openid;
					receiver = "gh_50738c3e80e3";
					wxMsgId = msgId;
				}
				if(msgType!=null){
					if(msgType.equals("text")){//�ı���Ϣ
						messagetype = "text";
						content = data.selectSingleNode("//Content").getText();
					}else if(msgType.equals("image")){//ͼƬ
						messagetype = "image";
						String picUrl = data.selectSingleNode("//PicUrl").getText();
						content = "<a href=\""+picUrl+"\" target=\"_blank\" >������ͼƬλ��</a>,<a target=\"_blank\"  href=\""+imageUrl+"/"+datetime.substring(0,4)+"/"+datetime.substring(5,7)+"/"+datetime.substring(8,10)+"/"+openid+"_"+msgId+".jpg"+"\">����ͼƬ</a>";
					}else if(msgType.equals("voice")){//��Ƶ
						messagetype = "voice";
						content = "������" + data.selectSingleNode("//Recognition").getText();
					}else if(msgType.equals("location")){//λ��
						messagetype = "location";
						String x = data.selectSingleNode("//Location_X").getText();
						String y = data.selectSingleNode("//Location_Y").getText();
						content = "<a target=\"_blank\"  href=\""+servicePath+"/UserLocationServlet?x="+x+"&y="+y+"\" >����λ��,x:"+x+" , y:"+y+"</a>";
					}else if(msgType.equals("news")){//ͼ����Ϣ
						messagetype = "imageandtext";
						List<Node> nodeList = data.selectNodes("//item");
						if(nodeList!=null&&nodeList.size()>0){
							for(Node nod: nodeList){
								String mesg = nod.selectSingleNode("//Title").getText();
								content+=" "+mesg;
							}
						}else{
							content = "����Ϣ";
						}
					}else if(msgType.equals("event")) {
						messagetype = "menuclick";
						content = "�Զ���˵������" + data.selectSingleNode("//EventKey").getText();
					}
				}
			} else {
				sender = "gh_50738c3e80e3";
				receiver = data.selectSingleNode("//openid") != null ? data.selectSingleNode("//openid").getText() : "";
				this.isSend = true;
				try{
					msgType = data.selectSingleNode("//MsgType").getText();
					wxMsgId = data.selectSingleNode("//wxMsgId").getText();
				}catch (Exception e) {
					
				}
				if(receiver.equals("")) {
					String fakeid = data.selectSingleNode("//wxfakeid").getText();
					 WXUserinfo userinfo = (WXUserinfo) sqlMap.queryForObject("getwxuserinfobyfakeid", fakeid);
					 receiver = userinfo.getOpenid();
				}
				String action = data.selectSingleNode("//ServiceAction").getText();
				if (action.equals("SendWXImageAndTextMessageWithID")) {
					messagetype = "imageandtext";
					content = "ͼ����Ϣ����ϢID=" + data.selectSingleNode("//msgid").getText();
				} else if (action.equals("SendWXImageAndTextMessage") || action.equals("SendWXImageAndTextMessageWithCustomAPI")) {
					messagetype = "imageandtext";
					content = "ͼ����Ϣ����Ϣ����=" + data.selectSingleNode("//title").getText() + "   "
							+ data.selectSingleNode("//digest").getText();
				} else if(action.equals("SendTracePushMessageWithTemplateAPI")) {
					messagetype = "text";
					content = "ģ����Ϣ=�����ĵ��˵�"+data.selectSingleNode("//awb_code").getText()+"���µĹ켣��"+data.selectSingleNode("//stardard_data").getText()+"�ظ�\"��ϸ\"��ֱ�ӻظ��˵��Ų鿴�˵��켣��ϸ��";
				} else if(action.equals("SendWXMessage")){
					String message = "";
					String type = "";
					String fileid = "";
					String fileUrl = "";
					try{
						message = data.selectSingleNode("//message").getText();
						type=data.selectSingleNode("//type").getText();
						fileid = data.selectSingleNode("//fileid").getText();
					}catch (Exception e) {
						
					}
					messagetype = "REPLY";
					//��������һ����ѯ�ļ�·��
					if(fileid!=null&&!"".equals(fileid)){
						Map<String,String> map = new HashMap<String, String>();
						map.put("wxFileId", fileid);
						WXUploadFile file = null ;
						try{
							file = (WXUploadFile)sqlMap.queryForObject("loadwxuploadfile",map);
							if(file!=null&&file.getFileUrl()!=null&&!"".equals(file.getFileUrl())){
								fileUrl = file.getFileUrl();
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(type!=null){
						if("1".equals(type)){
							content = "�˹��ظ��ı���Ϣ����Ϣ="+message;
						}else if("2".equals(type)){
							content = "�˹��ظ�ͼƬ��Ϣ����Ϣid="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">ͼƬ</a>";
						}else if ("3".equals(type)){
							content = "�˹��ظ�������Ϣ����Ϣid="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">����</a>";
						}else if("4".equals(type)){
							content = "�˹��ظ���Ƶ��Ϣ����Ϣid="+fileid+" <a href=\""+fileUrl+"\" target=\"_blank\">��Ƶ</a>";
						}
						
					}
				} else {
					messagetype = "text";
					content = data.selectSingleNode("//message").getText();
				}
			}
			// Map<String, String> args = new HashMap<String, String>();
//			String sql = "insert into messagelog (messageid,datetime,sender,receiver,messagetype,content,success) values (seq_wxmessage.nextval,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?)";
			// �������Դ
			boolean success = result!=null&&(result.equals("true") || result.contains("\"msg\":\"ok\"")||result.contains("preview send success")) || result.contains("\"errmsg\":\"ok\"");
			System.out.println("msgType : "+msgType);
			Map<String,String> entity = new HashMap<String, String>();
			entity.put("datetime", datetime);
			entity.put("sender", sender);
			entity.put("receiver", receiver);
			entity.put("messagetype", messagetype);
			entity.put("content", content);
			entity.put("success", success?"true":"false");
			entity.put("xml", data.asXML());
			entity.put("wxMsgId", wxMsgId);
			entity.put("msgType", msgType);
			entity.put("issend", this.isSend==true?"1":"0");
			entity.put("msgresult", msgResult);
			entity.put("sendresult",result);
		    //������¼
			InsertMessageLog iml = new InsertMessageLog();
			try{
				iml.insertXML(entity);
			}catch(Exception e){
				success = false;
			}
//		    sqlMap.insert("insertmessagelog",entity);
//			PreparedStatement pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, datetime);
//			pstmt.setString(2, sender);
//			pstmt.setString(3, receiver);
//			pstmt.setString(4, messagetype);
//			pstmt.setString(5, content);
//			//pstmt.setCLOB(6, this.getCLOB(data.asXML(), conn));
//			pstmt.setString(6, String.valueOf(success));
//			pstmt.executeUpdate();
			Map<String, String> args = new HashMap<String, String>();
			args.put("datetime", datetime);
			args.put("sender", sender);
			args.put("receiver", receiver);
			args.put("messagetype", messagetype);
			args.put("content", content);
			args.put("success", result);
			args.put("xml", data.asXML());
			args.put("wxMsgId", wxMsgId);
			// sqlMap.insert("WXAPIService.insertmessagelog", args);
			
//			if(!this.isSend &&(messagetype.equals("location")||messagetype.equals("image")||messagetype.equals("voice"))) {
//				MailComponent component  = new MailComponent("smtp.ym.163.com", "report@efreight.cn", "pass@word1");
//				String mailContent = WXInfoDownloader.userWithOpenId.get(sender).getNickname() + "��" + datetime + "������һ��" + messagetype + "��Ϣ����ע��ظ���";
//				component.smtp(PropertiesUtils.readProductValue("", "messagereportreciever"), "���µ�" + messagetype +"��Ϣ", mailContent, false);
//			}
//			if (!success) {
//				String warningXml = "<Service>" + "<ServiceURL>MailService</ServiceURL>"
//						+ "<ServiceAction>send</ServiceAction>" + "<ServiceData>" + "<MailService>"
//						+ "<mailserver>smtp.ym.163.com</mailserver>" + "<sender>service4@efreight.me</sender>"
//						+ "<senderpassword>123456</senderpassword>"
//						+ "<receiverlist>ludan@efreight.cn;renyj@efreight.cn;yes@efreight.cn</receiverlist>"
//						+ "<subject>΢��ƽ̨��Ϣ����ʧ��Ԥ��</subject>" + "<content>��Ϣ����:" + args.toString() + "</content>"
//						+ " </MailService>" + "</ServiceData>" + "</Service>";
//				HttpHandler.postHttpRequest("http://app.efreight.me/HttpEngine", warningXml);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}

	}


	public static CLOB getCLOB(String innStr, Connection conn) throws Exception {
		CLOB tempClob = null;
		// If the temporary CLOB has not yet been created, create new
		tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);

		// Open the temporary CLOB in readwrite mode to enable writing
		tempClob.open(CLOB.MODE_READWRITE);
		// Get the output stream to write
		Writer tempClobWriter = tempClob.getCharacterOutputStream();
		// Write the data into the temporary CLOB
		tempClobWriter.write(innStr);

		// Flush and close the stream
		tempClobWriter.flush();
		tempClobWriter.close();

		// Close the temporary CLOB
		tempClob.close();
		return tempClob;
	}
}
