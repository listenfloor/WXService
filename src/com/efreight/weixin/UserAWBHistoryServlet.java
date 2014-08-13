package com.efreight.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.json.JSONObject;

import com.efreight.SQL.iBatisSQLUtil;
import com.efreight.commons.HttpHandler;
import com.efreight.commons.PropertiesUtils;
import com.efreight.model.UserAWBHis;
import com.efreight.subscribe.UserSettings;
import com.efreight.weixin.handler.WXMessageHandler;
import com.efreight.weixin.process.WXProcessHandler;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * ������Ϊ�˽��տͻ�������˵��Ĳ�����¼
 */
public class UserAWBHistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(UserAWBHistoryServlet.class);
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserAWBHistoryServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *   ����get����
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * ���մӿͻ��˷��͹�������ʷ��¼
	 * ���и���json���е�ops�����֣����ops���ڣ����ǽ��в�������������ڣ�����Ϊ�ǲ�ѯ��
	 * �˵���¼����
	 * ����{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //У��λ
			"ops":"0",   //�Ƿ�ȡ�����ġ�0Ϊȡ����1Ϊ����
			"histype":"SUB", //SUB���ģ�QUERY��ѯ
			"pushdayofweek":"1,2,3,4,5",//day of week
			"pushtimefrom":"08:00",//����ʱ��
			"pushtimeto":"17:00",//����ʱ��
			"awbnum":"999-12345675", //�˵���
			"resource":"WEB" //��Դ��IOS / WEB
		}
		���أ�{
			"statue":"success", //����״̬��success �ɹ� error ʧ��
			"description":"�ɹ�", //����
		}
	 *
	 *	�˵���¼��ѯ
	 * ����{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //У��λ
			"awbnum":"999-12345675", //�˵��ţ�����Ϊ�գ�Ϊ�ձ�ʾ��ѯ���У���Ϊ�ձ�ʾ��ѯ������
		}
		���أ�{
			"userid":"woodenwalker",
			"key":"asfkdjbaskdhaskdjhas",  //У��λ
			"data":[
					{
						"awbnum":"999-12345675"
					},
					{
						"awbnum":"180-12345675"
					},
					{
						"awbnum":"760-12345675"
					}
				]
			}
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BufferedReader is = new BufferedReader(new InputStreamReader(
				request.getInputStream(), "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String s = "";
		while ((s = is.readLine()) != null) {
			sb.append(s);
		}
		String responseStr = "";
		System.out.println(sb.toString());
		log.info(sb.toString());//����json
		JSONObject json = new JSONObject(sb.toString());
		
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		String userid = null;
		String key = null;
		try{
			userid = json.getString("userid").toUpperCase();
			key = json.getString("key");
		}catch(Exception e ){
		}
		//�����û���Ϣ��֤
		if(userid==null||"".equals(userid)){
			responseStr = "{\"status\":\"error\",\"description\":\"�û�idΪ��\"}";
		}
		String ops = null;
		try{
			ops = json.getString("ops");
		}catch(Exception e){
			
		}
		if(ops!=null&&!"".equals(ops)){//��ʾ�Ǳ����¼�¼
//			ops = ops.equals("0")?"UNSUB":ops.equals("1")?"SUB":ops;
			responseStr = this.saveUserHis(json);
		}else{//��ѯ���
			try {
				String mawb = "";
				try {
					mawb = json.getString("awbnum");
				} catch (Exception e) {
					// TODO: handle exception
				}
				StringBuffer responsesb = new StringBuffer("{ \"userid\":\""+userid+"\", \"key\":\""+key+"\", \"data\":[");
				Map<String, String> para = new HashMap<String, String>();
				para.put("receiver", userid);
				if(!"".equals(mawb))
				para.put("mawbcode",mawb);
				List<Map<String,String>> hisList = sqlMap.queryForList("getuserawbhistory",para);
				if(hisList!=null&&hisList.size()>0){
					for(int i=0;i<hisList.size();i++){
						Map<String,String> uah = hisList.get(i);
						responsesb.append("{\"awbnum\":\""+uah.get("MAWBCODE")+"\"}");
						if(i!=(hisList.size()-1)){
							responsesb.append(",");
						}
					}
				}
				responsesb.append("]}");
				responseStr = responsesb.toString();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				responseStr = "{\"status\":\"error\",\"description\":\""+e.getMessage()+"\"}";
			}
		}
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	
	public static String saveUserHis(JSONObject json) {
		String receiver = json.getString("userid").toUpperCase();
		SqlMapClient sqlMap = iBatisSQLUtil.getSqlMapInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String awbtracenum ="";
		try{
			awbtracenum = json.getString("awbnum");
			awbtracenum = awbtracenum.trim();
			Pattern regex = Pattern.compile("^[0-9]{3}\\-[0-9]{8}$");
			Matcher matcher = regex.matcher(awbtracenum);
			if(!matcher.matches()){
				return "{\"status\":\"error\",\"description\":\"�˵��������밴������д����:999-12345675\"}";
			}
		}catch(Exception e){
			return "{\"status\":\"error\",\"description\":\"�˵�������\"}";
		}
		Map<String,String> map = new HashMap<String,String>();
//		ID
//		OPENID
//		USERNAME
//		MAWBCODE
//		CREATEDATE
//		HISTYPE
//		pushdayofweek
//		PUSHTIMEFROM
//		PUSHTIMETO
//		DATASOURCE
		String ops = json.getString("ops");
		String subtype = json.getString("resource").equals("WEB")?"WEIXIN":json.getString("resource");
		boolean isNewAPI = false;
		if(ops.equals("0")) {
			ops = "UNSUB";
		}else if(ops.equals("1")){
			ops = "SUB";
		}else {
			subtype = json.getString("subtype");
			isNewAPI = true;
		}
//		ops = ops.equals("0")?"UNSUB":ops.equals("1")?"SUB":ops;
		map.put("receiver", receiver );
		map.put("histype", ops);
		map.put("datasource", json.getString("resource"));
		map.put("mawbcode", awbtracenum);
		map.put("createdate", format.format(new Date()));
		map.put("subtype", subtype);
		if(isNewAPI && json.getString("pushdayofweek") != null && !"".equals(json.getString("pushdayofweek"))) {
			map.put("pushdayofweek", json.getString("pushdayofweek"));
			map.put("pushtimefrom", json.getString("pushtimefrom"));
			map.put("pushtimeto", json.getString("pushtimeto"));
		}else {
			UserSettings settings = new UserSettings();
			WXUserinfo userinfo = settings.getUserInfo(receiver);
			if(userinfo == null || (userinfo.getPushdayofweek() == null || "".equals(userinfo.getPushdayofweek()))) {
				map.put("pushdayofweek", UserSettings.DEFAULTPUSHDAYOFWEEK);
				map.put("pushtimefrom", UserSettings.DEFAULTPUSHTIMEFROM);
				map.put("pushtimeto", UserSettings.DEFAULTPUSHTIMETO);
			}else{
				map.put("pushdayofweek", userinfo.getPushdayofweek());
				map.put("pushtimefrom", userinfo.getPushtimefrom());
				map.put("pushtimeto", userinfo.getPushtimeto());
			}
		}
		String email = null;
		try {
			email = json.getString("email");
			if("".equals(email))
				email = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			List<Map<String,String>> result = sqlMap.queryForList("getuserawbhistory",map);
			if(result.size() > 0)
				sqlMap.update("updatepushinfo",map);
				
			else{
				sqlMap.insert("insertuserawbhistory", map);
			}
			String response = "";
			if(ops.equals("UNSUB"))
				response = WXProcessHandler.USubAwbTraceData(awbtracenum, receiver, false, null, null, null); 
			else if (ops.equals("SUB"))
				response = WXProcessHandler.GetAwbTraceData(awbtracenum,receiver,true,WXInfoDownloader.userWithFakeId.get(receiver).openid,null,null,email);
			System.out.println(response);
			return "{\"status\":\"success\",\"description\":\"�ɹ�\"}";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{\"status\":\"error\",\"description\":\""+e.getMessage()+"\"}";
		}
	}
	
}
