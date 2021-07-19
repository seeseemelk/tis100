package be.seeseemelk.tiside.spi;

import org.ftdi.BitModes;
import org.ftdi.FTDIContext;
import org.ftdi.FTDIException;

public class FTDISPI implements SPI, AutoCloseable
{
	public final int PIN_MOSI = 0;
	public final int PIN_MISO = 1;
	public final int PIN_SCK = 2;
	public final int PIN_SS = 3;

	private final int MOSI = (1 << PIN_MOSI);
	private final int MISO = (1 << PIN_MISO);
	private final int SCK = (1 << PIN_SCK);
	private final int SS = (1 << PIN_SS);

	private final FTDIContext ftdi;
	private int output = 0xFF;

	public FTDISPI(FTDIContext ftdi)
	{
		this.ftdi = ftdi;
	}

	public void open() throws SPIException
	{
		try
		{
			int pinMask = (1 << PIN_MOSI) | (1 << PIN_SCK) | (1 << PIN_SS);
			ftdi.setBitMode((byte) pinMask, BitModes.BITBANG);
			setSS();
			clearSCK();
		}
		catch (FTDIException e)
		{
			throw new SPIException(e);
		}
	}

	private int read() throws SPIException
	{
		try
		{
			return ftdi.readPins();
		}
		catch (FTDIException e)
		{
			throw new SPIException(e);
		}
	}

	private void setClear(int set, int clear) throws SPIException
	{
		try
		{
			output |= set;
			output &= ~clear;
			ftdi.write(output);
		}
		catch (FTDIException e)
		{
			throw new SPIException(e);
		}
	}

	private void set(int mask) throws SPIException
	{
		setClear(mask, 0);
	}

	private void clear(int mask) throws SPIException
	{
		setClear(0, mask);
	}

	private void setBit(int bit) throws SPIException
	{
		set(1 << bit);
	}

	private void clearBit(int bit) throws SPIException
	{
		clear(1 << bit);
	}

	public boolean getMISO() throws SPIException
	{
		return (read() & (1 << PIN_MISO)) != 0;
	}

	public void setMOSI() throws SPIException
	{
		setBit(PIN_MOSI);
	}

	public void clearMOSI() throws SPIException
	{
		clearBit(PIN_MOSI);
	}

	public void setSCK() throws SPIException
	{
		setBit(PIN_SCK);
	}

	public void clearSCK() throws SPIException
	{
		clearBit(PIN_SCK);
	}

	public void setSS() throws SPIException
	{
		setBit(PIN_SS);
	}

	public void clearSS() throws SPIException
	{
		clearBit(PIN_SS);
	}

	public int transfer(int value) throws SPIException, InterruptedException
	{
		int result = 0;
		try
		{
			clear(SCK | SS);
			for (int i = 0; i < 8; i++)
			{
				clearSCK();
				if ((value & 0x80) == 0)
					setClear(SCK, MOSI);
				else
					set(SCK | MOSI);
				value <<= 1;
				result = (result << 1) | (getMISO() ? 1 : 0);
			}
		}
		finally
		{
			setClear(SS, SCK);
		}
		return result;
	}

	@Override
	public void close() throws Exception
	{
		ftdi.close();
	}
}
