package com.efreight.weixin.handler;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;

/**
 * �����࣬���е���Ϣ������ĸ��ࡣ���а����������Ժ�һ��������
 * @author xianan
 *
 */
public abstract class WXMessageHandler {

	/**
	 * Document���ԣ�dom4j��Document���ԡ�����ʵ������ʱ������Ҫ��΢�ŷ�������������xmlת����dom4j��Document�ġ�
	 *
	 */
	protected Document doc ;
	protected String url;
	public HttpServletResponse response;
	
	/**
	 * �������󣬷���String���͵Ľ����
	 * @return String
	 */
	public abstract String process();
}
