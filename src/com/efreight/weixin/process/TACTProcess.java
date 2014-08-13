package com.efreight.weixin.process;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.MapDistance;
import com.efreight.commons.PropertiesUtils;
import com.efreight.commons.WeixinI18nUtil;
import com.efreight.weixin.WXAPIService;
import com.efreight.weixin.WXAPIServiceProcess;
import com.efreight.weixin.WXInfoDownloader;
import com.efreight.weixin.WXMessageLogHelper;
import com.efreight.weixin.WXUserinfo;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * �����û�����tact�˼۲�ѯ��Ϣ���̳�WXProcess
 * @author xianan
 *
 */
public class TACTProcess extends WXProcess {
	
	private static Map<String,String> cityMap = new HashMap<String, String>();
	static{
		cityMap.put("BJS", "39.55`116.24");
		cityMap.put("SHA", "31.14`121.29");
		cityMap.put("CAN", "23.08`113.14");
		cityMap.put("CTU", "30.4`104.04");
	}
	
	/**
	 * ���췽��
	 * @param doc
	 */
	public TACTProcess(Document doc){
		this.doc = doc;
		this.openId = doc.selectSingleNode("//FromUserName").getText();
		this.message = doc.selectSingleNode("//Content").getText();
		this.wxMsgId = doc.selectSingleNode("//MsgId").getText();
	}

	
	/**
	 * ͬ��������
	 */
	public String snycProcess() {
		//���Ȳ�ѯ�ۿ���Ϣ�����û�У�ֱ�ӷ��ش�����ʾ��
		new Thread(new WXMessageLogHelper(doc, false, "true", "TACT")).start();// ������־
		String responseXml = "";
		String wxfakeid = null;
		try {
			wxfakeid = WXProcessHandler.GetWXFakeidWithOpenid(openId, message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(wxfakeid==null||wxfakeid.equals("")){
			System.out.println("fakeid û�в鵽");
			return null;
		}
		this.clearMessage = message.toUpperCase().replaceAll("TACT", "").trim();
		
		if(clearMessage.length()!=3){
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, WeixinI18nUtil.getMessageWithOpenid(openId, "tact_3code_error", null), openId, this.wxMsgId, "TACT","FORMATERROR");
//			responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "tact_3code_error", null));
			return responseXml;
		}
		
		List<Map<String,String>> messageList = new ArrayList<Map<String,String>>();
		WXUserinfo user = WXInfoDownloader.userWithFakeId.get(wxfakeid);
		String x= "39.55";
		String y ="116.24";
		String cityname = "";
		if(user.getProvince()!=null){
			if(user.getProvince().equals("����")||user.getProvince().equals("�Ϻ�")||user.getProvince().equals("����")||user.getProvince().equals("���")||user.getProvince().equals("����")||user.getProvince().equals("���")){
				cityname=user.getProvince();
			}else{
				cityname = user.getCity();
			}
		}
		if(cityname!=null&&!"".equals(cityname)){
			SqlMapClient sqlmap = iBatisSQLUtil.getSqlMapInstance();
			try {
				List<Map<String,String>> cityMapList = (List<Map<String, String>>) sqlmap.queryForList("findcitycoordinatesbycityname", cityname);
				if(cityMapList!=null&&cityMapList.size()>0){
					Map<String,String> cityCoordinateMap = cityMapList.get(0);
					x = cityCoordinateMap.get("X");
					y = cityCoordinateMap.get("Y");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		List<String> nearstList = new ArrayList();
		Set<String> keySet = cityMap.keySet();
		Iterator it = cityMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,String> ent = (Entry<String, String>) it.next();
			String key = ent.getKey();
			String[] coordinates = ent.getValue().split("`");
//			File tactFile = new File("/Users/xianan/meftcn/tact/"+key+"-"+clearMessage+".html");
			//�ж��Ƿ����tact�۸�html
			System.out.println(PropertiesUtils.readProductValue("", "tactfileurl")+"tact/"+key+"-"+clearMessage+".html");
			if(WXProcessHandler.existsUrl(PropertiesUtils.readProductValue("", "tactfileurl")+"tact/"+key+"-"+clearMessage+".html")){
				if(nearstList!=null&&nearstList.size()>0){
					String[] city = nearstList.get(0).split("`");
					//city[0]���� city[1]Ϊ����
					if(Double.parseDouble(city[1])>MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))){
						nearstList.add(0, key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
					}else{
						nearstList.add(key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
					}
				}else{
					nearstList.add(key+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
				}
			}
		}
		System.out.println("the tact list : "+nearstList.size());
		if(nearstList!=null&&nearstList.size()>0){
			for(int i=0;i<nearstList.size();i++){
				String[] city = nearstList.get(i).split("`");
				Map<String,String> tactMap = new HashMap<String,String>();
//				tactMap.put("title",city[0]+"��"+clearMessage+" tact�˼���ϸ��Ϣ");
//				tactMap.put("content", city[0]+"��"+clearMessage+" tact�˼���ϸ��Ϣ");
				tactMap.put("title", WeixinI18nUtil.getMessageWithOpenid(openId, "tact_main", new Object[]{city[0],clearMessage}));
				tactMap.put("content", WeixinI18nUtil.getMessageWithOpenid(openId, "tact_main", new Object[]{city[0],clearMessage}));
				tactMap.put("picUrl",PropertiesUtils.readProductValue("", "tactfileurl")+"tactimages/"+city[0]+".jpg");
				tactMap.put("url", PropertiesUtils.readProductValue("", "tactfileurl")+"tact/"+city[0]+"-"+clearMessage+".html");
				messageList.add(tactMap);
			}
		}
		if(messageList==null||messageList.size()<=0){
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid,  WeixinI18nUtil.getMessageWithOpenid(openId, "tact_not_exist", new Object[]{clearMessage}), openId, this.wxMsgId, "TACT","NOTFOUND");
//			responseXml = WXProcessHandler.getWXTextResponseXML(this.openId, PropertiesUtils.readProductValue("", "fromuser"), WeixinI18nUtil.getMessageWithOpenid(openId, "tact_not_exist", new Object[]{clearMessage}));
			
			return responseXml;
		}
		//����xml
		responseXml =  WXProcessHandler.getWXTextAndImageCustomApiXML(this.openId, messageList,this.wxMsgId,"TACT","NORMAL");
		return responseXml;
	}
	/**
	 * �첽��������
	 * Ŀǰֻ�ܴ�����ʼ������ȫ�����ص��˼ۡ�����tact������ո��Ŀ�ĵ������롣
	 */
	public void asnycProcess() {
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
		new Thread(new WXMessageLogHelper(doc, false, "true", "TACT")).start();// ������־
		String clearMessage = message.trim().toUpperCase().replace("TACT", "").trim();
		if (clearMessage.length() == 3) {
			try {
				this.SendTactMessage(clearMessage, wxfakeid, openId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {//�ı���ʽ�д����Ͱ�����Ϣ
			this.errorMsgType="FORMATERROR";
			this.msgType="TACT";
			this.errorProcess();
			
		}
	}
	
	/**
	 * ��ȡtact��������
	 * @param clearMessage
	 * @return
	 */
	private Document getTactDoc(String clearMessage){
//		String xml = "<Service><ServiceURL>QueryTACT</ServiceURL><ServiceAction>queryAirport</ServiceAction><ServiceData><Departure>PEK</Departure><Destination>"
//				+ clearMessage + "</Destination><Weight/><Carrier/></ServiceData></Service>";
		String xml = "<Service><ServiceURL>QueryTACT</ServiceURL><ServiceAction>queryTactByDestination_ap</ServiceAction><ServiceData><destination>"
				+ clearMessage + "</destination></ServiceData><PageSize>100</PageSize><CurrentPage>1</CurrentPage></Service>";
		String responseStr = HttpHandler.postHttpRequest("http://app.efreight.me/HttpEngine", xml);
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(responseStr);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * ����tact�˼�
	 * 1 ����Ŀ�ĵ�����������tact�˼۲�ѯ��xml������ѯ�����
	 * 2 �������ؽ��������������أ���������ؽ����ƴ��xml���͸��û���
	 * @param clearMessage Ŀ�ĵ�������
	 * @param wxfakeid ΢���û�id
	 * @param openid ΢���û���Ӧƽ̨id
	 * @throws Exception
	 */
	private void SendTactMessage(String clearMessage, String wxfakeid, String openid) throws Exception {
		// TODO Auto-generated method stub
		Document doc = this.getTactDoc(clearMessage);
		if(doc==null){
			System.out.println("tact server is error");
		}
		List<Node> tactNodeList = doc.selectNodes("//QueryTACT");
		String responseXml = "";
		String nearest = "";
		StringBuffer sb = new StringBuffer();
		if(doc.asXML().contains("������")) {
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "��������������벻��ȷ��������������롣", openid,this.wxMsgId,"TACT","ERRORCODE");
		}else if (tactNodeList.size() == 0) {
			responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "������ѯ���˼�û�м�¼��", openid,this.wxMsgId,"TACT","NORMAL");
		} else {
			String digest = "";
			String html = "";
			WXUserinfo user = WXInfoDownloader.userWithFakeId.get(wxfakeid);
			String x= "39.55";
			String y ="116.24";
			String cityname = "";
			if(user.getProvince()!=null){
				if(user.getProvince().equals("����")||user.getProvince().equals("�Ϻ�")||user.getProvince().equals("����")||user.getProvince().equals("���")||user.getProvince().equals("����")||user.getProvince().equals("���")){
					cityname=user.getProvince();
				}else{
					cityname = user.getCity();
				}
			}
			if(cityname!=null&&!"".equals(cityname)){
				SqlMapClient sqlmap = iBatisSQLUtil.getSqlMapInstance();
				try {
					List<Map<String,String>> cityMapList = (List<Map<String, String>>) sqlmap.queryForList("findcitycoordinatesbycityname", cityname);
					if(cityMapList!=null&&cityMapList.size()>0){
						Map<String,String> cityCoordinateMap = cityMapList.get(0);
						x = cityCoordinateMap.get("X");
						y = cityCoordinateMap.get("Y");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			Map<String,List<String>> departureMap = new HashMap<String, List<String>>();
			for (Node node : tactNodeList) {
				String departure = node.selectSingleNode("Departure").getText();
				String level = node.selectSingleNode("Weight").getText();
				String price = node.selectSingleNode("Rate").getText();
				//�����ѯ�Ǹ������������
				String value = cityMap.get(departure);
				String[] coordinates = value.split("`");
				if(nearest!=null&&!"".equals(nearest)){
					String[] city = nearest.split("`");
					//city[0]���� city[1]Ϊ����
					if(Double.parseDouble(city[1])>MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))){
						nearest = departure+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
					}
				}else{
					nearest = departure+"`"+MapDistance.getDistance(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
				}
				if(departureMap.containsKey(departure)){
					List<String> l = departureMap.get(departure);
					int sign = 0;
					for(int i=0;i<l.size();i++){
						try{
							float target =Float.parseFloat(l.get(i).substring(l.get(i).indexOf("`")+1));
							float fprice = Float.parseFloat(price);
							if(fprice>target){
								l.add(i, level+"`"+price);
								sign = 1;
								break;
							}
						}catch(Exception e){
						}
					}
					if(sign==0){
						l.add( level+"`"+price);
					}
					//l.add(level+"`"+price);
				}else{
					List<String> l = new ArrayList<String>();
					l.add(level+"`"+price);
					departureMap.put(departure, l);
				}
			}
			nearest = nearest.substring(0, nearest.indexOf("`"));
			if(departureMap!=null&&departureMap.size()>0){
				//ȡ��һ��������htmlȻ���map��ȥ����������ѭ����
				if(departureMap.containsKey(nearest)){
					List<String> value = departureMap.get(nearest);
					sb.append("<messageitem><title>TACT�˼۲�ѯ "+nearest+" > " + clearMessage + "</title>");
					String fileid = PropertiesUtils.readWXFileValue("", "tact-"+nearest);
					if(fileid==null||"".equals(fileid)){
						fileid = PropertiesUtils.readWXFileValue("", "tact-DEFAULT");
					}
					sb.append("<fileid>"+fileid+"</fileid>");
					StringBuffer s = new StringBuffer();
					digest = "";
					if(value!=null&&value.size()>0){
						for(int i=0;i<value.size();i++){
							String[] levelAndPrice = value.get(i).split("`");
							digest += levelAndPrice[0]+" �� "+levelAndPrice[1]+" ";
							s.append("<p><span style=\"color:#0070c0;\"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;" + levelAndPrice[0]
									+ " &nbsp; &nbsp;</span>��" + levelAndPrice[1] + "</p>");
						}
					}
					html = (new sun.misc.BASE64Encoder()).encode(s.toString().getBytes("UTF-8"));
					sb.append("<digest>"+digest+"</digest>");
					sb.append("<content>" + html + "</content>");
					sb.append("</messageitem>");
					departureMap.remove(nearest);
				}
				Iterator it = departureMap.entrySet().iterator();
				while(it.hasNext()){
					Entry<String, List<String>> ent = (Entry<String, List<String>>) it.next();
					String  key = ent.getKey();
					List<String> value = ent.getValue();
					sb.append("<messageitem><title>TACT�˼۲�ѯ "+key+" > " + clearMessage + "</title>");
					String fileid = PropertiesUtils.readWXFileValue("", "tact-"+key);
					if(fileid==null||"".equals(fileid)){
						fileid = PropertiesUtils.readWXFileValue("", "tact-DEFAULT");
					}
					sb.append("<fileid>"+fileid+"</fileid>");
					StringBuffer s = new StringBuffer();
					digest = "";
					if(value!=null&&value.size()>0){
						for(int i=0;i<value.size();i++){
							String[] levelAndPrice = value.get(i).split("`");
							digest += levelAndPrice[0]+" �� "+levelAndPrice[1]+" ";
							s.append("<p><span style=\"color:#0070c0;\"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;" + levelAndPrice[0]
									+ " &nbsp; &nbsp;</span>��" + levelAndPrice[1] + "</p>");
						}
					}
					html = (new sun.misc.BASE64Encoder()).encode(s.toString().getBytes("UTF-8"));
					sb.append("<digest>"+digest+"</digest>");
					sb.append("<content>" + html + "</content>");
					sb.append("</messageitem>");
				}
			}else{
				responseXml = WXProcessHandler.GetTextMessageDoc(wxfakeid, "������ѯ���˼�û�м�¼��", openid,this.wxMsgId,"TACT","NORMAL");
			}
//			html = (new sun.misc.BASE64Encoder()).encode(html.getBytes("UTF-8"));
//			responseXml = "<eFreightService>" + "<ServiceURL>WXAPIService</ServiceURL>"
//					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>" + "<ServiceData>" + "<wxfakeid>"
//					+ wxfakeid + "</wxfakeid><messageid>10000125</messageid>" + "<messageitem>"
//					+ "<title>TACT�˼۲�ѯ PEK > " + clearMessage + "</title>" + "<digest>" + digest + "</digest>"
//					//����wxMsgId�ڵ�
//					+ "<content>" + html + "</content><fileid>"+PropertiesUtils.readProductValue("", "tactfileid")+"</fileid>" + "</messageitem><wxMsgId>"+wxMsgId+"</wxMsgId>" + "<MsgType>TACT</MsgType><ErrorMsgType>NORMAL</ErrorMsgType><openid>"
//					+ openid + "</openid>" + "</ServiceData>" + "</eFreightService>";
			responseXml = "<eFreightService>" + "<ServiceURL>WXAPIService</ServiceURL>"
					+ "<ServiceAction>SendWXImageAndTextMessage</ServiceAction>" + "<ServiceData>" + "<wxfakeid>"
					+ wxfakeid + "</wxfakeid><messageid>10000125</messageid>" +sb.toString()+"<wxMsgId>"+wxMsgId+"</wxMsgId>" + "<MsgType>TACT</MsgType><ErrorMsgType>NORMAL</ErrorMsgType><openid>"
					+ openid + "</openid>" + "</ServiceData>" + "</eFreightService>";
		}
		WXAPIServiceProcess service = new WXAPIServiceProcess();
		boolean success = service.process(responseXml).contains("���ͳɹ�");
//		new Thread(new WXMessageLogHelper(DocumentHelper.parseText(responseXml), true, success)).start();
	}
	
	/**
	 * ���ɷ���xml
	 * @param itemList
	 * @return
	 */
	private String createResponseXml(String openid,List<Map<String,String>> itemList){
		StringBuffer sb = new StringBuffer("<xml>");
		sb.append("<ToUserName><![CDATA["+openid+"]]></ToUserName>");
		sb.append("<FromUserName><![CDATA["+PropertiesUtils.readProductValue("", "fromuser")+"]]></FromUserName>");
		sb.append("<CreateTime>"+(new Date().getTime() / 1000 + 3)+"</CreateTime>");
		sb.append("<MsgType><![CDATA[news]]></MsgType>");
		if(itemList!=null&&itemList.size()>0){
			sb.append("<ArticleCount>"+itemList.size()+"</ArticleCount>");
			sb.append("<Articles>");
			for(int i=0;i<itemList.size();i++){
				Map<String,String> map = itemList.get(i);
				sb.append("<item>");
				sb.append("<Title><![CDATA["+map.get("Title")+"]]></Title> ");
				sb.append("<Description><![CDATA["+map.get("Description")+"]]></Description>");
				sb.append("<PicUrl><![CDATA["+map.get("PicUrl")+"]]></PicUrl>");
				sb.append("<Url><![CDATA["+map.get("Url")+"]]></Url>");
				sb.append("</item>");
			}
			sb.append("</Articles>");
		}else{
			sb.append("<ArticleCount>1</ArticleCount>");
			sb.append("<Articles>");
				sb.append("<item>");
				sb.append("<Title><![CDATA[��δ��ѯ�������]]></Title> ");
				sb.append("<Description><![CDATA[��δ��ѯ�������]]></Description>");
				sb.append("<PicUrl><![CDATA["+PropertiesUtils.readProductValue("", "notfoundpicurl")+"]]></PicUrl>");
				sb.append("<Url><![CDATA["+PropertiesUtils.readProductValue("", "notfoundurl")+"]]></Url>");
				sb.append("</item>");
			sb.append("</Articles>");
		}
		sb.append("<FuncFlag>1</FuncFlag>");
		sb.append("</xml>");
		return sb.toString();
	}
	
	private String createTactHtml(String title,String description,String img){
		StringBuffer sb = new StringBuffer("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>"+title+"</title>");
		sb.append("<meta http-equiv=Content-Type content=\"text/html;charset=utf-8\">");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0\">");
		sb.append("<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">");
		sb.append("<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\">");
		sb.append("<meta name=\"format-detection\" content=\"telephone=no\">");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"tact.css\"/>");
		sb.append("<style>#nickname{overflow: hidden;white-space: nowrap;text-overflow: ellipsis;max-width: 90%;  }  ol,ul{list-style-position:inside;  }</style>");
		sb.append("</head>");
		sb.append("<body id=\"activity-detail\">");
		sb.append("<div class=\"page-bizinfo\">");
		sb.append("<div class=\"header\">");
		sb.append("<h1 id=\"activity-name\">"+title+"</h1>");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("<div class=\"page-content\">");
		sb.append("<div class=\"media\" id=\"media\">");
		sb.append("<img src=\""+img+"\" onerror=\"this.parentNode.removeChild(this)\">");
		sb.append("</div>");
		sb.append("<div class=\"text\">");
		sb.append(description);
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	
}
