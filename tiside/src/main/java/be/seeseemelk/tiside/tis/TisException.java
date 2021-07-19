package be.seeseemelk.tiside.tis;

public class TisException extends Exception
{
	private static final long serialVersionUID = -2073059278653322178L;

	public TisException()
	{
	}

	public TisException(String message)
	{
		super(message);
	}

	public TisException(Throwable cause)
	{
		super(cause);
	}

	public TisException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public TisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
