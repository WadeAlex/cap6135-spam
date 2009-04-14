package edu.spam.framework.filters;

import javax.mail.internet.MimeMessage;

public interface Filter {
	public boolean train(MimeMessage msg);
	public boolean test(MimeMessage msg);
}
