package com.efreight.model;

import java.util.Date;

public class UserFootprint {

	private String id;
	
	/**
	 * ��¼url
	 */
	private String url;
	
	/**
	 * ����ͨ��url���ж��û�ʹ�õ������ַ��� tact/wabtrace/help/index/
	 * 
	 */
	private String service;
	
	/**
	 * ����ʱ��
	 */
	private Date createTime;
	
	/**
	 * �Ƿ���ת��
	 */
	private String isTransmit;
	
	/**
	 * �û�id�����п�����fakeid��native��id������id�ȡ�
	 */
	private String userid; 
	
	/**
	 * ��Դ���ֶ�Ϊ��weixin/ios/android����
	 */
	private String resource;
}
