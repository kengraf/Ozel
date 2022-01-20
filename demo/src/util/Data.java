package util;

public class Data {
	/**
	 * Turns array of bytes into string
	 *
	 * @param buf	Array of bytes to convert to hex string
	 * @return	Generated hex string
	 */
	public static String asHex (byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 3);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
			strbuf.append(" ");
		}

		return strbuf.toString();
	}
}
