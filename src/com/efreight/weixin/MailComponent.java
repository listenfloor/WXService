package com.efreight.weixin;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


/** 
 * Created by code machine
 * @author TransformerPlugin
 */
public class MailComponent {
	private String smtpHost = "";
	private String sender = "";
	private String password = "";
	
	public MailComponent(String smtpHost,String sender,String password) {
		this.smtpHost = smtpHost;
		this.sender = sender;
		this.password = password;
	}

	/**
	 * ʹ��smtp�����ʼ� ������
	 * 
	 * @throws MessagingException
	 *             mail����ʧ��
	 * @throws UnsupportedEncodingException 
	 */
	public void smtp(String receiverlist,String subject,String content,boolean withzip) throws Exception {
		if (smtpHost == null)
			throw new MessagingException("smtpHost not found");
		if (sender == null)
			throw new MessagingException("user not found");
		if (password == null)
			throw new MessagingException("password not found");

//		if(smtpHost.indexOf("ym.163.com")<0)
//			user = sender.substring(0,sender.indexOf("@"));
		Properties properties = new Properties();
		properties.put("mail.smtp.host", smtpHost);// ����smtp����
		properties.put("mail.smtp.auth", "true");// ʹ��smtp�����֤
		Session session = Session.getInstance(properties,
				new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sender, password);
					}
				});
		session.setDebug(false);
		InternetAddress[] receivers = parse(receiverlist);

		// ����ʼ��Ự����
		MimeMessage mimeMsg = new MimeMessage(session);// ����MIME�ʼ�����
		if (sender != null)// ���÷����˵�ַ
		{
			mimeMsg.setFrom(new InternetAddress(sender));
		}
		if (receivers != null)// �����ռ��˵�ַ
		{
			mimeMsg.setRecipients(Message.RecipientType.TO, receivers);
		}
		if (subject != null)// �����ʼ�����
		{
			mimeMsg.setSubject(subject, "UTF-8");
		}
		MimeBodyPart part = new MimeBodyPart();// mail���ݲ���
		part.setText(content == null ? "" : content, "UTF-8");

		// �����ʼ���ʽΪhtml cqc
		part.setContent(content.toString(), "text/html;charset=UTF-8");
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(part);// �� Multipart ������mail���ݲ���
		if (withzip) {
			MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			File attach = new File("tempreport/" + subject + ".zip");
			FileDataSource fds = new FileDataSource(attach);
			mbp2.setDataHandler(new DataHandler(fds));
			// mbp2.setFileName(subject+".zip");
			mbp2.setFileName(MimeUtility.encodeWord(fds.getName(), "GB2312",
					null));
			multipart.addBodyPart(mbp2);
		}
		mimeMsg.setContent(multipart);// ���� Multipart ����Ϣ��
//		mimeMsg.setSentDate(new Date());// ���÷�������
		Transport.send(mimeMsg);// �����ʼ�
	}

	/** ������ַ�����ַ��� */
	private InternetAddress[] parse(String addressSet) throws AddressException {
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(addressSet, ";");
		while (tokens.hasMoreTokens()) {
			String tempadd = tokens.nextToken().trim();
			if(tempadd.indexOf("@")<0){
				tempadd+="@sinoair.com";
			}
			list.add(new InternetAddress(tempadd));
		}
		InternetAddress[] addressArray = new InternetAddress[list.size()];
		list.toArray(addressArray);
		return addressArray;
	}
	
	private InternetAddress[] parse(ArrayList maillist) throws AddressException {
		InternetAddress[] addressArray = new InternetAddress[maillist.size()];
		for(int i = 0;i<maillist.size();i++){
			InternetAddress address = new InternetAddress((String) maillist.get(i));
			addressArray[i] = address;
		}
		return addressArray;
	}

	public static void main(String[] args) {
		MailComponent c = new MailComponent("smtp.ym.163.com", "report@efreight.cn", "pass@word1");
		String content = "<html><head></head><body><font color='red'>���ԣ�</font></body></html>";
		try {
			c.smtp("ludan@efreight.cn;liuww1@sinoair.com", "�����ʼ�", content, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
