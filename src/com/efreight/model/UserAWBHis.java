package com.efreight.model;

import java.util.Date;

public class UserAWBHis {

	private String id;
	
	private String userid;
	
	private Date createTime;
	
	/**
	 * ��Դ
	 */
	private String source; 
	
	/**
	 * ��״̬Ϊ�Ƿ���ȡ������
	 */
	private String isdel;
	
	/**
	 * �˵���
	 */
	private String awbTraceNum;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getIsdel() {
		return isdel;
	}

	public void setIsdel(String isdel) {
		this.isdel = isdel;
	}

	public String getAwbTraceNum() {
		return awbTraceNum;
	}

	public void setAwbTraceNum(String awbTraceNum) {
		this.awbTraceNum = awbTraceNum;
	}
}
