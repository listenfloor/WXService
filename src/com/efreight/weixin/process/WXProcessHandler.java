package com.efreight.weixin.process;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.UserAWBHistoryServlet;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * �������������
 * 
 * @author xianan
 * 
 */
public class WXProcessHandler {
	/**
	 * ��¼�û�֮ǰ��ѯ���˵���Ϣ��Ϊ�ظ�m/yָ��ʹ��
	 */
	public static Map<String, Map<String, Object>> commandChains = new HashMap<String, Map<String, Object>>();
	
	/**
	 * ��¼�û���ѯ�˵�ʱ�䡣�����ʱ���򲻷��Ͳ���ʾ�˹�����
	 */
	public static Map<String,Date> requestTimeChains = new HashMap<String,Date>();
	/**
	 * ֧�ֺ��չ�˾�б�
	 */
	public static Map<String, String> aircompanyList = null;
	/**
	 * 
	 */
//	public static String airCompanyTip = "ϵͳ�ݲ�֧�ָú��չ�˾���˵��켣��ѯ�붩�ġ�";
	
	private static Date aircompanyUpdateTime = null;
	
	/**
	 * ��ȡ΢���û���fakeid
	 * 
	 * @param openid
	 *            openid
	 * @param content
	 *            �ı�����
	 * @return String
	 * @throws Exception
	 */
	public static String GetWXFakeidWithOpenid(String openid, String content)
			throws Exception {
		Document doc = GetWXUserInfo(openid, content);
		Node node = doc.selectSingleNode("//fakeid");
		if (node != null)
			return node.getText();
		return null;
	}

	/**
	 * ��ȡ΢���û���Ϣ
	 * 
	 * @param openid
	 * @param content
	 * @return
	 * @throws Exception
	 * @throws DocumentException
	 */
	private static Document GetWXUserInfo(String openid, String content)
			throws Exception, DocumentException {
		String xml = "<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>GetWXUserinfoWithOpenid</ServiceAction><ServiceData><WXUserInfo><openid>"
				+ openid
				+ "</openid><content>"
				+ content
				+ "</content></WXUserInfo></ServiceData></eFreightService>";
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		String response = service.process(xml);
		Document doc = DocumentHelper.parseText(response);
		return doc;
	}

	/**
	 * ��ȡ΢���ı�����
	 * @param openId
	 * @param fromUser
	 * @param description
	 * @return
	 */
	public static String getWXTextResponseXML(String openId, String fromUser,  String description) {// �����ǵ�����Ϣ
		StringBuffer sb = new StringBuffer("<xml>" + "<ToUserName><![CDATA[" + openId + "]]></ToUserName>"
				+ "<FromUserName><![CDATA["+fromUser+"]]></FromUserName>" + "<CreateTime>" + new Date().getTime()
				/ 1000 + 3 + "</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>"
				+ "<Content><![CDATA["+description
				+ "]]></Content>" + "<FuncFlag>0</FuncFlag>" + "</xml>");
		return sb.toString();
	}


	/**
	 * �����ظ���Ϣ
	 * 
	 * @param openId
	 * @param fromUser
	 * @param messageList
	 * @param errorMsgType 
	 * @param msgType 
	 * @param wxMsgId 
	 * @return String
	 */
	public static String getWXTextAndImageCustomApiXML(String openId,
			 List<Map<String, String>> messageList, String wxMsgId, String msgType, String errorMsgType) {// �����ǵ�����Ϣ
		StringBuffer sb = new StringBuffer("<eFreightService><ServiceURL>WXAPIService</ServiceURL><ServiceAction>SendWXImageAndTextMessageWithCustomAPI</ServiceAction><ServiceData>");
				sb.append("<wxfakeid>"+WXInfoDownloader.userWithOpenId.get(openId).getFakeid()+"</wxfakeid>");
		if (messageList != null && messageList.size() > 0) {
			for (Map<String, String> msg : messageList) {
				sb.append("<messageitem>")
				.append("<title><![CDATA["+(msg.get("title") != null ? msg.get("title") : "")+"]]></title>")
				.append("<digest><![CDATA["+(msg.get("content") != null ? msg.get("content") : "")+"]]></digest>")
				.append("<url><![CDATA["+(msg.get("url") != null ? msg.get("url") : "")+"]]></url>")
				.append("<imgurl><![CDATA["+(msg.get("picUrl") != null ? msg.get("picUrl") : "")+"]]></imgurl>")
				.append("</messageitem>");
			}
		}

		sb.append("<wxMsgId>"+wxMsgId+"</wxMsgId>")
			.append("<MsgType>"+msgType+"</MsgType>")
			.append("<ErrorMsgType>"+errorMsgType+"</ErrorMsgType>")
			.append("<openid>"+openId+"</openid>")
			.append("</ServiceData>")
			.append("</eFreightService>");
		
		return sb.toString();
		
	}
	
