package com.lg.mobility.data;

public class UserInfo {
	
	public UserInfo(){}
	private String userID;
	private boolean permissionForIR;
	private boolean permissionForTJD;
	
	public String getUserID(){return userID;}
	public void setUserID(String _userID){userID = _userID;}
	
	public boolean hasIRPermission(){return permissionForIR;}
	public void setIRPermission(boolean perm){permissionForIR = perm;}
	
	public boolean hasTJDPermission(){return permissionForTJD;}
	public void setTJDPermission(boolean perm){permissionForTJD = perm;}
	
}
