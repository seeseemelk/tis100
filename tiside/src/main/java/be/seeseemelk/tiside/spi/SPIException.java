package be.seeseemelk.tiside.spi;

import java.io.IOException;

public class SPIException extends IOException
{
	private static final long serialVersionUID = -6324706989796295787L;

	public SPIException()
	{
	}

	public SPIException(String message)
	{
		super(message);
	}

	public SPIException(Throwable cause)
	{
		super(cause);
	}

	public SPIException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
