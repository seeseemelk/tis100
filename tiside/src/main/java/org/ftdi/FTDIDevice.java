package org.ftdi;

import org.ftdi.c.LibFTDI;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * A wrapper around a libftdi device.
 */
public class FTDIDevice
{
	private static final int STR_LENGTH = 32;
	private final FTDIContext context;
	private final Pointer ptr;

	public FTDIDevice(FTDIContext context, Pointer ptr)
	{
		this.context = context;
		this.ptr = ptr;
	}

	/**
	 * Gets the internal pointer to the libusb device.
	 *
	 * @return A pointer to the internal struct.
	 */
	public Pointer getPointer()
	{
		return ptr;
	}

	/**
	 * Gets the name of the manufacturer.
	 *
	 * @return The name of the manufacturer.
	 * @throws FTDIException
	 */
	public String getManufacturer() throws FTDIException
	{
		Memory memory = new Memory(STR_LENGTH);
		int result = LibFTDI.INSTANCE.ftdi_usb_get_strings2(
				context.getPointer(), ptr,
				memory, (int) memory.size(),
				Memory.NULL, 0,
				Memory.NULL, 0
		);
		context.enforceOk(result);
		return memory.getString(0);
	}

	/**
	 * Gets the description of the device.
	 *
	 * @return The description of the device.
	 * @throws FTDIException
	 */
	public String getDescription() throws FTDIException
	{
		Memory memory = new Memory(STR_LENGTH);
		int result = LibFTDI.INSTANCE.ftdi_usb_get_strings2(
				context.getPointer(), ptr,
				Memory.NULL, 0,
				memory, (int) memory.size(),
				Memory.NULL, 0
		);
		context.enforceOk(result);
		return memory.getString(0);
	}

	/**
	 * Gets the serial number of the device.
	 *
	 * @return The serial number of the device.
	 * @throws FTDIException
	 */
	public String getSerial() throws FTDIException
	{
		Memory memory = new Memory(STR_LENGTH);
		int result = LibFTDI.INSTANCE.ftdi_usb_get_strings2(
				context.getPointer(), ptr,
				Memory.NULL, 0,
				Memory.NULL, 0,
				memory, (int) memory.size()
		);
		context.enforceOk(result);
		return memory.getString(0);
	}
}
