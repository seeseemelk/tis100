package org.ftdi.c;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.PointerByReference;

public interface LibFTDI extends Library
{
	public static LibFTDI INSTANCE = Native.load("ftdi1", LibFTDI.class);

	int ftdi_init(Pointer context);

	Pointer ftdi_new();

	int ftdi_set_interface(Pointer context, int interf);

	void ftdi_deinit(Pointer context);

	void ftdi_free(Pointer context);

	void ftdi_set_usbdev(Pointer context, Pointer usbdev);

	FTDIVersionInfo_C ftdi_get_library_version();

	int ftdi_usb_find_all(Pointer context, PointerByReference devlist, int vendor, int product);

	void ftdi_list_free(PointerByReference devlist);

	void ftdi_list_free2(Pointer devlist);

	int ftdi_usb_get_strings(Pointer context, Pointer dev, Pointer manufacturer, int mnf_len, Pointer description,
	        int desc_len, Pointer serial, int serial_len);

	int ftdi_usb_get_strings2(
			Pointer context,
			Pointer dev,
			Pointer manufacturer, int mnf_len,
			Pointer description, int desc_len,
			Pointer serial, int serial_len
	);

	int ftdi_eeprom_get_strings(Pointer context, Memory manufacturer, int mnf_len, Memory product, int prod_len,
	        Memory serial, int serial_len);

	int ftdi_eeprom_set_strings(Pointer context, String manufacturer, String product, String serial);

	int ftdi_usb_open(Pointer context, int vendor, int product);

	int ftdi_usb_open_desc(Pointer context, int vendor, int product, String description, String serial);

	int ftdi_usb_open_desc_index(Pointer context, int vendor, int product, String description, String serial,
	        int index);

	int ftdi_usb_open_bus_addr(Pointer context, byte bus, byte addr);

	int ftdi_usb_open_dev(Pointer context, Pointer dev);

	int ftdi_usb_open_string(Pointer context, String description);

	int ftdi_usb_close(Pointer context);

	int ftdi_usb_reset(Pointer context);

	int ftdi_tciflush(Pointer context);

	int ftdi_tcoflush(Pointer context);

	int ftdi_tcioflush(Pointer context);

	@Deprecated
	int ftdi_usb_purge_rx_buffer(Pointer context);

	@Deprecated
	int ftdi_usb_purge_tx_buffer(Pointer context);

	@Deprecated
	int ftdi_usb_purge_buffers(Pointer context);

	int ftdi_set_baudrate(Pointer context, int baudrate);

	int ftdi_set_line_property(Pointer context, int bits, int sbit, int parity);

	int ftdi_set_line_property2(Pointer context, int bits, int sbit, int parity, int break_type);

	int ftdi_read_data(Pointer context, byte[] buf, int size);

	int ftdi_read_data_set_chunksize(Pointer context, int chunksize);

	int ftdi_read_data_get_chunksize(Pointer context, Pointer chunksize);

	int ftdi_write_data(Pointer context, byte[] buf, int size);

	int ftdi_write_data_set_chunksize(Pointer context, int chunksize);

	int ftdi_write_data_get_chunksize(Pointer context, Pointer chunksize);

//    int ftdi_readstream(Pointer context, FTDIStreamCallback *callback,
//                        void *userdata, int packetsPerTransfer, int numTransfers);
	Pointer ftdi_write_data_submit(Pointer context, Memory buf, int size);

	Pointer ftdi_read_data_submit(Pointer context, Memory buf, int size);

	int ftdi_transfer_data_done(Pointer tc);

	void ftdi_transfer_data_cancel(Pointer tc, Pointer to);

	int ftdi_set_bitmode(Pointer context, byte bitmask, byte mode);

	int ftdi_disable_bitbang(Pointer context);

	int ftdi_read_pins(Pointer context, ByteByReference pins);

	int ftdi_set_latency_timer(Pointer context, byte latency);

	int ftdi_get_latency_timer(Pointer context, Pointer latency);

	int ftdi_poll_modem_status(Pointer context, Pointer status);

	/* flow control */
	int ftdi_setflowctrl(Pointer context, int flowctrl);

	int ftdi_setflowctrl_xonxoff(Pointer context, byte xon, byte xoff);

	int ftdi_setdtr_rts(Pointer context, int dtr, int rts);

	int ftdi_setdtr(Pointer context, int state);

	int ftdi_setrts(Pointer context, int state);

	int ftdi_set_event_char(Pointer context, byte eventch, byte enable);

	int ftdi_set_error_char(Pointer context, byte errorch, byte enable);

	/* init eeprom for the given FTDI type */
	int ftdi_eeprom_initdefaults(Pointer context, Memory manufacturer, Memory product, Memory serial);

	int ftdi_eeprom_build(Pointer context);

	int ftdi_eeprom_decode(Pointer context, int verbose);

	int ftdi_get_eeprom_value(Pointer context, int value_name, Pointer value);

	int ftdi_set_eeprom_value(Pointer context, int value_name, int value);

	int ftdi_get_eeprom_buf(Pointer context, Memory buf, int size);

	int ftdi_set_eeprom_buf(Pointer context, Memory buf, int size);

	int ftdi_set_eeprom_user_data(Pointer context, String buf, int size);

	int ftdi_read_eeprom(Pointer context);

	int ftdi_read_chipid(Pointer context, Pointer chipid);

	int ftdi_write_eeprom(Pointer context);

	int ftdi_erase_eeprom(Pointer context);

	int ftdi_read_eeprom_location(Pointer context, int eeprom_addr, Pointer eeprom_val);

	int ftdi_write_eeprom_location(Pointer context, int eeprom_addr, short eeprom_val);

	String ftdi_get_error_string(Pointer context);
}
