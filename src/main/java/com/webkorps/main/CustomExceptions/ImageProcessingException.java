package com.webkorps.main.CustomExceptions;

import java.io.IOException;

public class ImageProcessingException extends Exception {
	
	public ImageProcessingException(String msg, IOException e) {
		super(msg);
	}
	
	public ImageProcessingException(String msg) {
		super(msg);
	}
	

}
