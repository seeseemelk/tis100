package be.seeseemelk.tiside.spi;

public interface SPI extends AutoCloseable
{
	/**
	 * Sends a value, returning the result.
	 *
	 * @param value The value to send.
	 *
	 * @return The returned value.
	 */
	int transfer(int value) throws SPIException, InterruptedException;
}
