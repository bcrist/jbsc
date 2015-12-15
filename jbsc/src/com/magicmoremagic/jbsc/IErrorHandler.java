package com.magicmoremagic.jbsc;

public interface IErrorHandler {

	void handleError(ErrorCategory category, ErrorType type, String message, Exception ex);
	
}