	public static String getWXTextAndImageXML(String openId,List<Map<String,String>> messageList) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < messageList.size(); i++) {
			Map<String,String> item = messageList.get(i);
			sb.append("<item>");
			sb.append("<Title><![CDATA["+(item.get("title") != null ? item.get("title") : "")+"]]></Title>");
			sb.append("<Description><![CDATA["+(item.get("content") != null ? item.get("content") : "")+"]]></Description>");
			sb.append("<PicUrl><![CDATA["+(item.get("picUrl") != null ? item.get("picUrl") : "")+"]]></PicUrl>");
			sb.append("<Url><![CDATA["+(item.get("url") != null ? item.get("url") : "")+"]]></Url>");
			sb.append("</item>");
			
		}
		String response = "<xml>"
				+ "<ToUserName><![CDATA["+openId+"]]></ToUserName>"
				+ "<FromUserName><![CDATA["+ PropertiesUtils.readProductValue("", "fromuser")+"]]></FromUserName>"
				+ "<CreateTime>" + new Date().getTime()
				/ 1000 + 3 + "</CreateTime>"
				+ "<MsgType><![CDATA[news]]></MsgType>"
				+ "<ArticleCount>"+messageList.size()+"</ArticleCount><Articles>" + sb.toString()
				+"</Articles>"
				+ "</xml> ";
		
		return response;
	}
	
	public static String GetWXImageMessageCustomApiXML(String wxfakeid, String message,
			String openid, String wxMsgId, String msgType, String errorMsgType) {
		return "<eFreightService><ServiceURL>WXAPIService</ServiceURL>	<ServiceAction>SendWXImageMessageWithCustomAPI</ServiceAction><ServiceData><WXUserInfo><message><![CDATA["
				+ message
				+ "]]></message><wxfakeid>"
				+ wxfakeid
				+ "</wxfakeid><openid>"
				+ openid
				+ "</openid><wxMsgId>"
				+ wxMsgId
				+ "</wxMsgId><MsgType>"
				+ msgType
				+ "</MsgType><ErrorMsgType>"
				+ errorMsgType
				+ "</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
	}
	/**
	 * ��ȡ������Ϣxml,����errorMsgType
	 * 
	 * @param wxfakeid
	 * @param message
	 * @param openid
	 * @return String
	 */
	public static String GetTextMessageDoc(String wxfakeid, String message,
			String openid, String wxMsgId, String msgType, String errorMsgType) {
		return "<eFreightService><ServiceURL>WXAPIService</ServiceURL>	<ServiceAction>SendWXTextMessage</ServiceAction><ServiceData><WXUserInfo><message><![CDATA["
				+ message
				+ "]]></message><wxfakeid>"
				+ wxfakeid
				+ "</wxfakeid><openid>"
				+ openid
				+ "</openid><wxMsgId>"
				+ wxMsgId
				+ "</wxMsgId><MsgType>"
				+ msgType
				+ "</MsgType><ErrorMsgType>"
				+ errorMsgType
				+ "</ErrorMsgType></WXUserInfo></ServiceData></eFreightService>";
	}

	/**
	 * ��ȡ���չ�˾map
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> GetAirCompanyMap() {
		Map<String, String> result = new HashMap<String, String>();
		String xml = "<Service><ServiceURL>TraceTranslate</ServiceURL><ServiceAction>querySupportAirCompany</ServiceAction><ServiceData></ServiceData></Service>";
		try {
			String resultXml = HttpHandler.postHttpRequest(PropertiesUtils.readProductValue("", "awbtraceurl"),xml);
			Document resultDoc = DocumentHelper.parseText(resultXml);
			List<Node> airCompanys = (List<Node>) resultDoc
					.selectNodes("//aircompany");
			for (Node airCompany : airCompanys) {
				String code = airCompany.selectSingleNode("ac_code3c")
						.getText();
				String name = airCompany.selectSingleNode("cnname").getText();
				result.put(code, name);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, 5);
			aircompanyUpdateTime = calendar.getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * ����/ȡ������ �����˵�����
	 * 
	 * @param message
	 *            ��Ϣ��
	 * @param wxid
	 *            ΢��ID
	 * @param subscribe
	 *            ����/ȡ������
	 * @param openid
	 *            openId
	 * @param email
	 * @return String
	 * @throws Exception
	 */
	public static String GetAwbTraceData(String message, String wxid,
			boolean subscribe, String openid, String wxMsgId, String msgType,
			String email) throws Exception {
		WXProcessHandler.requestTimeChains.put(wxMsgId, new Date());
		String subscribertype = "WEIXIN";// subscribe ? "WEIXIN" : "NONE";
		String limit_num = subscribe ? "-1" : "1";
		String sync = subscribe ? "N" : "Y";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String requestXml = "<eFreightService>"
				+ "<ServiceURL>Subscribe</ServiceURL>"
				+ "<ServiceAction>TRANSACTION</ServiceAction>"
				+ "<ServiceData>" + "<Subscribe>"
				+ "<type>trace</type><target>" + message
				+ "</target><targettype>MAWB</targettype>" + "<sync>" + sync
				+ "</sync><subscriber>" + wxid + "</subscriber><wxMsgId>"
				+ wxMsgId + "</wxMsgId><subscribertype>" + subscribertype
				+ "</subscribertype>"
				+ "<standard_type>3</standard_type><limit_num>" + limit_num
				+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
				+ "</eFreightService>";
		String responseData = HttpHandler
				.postHttpRequest(
						PropertiesUtils.readProductValue("", "awbtraceurl"),
						requestXml);
		
		if (subscribe && email != null) {
			requestXml = "<eFreightService>"
					+ "<ServiceURL>Subscribe</ServiceURL>"
					+ "<ServiceAction>TRANSACTION</ServiceAction>"
					+ "<ServiceData>" + "<Subscribe>"
					+ "<type>trace</type><target>" + message
					+ "</target><targettype>MAWB</targettype>" + "<sync>"
					+ sync + "</sync><subscriber>" + email
					+ "</subscriber><wxMsgId>" + wxMsgId
					+ "</wxMsgId><subscribertype>MAIL</subscribertype>"
					+ "<standard_type>3</standard_type><limit_num>" + limit_num
					+ "</limit_num><offflag></offflag><systime>"+format.format(new Date())+"</systime></Subscribe>" + "</ServiceData>"
					+ "</eFreightService>";
			HttpHandler.postHttpRequest(
					PropertiesUtils.readProductValue("", "awbtraceurl"),
					requestXml);
		}
		if (!subscribe){
			//��ȡweixin���յ����͵ĺ�����
			String responseXml = GetAwbTraceHtmlData(responseData, wxid, openid, wxMsgId,msgType,false);
			long timer = getAWBMsgTime(wxMsgId);
			if(timer>=180000){//����3�����򲻷����󲢼�¼log
				// ���̷߳�log
				new Thread(new WXMessageLogHelper(
						org.dom4j.DocumentHelper.parseText(responseXml), true, "false", null)).start();
				// ɾ���ڴ�����Ϣʱ��
				removeAWBMsgTime(wxMsgId);
				return "";
			}
			return responseXml;
		}else{
			return "";
		}
	}
	

	public static String USubAwbTraceData(String message, String wxid,
			boolean subscribe, String openid, String wxMsgId, String msgType) throws Exception {
		String requestXml = "<eFreightService>"
				+ "<ServiceURL>Subscribe</ServiceURL>"
				+ "<ServiceAction>TRANSACTION</ServiceAction>"
				+ "<ServiceData><Subscribe>"
				+ "<type>trace</type><target>" + message
				+ "</target><targettype>MAWB</targettype>" + "<sync>N</sync><subscriber>" + wxid + "</subscriber><wxMsgId>"
				+ wxMsgId + "</wxMsgId><subscribertype>WEIXIN</subscribertype>"
				+ "<standard_type>2</standard_type><offflag>1</offflag><limit_num>-1</limit_num></Subscribe>" + "</ServiceData>"
				+ "</eFreightService>";
		String responseData = HttpHandler
				.postHttpRequest(
						PropertiesUtils.readProductValue("", "awbtraceurl"),
						requestXml);
		return responseData;
	}

	/**
	 * ���ͺ����˵���������
	 * 
	 * @param message
	 *            ��Ϣ��
	 * @param wxid
	 *            ΢��ID
	 * @param openid
	 *            openid
	 * @return String
	 * @throws Exception
	 */
	public static String GetAwbTraceData(String message, String wxid,
			String openid, String wxMsgId, String msgType) throws Exception {
		String result = null;
		try {
			
			String airCompanyName = aircompanyList.get(message.substring(0, 3));
			if (airCompanyName == null) {
				result = WXProcessHandler.GetTextMessageDoc(wxid,
						WeixinI18nUtil.getMessageWithOpenid(openid, "aircompany_notsupport", null), openid, wxMsgId, msgType, "UNSUPPORT");// ��֧�ֵĺ��չ�˾
			} else{
				result = WXProcessHandler.GetAwbTraceData(message, wxid, false,
						openid, wxMsgId, msgType, null);
			}
		} catch (Exception e) {
			result = "���޹켣��Ϣ";
			// result = WXProcessHandler.GetTextMessageDoc(wxid,
			// "���޹켣��Ϣ�����Ժ��ٲ�ѯ��ظ�Y���Ĵ��˵���лл��", openid,wxMsgId,msgType,"NOTFOUND");
		}
		return result;
	}

	/**
	 * ���ݲ�ѯ�˵���Ϣ�ķ��أ���֯���͸��û�html�������xml
	 * 
	 * @param responseData
	 * @param wxid
	 * @param openid
	 * @param wxMsgId
	 * @param msgType
	 * @return
	 * @throws Exception
	 */
	public static String GetAwbTraceHtmlData(String responseData, String wxid,
			String openid, String wxMsgId, String msgType, boolean isSub)
			throws Exception {
		String htmlData = "";
		String responseXml = "";
		try {
			Document doc = DocumentHelper.parseText(responseData);
			String awbcode = doc.selectSingleNode("//AWB_CODE").getText();
			List<Node> tracesNodeList = doc.selectNodes("//TraceTranslate");
			if (tracesNodeList == null || tracesNodeList.size() == 0) {
				throw new Exception("");
			}
			String lastestDate = "";
			String lastestStatus = "";
			String listhtml = "";
			String last_stardard_data = "";
			SimpleDateFormat longformat = new SimpleDateFormat("MM-dd HH:mm");
			SimpleDateFormat shortformat = new SimpleDateFormat("MM-dd");
			for (int i = (tracesNodeList.size() - 1); i >= 0; i--) {
				Node traceNode = tracesNodeList.get(i);
				String occur_time = "";
				if (traceNode.selectSingleNode("OCCUR_TIME") != null
						&& traceNode.selectSingleNode("OCCUR_TIME").getText() != null
						&& !"".equals(traceNode.selectSingleNode("OCCUR_TIME"))) {
					occur_time = traceNode.selectSingleNode("OCCUR_TIME")
							.getText();
				}
				String cargo_code = traceNode.selectSingleNode("CARGO_CODE")
						.getText();
				String stardard_data = traceNode.selectSingleNode("TRACE_DATA")
						.getText();

				if (i == tracesNodeList.size() - 1) {
					last_stardard_data = traceNode
							.selectSingleNode("TRACE_DATA").getText()
							.replace("&lt;BR/&gt;", "\n");
					lastestDate = occur_time;

					if (stardard_data != null && stardard_data.length() > 12) {
						lastestStatus = stardard_data.substring(0, 12) + "...";
					} else {
						lastestStatus = stardard_data;
					}
				}

				listhtml += "<p><span style=\"color:#7f7f7f;font-size:12px;\">"
						+ stardard_data
						+ "</span></p><p><hr style=\"color:#d8d8d8; width:98%;border-style: dashed;\"/></p>";
			}
			String url = WeixinI18nUtil.getMessageWithOpenid(openid,
					"waybillurl", null);
			StringBuffer sb = new StringBuffer();
			sb.append("<messageitem>");
			if (isSub) {
				sb.append("<title>�����ĵ� : " + awbcode + "�й켣����</title>");
				sb.append("<digest>���¹켣 : " + lastestStatus + " </digest>");
			} else {
				sb.append("<title>"
						+ WeixinI18nUtil.getMessageWithOpenid(openid,
								"trace_title", new Object[] { awbcode })
						+ "</title>");
				sb.append("<digest>"
						+ WeixinI18nUtil.getMessageWithOpenid(openid,
								"trace_sub_tip", null) + "</digest>");
			}
			sb.append("<fileid>"
					+ PropertiesUtils.readProductValue("", "awbtracefileid")
					+ "</fileid>");
			sb.append("<content>"
					+ (new sun.misc.BASE64Encoder()).encode(listhtml
							.getBytes("UTF-8")) + "</content>");
			sb.append("<url>"
					+ url
					+ "?mawbcode="
					+ awbcode
					+ "*"
					+ wxid
					+ "*"
					+ wxMsgId
					+ "</url><imgurl>"
					+ "http://mmsns.qpic.cn/mmsns/LUkW1zMiauPqeaxFpQickX3yCNiaX74Bp1Yg4SzYlcPYgib5AdsAG48q5w/0"
					+ "</imgurl>");
			sb.append("</messageitem>");
			
			responseXml = "<eFreightService>"
					+ "<ServiceURL>WXAPIService</ServiceURL>"
					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>"
					+ "<ServiceData>" + "<wxfakeid>"
					+ wxid
					+ "</wxfakeid><messageid>10000125</messageid>"
					+ sb.toString()
					+ "<wxMsgId>"
					+ wxMsgId
					+ "</wxMsgId>"
					+ "<MsgType>"
					+ msgType
					+ "</MsgType><ErrorMsgType>NORMAL</ErrorMsgType><openid>"
					+ openid
					+ "</openid>"
					
					+ "<stardard_data>"
					+ last_stardard_data
					+ "</stardard_data><awb_code>"
					+ awbcode
					+ "</awb_code></ServiceData>"
					+ "</eFreightService>";

			// responseXml = WXProcessHandler.GetTextMessageDoc(
			// wxid,
			// "<![CDATA[����鿴�˵�<a href=\""
			// + PropertiesUtils
			// .readProductValue("", "waybillurl")
			// + "?mawbcode=" + code + "p" + wxid + "p" + wxMsgId
			// + "\">" + code + "</a>���¹켣]]>", openid,
			// wxMsgId, "AWBTRACE", "NORMAL");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(responseData);
			throw new Exception("���ɹ켣��ϸ��Ϣ�ǳ���WXAPIServlet-->GetTraceHtmlData()"
					+ e.getMessage());
		}
		return responseXml;
	}

	/**
	 * ��ȡ���չ�˾�б�
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> getAircompanyList() {
		Date now = new Date();
		if(aircompanyList==null||aircompanyList.size()<=0 || aircompanyUpdateTime == null || aircompanyUpdateTime.before(now))
			aircompanyList = WXProcessHandler.GetAirCompanyMap();
		
		List<String> companyList = new ArrayList<String>();
//		if (aircompanyList != null && aircompanyList.size() > 0&&airCompanyTip.length()<40) {
//			airCompanyTip = "ϵͳ�ݲ�֧�ָú��չ�˾���˵��켣��ѯ�붩�ġ�";
//			��Ŀǰָ�����֧�����º��չ�˾:\n";
//			Set<String> keySet = aircompanyList.keySet();
//			Iterator<String> it = keySet.iterator();
//			while (it.hasNext()) {
//				String code = it.next();
//				String name = aircompanyList.get(code);
//				int count = 0;
//				for (int i = 0; i < companyList.size(); i++) {
//					if (code.compareTo(companyList.get(i)) <= 0) {
//						companyList.add(i, code);
//						count = 1;
//						break;
//					}
//				}
//				if (count == 0) {
//					companyList.add(code);
//				}
//			}
//			for (int i = 0; i < companyList.size(); i++) {
//				String name = aircompanyList.get(companyList.get(i));
//				airCompanyTip += companyList.get(i) + "-" + name + "\n";
//			}
//		}
		return aircompanyList;
	}

	/**
	 * ��ȡ������Ϣ
	 * 
	 * @param message
	 * @param openid
	 * @param wxid
	 * @return String
	 * @throws Exception
	 */
	public static String GetTraceData(String message, String openid,
			String wxid, String wxMsgId) throws Exception {
		String requestXml = "<eFreightService><ServiceURL>Order</ServiceURL><ServiceAction>DASHBOARDVIEW</ServiceAction><ServiceData><OrderNo>"
				+ message + "</OrderNo></ServiceData></eFreightService>";
		String responseData = HttpHandler.postHttpRequest(
				"http://air.esinotrans.com/eFreightHttpEngine", requestXml);
		String content = GetTraceHtmlData(responseData, wxid, openid, wxMsgId,
				"MARKETTRACE");
		return content;
	}

	/**
	 * ��ȡ����html
	 * 
	 * @param responseData
	 * @param wxid
	 * @param openid
	 * @return
	 * @throws Exception
	 */
	private static String GetTraceHtmlData(String responseData, String wxid,
			String openid, String wxMsgId, String msgType) throws Exception {
		String htmlData = "";
		String responseXml = "";
		try {
			Document doc = DocumentHelper.parseText(responseData);
			List<Node> tracesNodeList = doc.selectNodes("//OrderStatusGroup");
			if (tracesNodeList.size() == 0) {
				return GetTextMessageDoc(wxid, "������Ķ����Ų����ڣ���ȷ�϶�������ȷ��", openid,
						wxMsgId, msgType, "NOTFOUND");
			}
			String lastestDate = doc.selectSingleNode(
					"//OrderRecentlyStatusTime").getText();
			String lastestStatus = doc
					.selectSingleNode("//OrderRecentlyStatus").getText();
			String code = doc.selectSingleNode("//OrderNo").getText();
			String listhtml = "";
			SimpleDateFormat longformat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat shortformat = new SimpleDateFormat("MM-dd");
			htmlData = "<p style=\"padding:0px;min-height:1.5em;color:#3e3e3e;\">���¶����켣״̬</p><p style=\"padding:0px;min-height:1.5em;color:#3e3e3e;\"><span style=\"font-size:10px;color:#a5a5a5;\">"
					+ lastestDate
					+ "</span></p><p style=\"padding:0px;min-height:1.5em;\"><span style=\"color:#76923c;\">"
					+ lastestStatus + "</span></p><p><br /></p>";
			for (int i = 0; i < tracesNodeList.size(); i++) {
				Node traceNode = tracesNodeList.get(i);
				htmlData += "<p><strong><span style=\"font-size:14px;color:#e36c09;text-decoration:underline;\">"
						+ traceNode.selectSingleNode("StatusDate").getText()
						+ "</span></strong></p>";
				List<Node> traceDetailNodeList = traceNode
						.selectNodes("OrderStatus");
				for (int j = 0; j < traceDetailNodeList.size(); j++) {
					Node traceDetailNode = traceDetailNodeList.get(j);
					htmlData += "<p><strong><span style=\"color:#7f7f7f;font-size:12px;\"><span style=\"color:#262626;font-size:12px;\">"
							+ traceDetailNode.selectSingleNode("StatusTime")
									.getText()
							+ "</span></span></strong><span style=\"color:#7f7f7f;font-size:12px;\"><span style=\"color:#262626;font-size:12px;\"></span>"
							+ traceDetailNode.selectSingleNode("StatusDesc")
									.getText() + "</span></p>";
				}
				htmlData += "<p><hr style=\"color:#d8d8d8; width:98%;border-style: dashed;\"/></p>";
			}
			htmlData = (new sun.misc.BASE64Encoder()).encode(htmlData
					.getBytes("UTF-8"));
			responseXml = "<eFreightService>"
					+ "<ServiceURL>WXAPIService</ServiceURL>"
					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>"
					+ "<ServiceData>" + "<wxfakeid>"
					+ wxid
					+ "</wxfakeid><openid>"
					+ openid
					+ "</openid><wxMsgId>"
					+ wxMsgId
					+ "</wxMsgId><messageid>10000127</messageid><MsgType>"
					+ msgType
					+ "</MsgType>"
					+ "<ErrorMsgType>NORMAL</ErrorMsgType><messageitem>"
					+ "<title>�����˵��� "
					+ code
					+ "</title>"
					+ "<digest>���¶����켣״̬: "
					+ lastestDate
					+ " "
					+ lastestStatus
					+ "</digest>"
					+ "<content>"
					+ htmlData
					+ "</content>"
					+ "<fileid>"
					+ PropertiesUtils.readProductValue("", "markettracefileid")
					+ "</fileid>"
					+ "</messageitem>"
					+ "</ServiceData>"
					+ "</eFreightService>";
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("���ɹ켣��ϸ��Ϣ�ǳ���WXAPIServlet-->GetTraceHtmlData()"
					+ e.getMessage());
		}
		return responseXml;
	}
	
	/**
	 * ͨ��wx��Ϣid��ѯ������ʼ������
	 * @param wxMsgId
	 * @return ����Ϊ��1���ʾ�����⣬û�鵽
	 */
	public static long getAWBMsgTime(String wxMsgId){
		if(requestTimeChains.containsKey(wxMsgId)){//������ڣ����ڴ���ȡ����������������ڣ�ȥ���ݿ��в�ѯ
			Date date = requestTimeChains.get(wxMsgId);
			return System.currentTimeMillis()-date.getTime();
		}else{
			SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
			try {
				String time = "";
				List<String> timeList = sqlMap.queryForList("loadmessagelogbywxmsgid",wxMsgId);
				if(timeList!=null&&timeList.size()>0){
					time = timeList.get(0);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = sdf.parse(time);
					return System.currentTimeMillis()-date.getTime();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static void removeAWBMsgTime(String wxMsgId){
		if(requestTimeChains.containsKey(wxMsgId)){
			requestTimeChains.remove(wxMsgId);
		}
	}
	
	/**
	 * �ж�url�Ƿ����
	 * @param url
	 * @return
	 */
	public static boolean existsUrl(String url) {
        try {
	        	URL airportInfo = new URL(url);
	    		HttpURLConnection.setFollowRedirects(false);
            //�� URL �����õ�Զ�̶��������
            HttpURLConnection con = (HttpURLConnection) airportInfo.openConnection();
            /* ���� URL ����ķ����� GET POST HEAD OPTIONS PUT DELETE TRACE ���Ϸ���֮һ�ǺϷ��ģ�����ȡ����Э������ơ�*/
			con.setRequestMethod("HEAD");
			System.out.println("return code : "+con.getResponseCode() +"  url : "+url+"   ");
			return con.getResponseCode()==HttpURLConnection.HTTP_OK;
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //�� HTTP ��Ӧ��Ϣ��ȡ״̬��
        catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
        return false;
	}

	public static boolean GetSubStatus(String userid, String mawb) throws Exception {
		System.out.println("=========GetSubStatus");
		Map<String, String> para = new HashMap<String, String>();
		para.put("receiver", userid);
		if(!"".equals(mawb))
		para.put("mawbcode",mawb);
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		List<Map<String,String>> hisList = sqlMap.queryForList("getuserawbhistory",para);
		return hisList.size()>0;
	}
	public static void main (String[] args){
		String url="http://m.eft.cn/meftcn/tact/CAN-ROM.html";
		WXProcessHandler.existsUrl(url);
	}
}
