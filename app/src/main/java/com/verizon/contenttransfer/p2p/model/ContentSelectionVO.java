package com.verizon.contenttransfer.p2p.model;

public class ContentSelectionVO {
	
	private String contentType;
	private String UImedia;
	private boolean contentFlag;
	private int  contentSize=0;
	private String contentStorage;
	private String cloudContent;
	private boolean permissionFlag;

	public String getUImedia() {
		return UImedia;
	}

	public void setUImedia(String UImedia) {
		this.UImedia = UImedia;
	}
	public String getCloudContent() {
		return cloudContent;
	}

	public void setCloudContent(String cloudContent) {
		this.cloudContent = cloudContent;
	}
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public boolean getContentflag() {
		return contentFlag;
	}
	
	public void setContentflag(boolean contentflag) {
		this.contentFlag = contentflag;
	}

	public int getContentsize() {
		return contentSize;
	}

	public void setContentsize(int contentsize) {
		this.contentSize = contentsize;
	}

	public String getContentStorage() {
		return contentStorage;
	}

	public void setContentStorage(String contentStorage) {
		this.contentStorage = contentStorage;
	}

	public boolean isPermissionFlag() {
		return permissionFlag;
	}

	public void setPermissionFlag(boolean permissionFlag) {
		this.permissionFlag = permissionFlag;
	}

}
