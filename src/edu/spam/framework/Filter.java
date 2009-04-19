package edu.spam.framework;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface Filter {
	public boolean train(MimeMessage msg, boolean spam);
	public float test(MimeMessage msg) throws IOException, MessagingException;
	public void clear();
}
