/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.ext.images.server.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

// TODO Is this class needed??
public class Uuid implements Serializable {
	private static final long serialVersionUID = 1118990168975148476L;
	private long high;
	private long low;
	private transient String str36;
	private static int UUID_HOST_LOCK_PORT = 5504;
	private static ServerSocket lockSocket;
	private static long timeStamp;
	private static long adapterAddress;
	private static int instanceCounter;
	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static synchronized Uuid get32Code() throws UuidException {
		if (timeStamp == 0L)
			setTimeStamp();
		if (adapterAddress == 0L)
			setAdapterAddress();
		Uuid uuid = new Uuid();
		long midTime = timeStamp >> 32 & 0xffffffffL;
		uuid.high = timeStamp << 32 | midTime << 16 & 0xffff0000L | 4096L
				| timeStamp >> 48 & 4095L;
		int count = instanceCounter++;
		if (count == 0x1fffffff) {
			instanceCounter = 0;
			setTimeStamp();
		}
		uuid.low = ((long) count & 0x1fffffffL) << 32 | 0xe000000000000000L
				| adapterAddress;
		return uuid;
	}

	private Uuid() {
		str36 = null;
	}

	private Uuid(long high, long low) {
		str36 = null;
		this.high = high;
		this.low = low;
	}

	private static void setAdapterAddress() throws UuidException {
		try {
			byte addr[] = InetAddress.getLocalHost().getAddress();
			int raw = addr[3] & 0xff | addr[2] << 8 & 0xff00 | addr[1] << 16
					& 0xff0000 | addr[0] << 24 & 0xff000000;
			adapterAddress = (long) raw & 0xffffffffL;
		} catch (UnknownHostException e) {
			throw new UuidException("Unexpected failure"); //$NON-NLS-1$
		}
	}

	private static void setTimeStamp() throws UuidException {
		acquireHostLock();
		try {
			long newTime = System.currentTimeMillis();
			if (timeStamp != 0L) {
				if (newTime < timeStamp)
					throw new UuidException("Unique identifier clock failure"); //$NON-NLS-1$
				if (newTime == timeStamp) {
					letClockTick(newTime);
					newTime = System.currentTimeMillis();
				}
			}
			timeStamp = newTime;
		} finally {
			releaseHostLock();
		}
	}

	private static void letClockTick(long curTime) throws UuidException {
		long sleepTime = 1L;
		for (long newTime = System.currentTimeMillis(); newTime == curTime; newTime = System
				.currentTimeMillis()) {
			sleepTime *= 2L;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
			}
			if (sleepTime > 60000L)
				throw new UuidException("Unique identifier unexpected failure"); //$NON-NLS-1$
		}

	}

	private static void acquireHostLock() throws UuidException {
		String portProperty = null;
		try {
			portProperty = System.getProperty("bluewater.uuid.hostLockPort"); //$NON-NLS-1$
		} catch (SecurityException e) {
		}
		if (portProperty != null)
			try {
				UUID_HOST_LOCK_PORT = Integer.parseInt(portProperty);
			} catch (NumberFormatException nfe) {
			}
		for (int numberOfRetrys = 0; lockSocket == null; numberOfRetrys++) {
			try {
				lockSocket = new ServerSocket(UUID_HOST_LOCK_PORT);
				return;
			} catch (BindException e) {
			} catch (IOException e2) {
				throw new UuidException("Unique identifier unexpected failure"); //$NON-NLS-1$
			}
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
			}
			if (numberOfRetrys == 1200)
				throw new UuidException("Unique identifier lock failure"); //$NON-NLS-1$
		}

	}

	private static void releaseHostLock() {
		if (lockSocket != null) {
			try {
				lockSocket.close();
			} catch (IOException e) {
			}
			lockSocket = null;
		}
	}

	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof Uuid))
			return high == ((Uuid) obj).high && low == ((Uuid) obj).low;
		else
			return false;
	}

	public int hashCode() {
		return (int) (low << 24) & 0xff000000 | (int) (high >> 20) & 0xfff000
				| (int) (low >> 32) & 0xfff;
	}

	public String toString() {
		if (str36 != null) {
			return str36;
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append(toHexString(high >>> 32, 8)); // .append("-");
			buf.append(toHexString(high >>> 16, 4)); // .append("-");
			buf.append(toHexString(high, 4)); // .append("-");
			buf.append(toHexString(low >>> 48, 4)); // .append("-");
			buf.append(toHexString(low, 12));
			str36 = buf.toString();
			return str36;
		}
	}

	private static String toHexString(long x, int chars) {
		char buf[] = new char[chars];
		for (int charPos = chars; --charPos >= 0;) {
			buf[charPos] = hexDigits[(int) (x & 15L)];
			x >>>= 4;
		}

		return new String(buf);
	}

	public byte[] toByteArray() {
		byte array[] = new byte[16];
		toBytes(high, array, 0);
		toBytes(low, array, 8);
		return array;
	}

	private void toBytes(long x, byte array[], int startPos) {
		for (int bytePos = 8; --bytePos >= 0;) {
			array[startPos + bytePos] = (byte) (int) (x & 255L);
			x >>>= 8;
		}

	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(high);
		out.writeLong(low);
	}

	public static Uuid read(DataInput in) throws IOException {
		long high = in.readLong();
		long low = in.readLong();
		return new Uuid(high, low);
	}

	public static Uuid read(String id) throws UuidException {
		Uuid uuid;
		String part = id.substring(0, 8);
		long high = 0L;
		high = Long.parseLong(part, 16) << 32;
		part = id.substring(9, 13);
		high |= Long.parseLong(part, 16) << 16;
		part = id.substring(14, 18);
		high |= Long.parseLong(part, 16);
		long low = 0L;
		part = id.substring(19, 23);
		low = Long.parseLong(part, 16) << 48;
		part = id.substring(24, 36);
		low |= Long.parseLong(part, 16);
		uuid = new Uuid(high, low);
		return uuid;
	}
}
