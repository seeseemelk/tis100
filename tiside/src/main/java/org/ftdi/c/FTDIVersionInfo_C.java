package org.ftdi.c;

import com.sun.jna.Structure;

public class FTDIVersionInfo_C extends Structure
{
	public int major;
	public int minor;
	public int micro;

	public String version_str;
	public String snapshot_str;
}
