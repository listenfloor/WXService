package com.efreight.weixin;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.efreight.weixin.process.WXProcessHandler;


/**
 * Servlet implementation class ResendMessageServlet
 */
public class ResendMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ResendMessageServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String responseStr = "ֻ֧��ͨ��POST������ʴ˽ӿڡ�";
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean success = true;
		System.out.println("���Ƴ�����ڣ�");
		String responseStr = "Ϊ��ֵ";
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if (action == null || "".equals(action))
			success = false;

		String type = request.getParameter("type");
		if (type == null || "".equals(type))
			success = false;

		String openid = request.getParameter("openid");
		if (openid == null || "".equals(openid))
			success = false;
		
		String fakeid = request.getParameter("fakeid");
		if (fakeid == null || "".equals(fakeid))
			success = false;
		
		String messageid = request.getParameter("messageid");
		if (messageid == null || "".equals(messageid))
			success = false;

		String message = request.getParameter("message");
		if (message == null || "".equals(message))
			success = false;
		
		System.out.println( action+" " +type+" " +openid+" " +fakeid+" " +messageid+" " +message);
		if (!success) {
			responseStr = "�������벻��ȷ����������ԡ�";
		} else {

			if (action.toUpperCase().equals("AWB")) {
				Pattern regex = Pattern.compile("\\D");
				Matcher matcher = regex.matcher(message);
				message = matcher.replaceAll("");
				message = message.trim();
				Pattern p = Pattern.compile("\\d{11}");
				Matcher m = p.matcher(message);
				if (m.matches()) {
					try {
						int order = Integer.parseInt(message.substring(3, 10));
						int lastNumber = Integer
								.parseInt(message.substring(10));
						if (order % 7 != lastNumber) {
							throw new Exception();
						}
					} catch (Exception e) {
						responseStr = "�˵���ʽ��������";
					}
					try {
						WXProcessHandler.getAircompanyList();
						String data = WXProcessHandler.GetAwbTraceData(message.substring(0, 3) + '-' + message.substring(3),fakeid,openid,messageid,"RESEND");
						if(type.toLowerCase().equals("customapi"))
							data = data.replace("SendWXImageAndTextMessage", "SendWXImageAndTextMessageWithCustomAPI");
						if(data!=null&&!"".equals(data)){
							WXAPIServiceProcess service = new WXAPIServiceProcess();
							boolean sendSuccess =service.process(data).contains("���ͳɹ�");
							if(sendSuccess) {
								responseStr = "���Ƴɹ���";
							}else
								responseStr = "����ʧ��";
						}
					} catch (Exception e) {
						responseStr = "���Ʒ�������쳣��"+e.getMessage();
						e.printStackTrace();
					}
				} else {
					responseStr = "�˵����������";
				}
			} else if (action.toUpperCase().equals("TACT")) {

			}
		}
		System.out.println("!!!!!!!!!!!"+ responseStr);
		response.getOutputStream().write(responseStr.getBytes("UTF-8"));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

}
