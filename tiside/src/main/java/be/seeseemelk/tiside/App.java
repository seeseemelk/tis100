package be.seeseemelk.tiside;

import javax.swing.SwingUtilities;

import org.ftdi.FTDIContext;
import org.ftdi.FTDIDevice;
import org.ftdi.FTDIDeviceList;

import be.seeseemelk.tiside.spi.FTDISPI;
import be.seeseemelk.tiside.tis.TisTap;
import be.seeseemelk.tiside.ui.TisIDE;

public class App
{
	public static void main(String[] args) throws Exception
	{
		FTDIContext ftdi = new FTDIContext();
		FTDIDevice device = null;
		try (FTDIDeviceList list = ftdi.findAll(0x0403, 0x6001))
		{
			for (FTDIDevice d : list)
			{
				device = d;
			}
		}

		if (device == null)
		{
			System.err.println("No device found");
			ftdi.close();
			return;
		}

		ftdi.open(device);
		FTDISPI spi = new FTDISPI(ftdi);
		spi.open();
		TisTap tap = new TisTap(spi);

//		for (int i = 0; i < 20; i++)
//		{
//			System.out.format("Result: 0x%02X%n", spi.transfer(0x03));
//			System.out.format("Result: 0x%02X%n", spi.transfer(0x01));
//			System.out.format("Result: 0x%02X%n", spi.transfer(0xAA));
//			System.out.format("Result: 0x%02X%n", spi.transfer(0x00));
//		}

		SwingUtilities.invokeLater(() ->
		{
			TisIDE ide = new TisIDE(tap);
			ide.setVisible(true);
		});

//		System.out.println("Opening context");
//		try (FTDIContext ftdi = new FTDIContext())
//		{
//			System.out.println("Context opened");
//			try (FTDIDeviceList list = ftdi.findAll(0, 0))
//			{
//				for (FTDIDevice device : list)
//				{
//					if (device.getSerial().equals("A105JI7F"))
//					{
//						System.out.format("Found device!");
//						ftdi.open(device);
//						useDevice(ftdi);
//					}
//				}
//			}
//		}
	}

//	private static void useDevice(FTDIContext ftdi) throws Exception
//	{
//		try (FTDISPI spi = new FTDISPI(ftdi))
//		{
//			spi.open();
//
//			for (int i = 0; i < 256; i++)
//			{
//				int result = spi.write(0xAA);
//				System.out.format("Result: 0x%02X%n", result);
//			}
//		}
//	}
}
