package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util;


public class RecurlyException extends Exception
{
	private String message;

	public RecurlyException(String msg)
	{
		this.message = msg;
	}
	
	public String getMessage()
	{
		if(this.message != null)
		{
			return this.message;
		}

		return "<no error message available>";
	}
}
