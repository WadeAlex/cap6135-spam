package edu.spam.framework.filters;

import javax.mail.internet.MimeMessage;

import edu.spam.framework.Filter;

public class TestFilter implements Filter {

	@Override
	public boolean test(MimeMessage msg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean train(MimeMessage msg) {
		// TODO Auto-generated method stub
		return false;
	}
}
