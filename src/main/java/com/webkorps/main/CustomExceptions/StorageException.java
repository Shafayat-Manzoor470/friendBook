package com.webkorps.main.CustomExceptions;

import java.io.IOException;

public class StorageException  extends Exception {
	
//	public StorageException(String msg) {
//		super(msg);
//	}

	public StorageException(String msg, IOException e) {
		super(msg);
	}

}
