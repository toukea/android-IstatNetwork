package istat.android.network.util;

import istat.android.network.util.ToolKits.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ARP {
	public static List<String> getConnectedIpAddresses() {
		List<String> address = new ArrayList<String>();

		try {
			String response = Stream.streamToLinearisedString(Runtime
					.getRuntime().exec("cat /proc/net/arp").getInputStream(),
					"UTF-8");
			Pattern p = Pattern.compile("((\\d{0,3}\\.){3})((\\d{0,3}))");
			Matcher m = p.matcher(response);
			while (m.find()) {
				address.add(m.group());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}
	public static List<String> getConnectedMacAddresses() {
		List<String> address = new ArrayList<String>();

		try {
			String response = Stream.streamToLinearisedString(Runtime
					.getRuntime().exec("cat /proc/net/arp").getInputStream(),
					"UTF-8");
			Pattern p = Pattern.compile("..\\...\\...\\...\\...\\...");
			Matcher m = p.matcher(response);
			while (m.find()) {
				address.add(m.group());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}
}
