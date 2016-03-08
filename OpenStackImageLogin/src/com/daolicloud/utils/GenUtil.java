package com.daolicloud.utils;

import java.util.UUID;

public class GenUtil {
	
	public static UUID getUUID() {
		return UUID.randomUUID();
	}
	
	public static UUID convert(String uid) {
		return UUID.fromString(uid);
	}
}
