package org.ftdi;

import org.ftdi.c.FTDIDeviceList_C;
import org.ftdi.c.LibFTDI;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A wrapper around the libftdi context.
 */
public class FTDIContext implements AutoCloseable
{
	private final Pointer context;
	private byte[] byteBuffer = new byte[1];
	private boolean open = true;

	public FTDIContext()
	{
		context = LibFTDI.INSTANCE.ftdi_new();
	}

	/**
	 * Throws an exception if the given status is negative.
	 *
	 * @param status The status to validate.
	 * @throws FTDIException if the status was negative.
	 */
	public void enforceOk(int status) throws FTDIException
	{
		if (status < 0)
			throw new FTDIException(status, getError());
	}

	/**
	 * Returns the JNI pointer to the context.
	 *
	 * @return The JNI pointer to the context.
	 */
	public Pointer getPointer()
	{
		return context;
	}

	/**
	 * Finds all FTDI devices with the given VID:PID on the USB bus.
	 * With VID:PID of 0:0, it will search for all default devices.
	 *
	 * @param vid Vendor ID to search for.
	 * @param pid Product ID to search for.
	 *
	 * @return List of found FTDI device.
	 * @throws FTDIException
	 */
	public FTDIDeviceList findAll(int vid, int pid) throws FTDIException
	{
		PointerByReference ref = new PointerByReference(Pointer.NULL);
		int status = LibFTDI.INSTANCE.ftdi_usb_find_all(context, ref, vid, pid);
		FTDIDeviceList_C list = new FTDIDeviceList_C(ref.getValue());
		enforceOk(status);
		return new FTDIDeviceList(this, list);
	}

	/**
	 * Opens an FTDI device.
	 * Only one device can be opened by a context at any given time.
	 *
	 * @param device The device to open
	 * @throws FTDIException if the device could not be opened.
	 */
	public void open(FTDIDevice device) throws FTDIException
	{
		int status = LibFTDI.INSTANCE.ftdi_usb_open_dev(context, device.getPointer());
		enforceOk(status);
	}

	/**
	 * Sets the mode of each pin.
	 *
	 * @param bitmask Mode of each pin. A {@code 1} configures the pin as an
	 * output, a {@code 0} configures the pin as an input.
	 * @param mode One of BitModes.
	 * @throws FTDIException
	 */
	public void setBitMode(byte bitmask, byte mode) throws FTDIException
	{
		int status = LibFTDI.INSTANCE.ftdi_set_bitmode(context, bitmask, mode);
		enforceOk(status);
	}

	/**
	 * Writes a value to the device.
	 *
	 * @param data The data to write.
	 */
	public void write(int data) throws FTDIException
	{
		byteBuffer[0] = (byte) data;
		write(byteBuffer);
	}

	/**
	 * Writes data to the device.
	 *
	 * @param data The data to write.
	 */
	public void write(byte[] data) throws FTDIException
	{
		int status = LibFTDI.INSTANCE.ftdi_write_data(context, data, data.length);
		enforceOk(status);
	}

	/**
	 * Reads data from the device.
	 *
	 * @return The data that was read.
	 * @throws FTDIException
	 */
	public byte read() throws FTDIException
	{
		read(byteBuffer);
		return byteBuffer[0];
	}

	/**
	 * Directly read pin state, bypassing pin buffers.
	 *
	 * @return The state of the pins.
	 * @throws FTDIException
	 */
	public byte readPins() throws FTDIException
	{
		ByteByReference ref = new ByteByReference();
		int status = LibFTDI.INSTANCE.ftdi_read_pins(context, ref);
		enforceOk(status);
		return ref.getValue();
	}

	/**
	 * Reads data from the device.
	 *
	 * @param data The data to read.
	 * @throws FTDIException
	 */
	public void read(byte[] data) throws FTDIException
	{
		int status = LibFTDI.INSTANCE.ftdi_read_data(context, data, data.length);
		enforceOk(status);
	}

	@Override
	public void close() throws Exception
	{
		if (open)
		{
			open = false;
			LibFTDI.INSTANCE.ftdi_free(context);
		}
	}

	/**
	 * Gets a string describing the last error.
	 *
	 * @return The last error as a string.
	 */
	public String getError()
	{
		return LibFTDI.INSTANCE.ftdi_get_error_string(context);
	}

}
