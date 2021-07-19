package org.ftdi;

public interface BitModes
{
	public static final byte RESET = 0x00;
	public static final byte BITBANG = 0x01;
	public static final byte MPSSE = 0x02;
	public static final byte SYNCBB = 0x04;
	public static final byte MCU = 0x08;
	public static final byte OPTO = 0x10;
	public static final byte CBUS = 0x20;
	public static final byte SYNCFF = 0x40;
	public static final byte FT1284 = (byte) 0x80;
}
