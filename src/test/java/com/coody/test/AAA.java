package com.coody.test;

import org.coody.framework.util.FileUtils;
import org.coody.framework.util.HttpUtil;

public class AAA {

	public static void main(String[] args) {
		String context=FileUtils.readFile("d://coll_no2.txt");
		String []domains=context.split("\r\n");
		String old=FileUtils.readFile("d://coll_no1.txt");
		for(String domain:domains){
			if(old.contains(domain)){
				continue;
			}
			System.out.println(domain);
		}
	}
}
