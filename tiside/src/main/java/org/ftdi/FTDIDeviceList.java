package org.ftdi;

import java.util.Iterator;

import org.ftdi.c.FTDIDeviceList_C;
import org.ftdi.c.LibFTDI;

import com.sun.jna.ptr.PointerByReference;

public class FTDIDeviceList implements AutoCloseable, Iterable<FTDIDevice>
{
	private final FTDIContext context;
	private final FTDIDeviceList_C ptr;

	public FTDIDeviceList(FTDIContext context, FTDIDeviceList_C ptr)
	{
		this.context = context;
		this.ptr = ptr;
	}

	@Override
	public Iterator<FTDIDevice> iterator()
	{
		return new DeviceIterator(context, ptr);
	}

	@Override
	public void close() throws Exception
	{
		PointerByReference ref = new PointerByReference(ptr.getPointer());
		LibFTDI.INSTANCE.ftdi_list_free(ref);
	}

	private static class DeviceIterator implements Iterator<FTDIDevice>
	{
		private FTDIContext context;
		private FTDIDeviceList_C list;

		public DeviceIterator(FTDIContext context, FTDIDeviceList_C list)
		{
			this.context = context;
			this.list = list;
		}

		@Override
		public boolean hasNext()
		{
			return list != null && list.dev != null;
		}

		@Override
		public FTDIDevice next()
		{
			if (list == null)
				throw new NullPointerException();

			FTDIDevice device = new FTDIDevice(context, list.dev);
			list = list.next;
			return device;
		}
	}
}
