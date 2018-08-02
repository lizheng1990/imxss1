package com.imxss.web.domain;

import org.coody.framework.context.base.BaseModel;

@SuppressWarnings("serial")
public class EmailSendCensus extends BaseModel{

	private Integer userId;
	
	private Integer sendNum;
	
	private String day;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getSendNum() {
		return sendNum;
	}

	public void setSendNum(Integer sendNum) {
		this.sendNum = sendNum;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}
	
	
}
