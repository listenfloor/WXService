package com.efreight.SQL;

public class WXUploadFile {

	/**
	 * ����������
	 */
	private int sys_code;
	
	/**
	 * �ļ���
	 */
	private String fileName;
	
	/**
	 * �ļ����� 2:ͼƬ 3:��Ƶ 4:��Ƶ
	 */
	private int fileType;
	
	/**
	 * ����ʱ��
	 */
	private String createDate;
	
	/**
	 * ����·��
	 */
	private String filePath;
	
	/**
	 * ΢���ļ�id
	 */
	private String wxFileId;
	
	/**
	 * ����·��
	 */
	private String fileUrl;

	public int getSys_code() {
		return sys_code;
	}

	public void setSys_code(int sys_code) {
		this.sys_code = sys_code;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getWxFileId() {
		return wxFileId;
	}

	public void setWxFileId(String wxFileId) {
		this.wxFileId = wxFileId;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	
	
}
