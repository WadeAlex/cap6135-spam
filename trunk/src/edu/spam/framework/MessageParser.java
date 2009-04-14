package edu.spam.framework;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MessageParser {

	static Session session = Session.getDefaultInstance(new Properties());
	public static MimeMessage parseMessage(String msg) {
		try {
			FileInputStream stream = new FileInputStream(msg);
			MimeMessage message = new MimeMessage(session, stream);
			stream.close();
			return message;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
