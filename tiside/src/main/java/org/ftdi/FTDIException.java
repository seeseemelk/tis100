package org.ftdi;

public class FTDIException extends Exception
{
	private static final long serialVersionUID = 1L;

	public FTDIException()
	{
		super();
	}

	public FTDIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FTDIException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public FTDIException(String message)
	{
		super(message);
	}

	public FTDIException(int status, String message)
	{
		this(String.format("Return value: %d, reason: %s", status, message));
	}

	public FTDIException(Throwable cause)
	{
		super(cause);
	}

}
