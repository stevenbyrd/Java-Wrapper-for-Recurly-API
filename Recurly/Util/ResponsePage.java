package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util;


import org.apache.http.Header;


public class ResponsePage
{
	private String 		responseText;
	private Header[]	pageHeaders;
	
	public void setResponseText(String text)
	{
		this.responseText = text;
	}
	
	public String getResponseText()
	{
		return this.responseText;
	}


	public Header[] getPageHeaders()
	{
		return pageHeaders;
	}


	public void setPageHeaders(Header[] pageHeader)
	{
		this.pageHeaders = pageHeader;
	}
}
