package infometry;

import java.net.HttpURLConnection;
import java.net.URL;

public class ExportDataRequest {
	public static void main(String[] args) throws Exception {
		sendResponse();
	}
	public static String sendResponse() throws Exception {

		String request = "<?xml version='1.0' encoding='UTF-8'?>\r\n" + 
				"<call method=\"exportData\" callerName=\"EDW\">\r\n" + 
				"    <credentials login=\"hailawadi@guidewire2.com\" password=\"Welcome@123\" instanceCode=\"GUIDEWIRE2\"/>\r\n" + 
				"    <version isDefault=\"true\"/>\r\n" + 
				"    <format useInternalCodes=\"true\" includeUnmappedItems=\"true\" />\r\n" + 
				"    <filters>\r\n" + 
				"        <accounts>\r\n" + 
				"            <account code=\"Personnel.Headcount\" isAssumption=\"false\" includeDescendants=\"false\"/>\r\n" + 
				"        </accounts>\r\n" + 
				"        <levels>\r\n" + 
				"            <level name=\"Total Entity\" isRollup=\"true\" includeDescendants=\"false\"/>\r\n" + 
				"        </levels>\r\n" + 
				"        <timeSpan start=\"01/2020\" end=\"03/2020\"/>\r\n" + 
				"    </filters>\r\n" + 
				"    <dimensions>\r\n" + 
				"        <dimension name=\"Workday Function\"/>\r\n" + 
				"        <dimension name=\"Home Department\" />\r\n" + 
				"        <dimension name=\"Location\" />\r\n" + 
				"    </dimensions>\r\n" + 
				"</call>";

		// Make a URL connection to the Adaptive Planning web service URL
		URL url = new URL("https://api.adaptiveinsights.com/api/v18");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// Set the content type, the HTTP method, and tell the connection we expect output
		conn.setRequestProperty("content-type", "text/xml;charset=UTF-8");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);

		// Send the request
		writeRequest(conn, request);

		// Read the response
		String response = readResponse(conn);
		//System.out.println(response);
		return response;
	}
	private static void writeRequest(HttpURLConnection conn, String request) throws Exception {
		conn.getOutputStream().write(request.getBytes("UTF-8"));
	}

	private static String readResponse(HttpURLConnection conn) throws Exception {
		byte[] buffer = new byte[4096];
		StringBuilder sb = new StringBuilder();
		int amt;
		while ((amt = conn.getInputStream().read(buffer)) != -1) {
			String s = new String(buffer, 0, amt, "UTF-8");
			sb.append(s);
		}
		return sb.toString();
	}
}
