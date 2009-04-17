package edu.spam.framework.filters;

import javax.mail.internet.MimeMessage;

import edu.spam.framework.Filter;

public class TestFilter implements Filter {

	@Override
	public float test(MimeMessage msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean train(MimeMessage msg, boolean spam) {
		// TODO Auto-generated method stub
		return false;
	}
}
