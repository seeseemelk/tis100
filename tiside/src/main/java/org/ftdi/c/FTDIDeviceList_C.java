package org.ftdi.c;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"next", "dev"})
public class FTDIDeviceList_C extends Structure
{
	public static class ByReference extends FTDIDeviceList_C implements Structure.ByReference {}

	public FTDIDeviceList_C.ByReference next;
	public Pointer dev;

	public FTDIDeviceList_C()
	{
	}

	public FTDIDeviceList_C(Pointer ptr)
	{
		super(ptr);
		read();
	}
}
