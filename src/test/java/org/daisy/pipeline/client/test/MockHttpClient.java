package org.daisy.pipeline.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.http.DP2HttpClient;
import org.daisy.pipeline.utils.XML;
import org.w3c.dom.Document;

public class MockHttpClient implements DP2HttpClient {

	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
		try {
			return new Pipeline2WSResponse(endpoint+path, 200, "OK", "Mock object retrieved successfully", "application/xml", null, get(URLDecoder.decode(path,"UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String, String> parameters) throws Pipeline2WSException {
		try {
			return new Pipeline2WSResponse(endpoint+path, 200, "OK", "Mock object retrieved successfully", "application/xml", null, get(URLDecoder.decode(path,"UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2WSException {
		return null;
	}
	
	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2WSException {
		return null;
	}
	
	/**
	 * Read mock response
	 * 
	 * @param path
	 * @return
	 */
	private InputStream get(String path) {
		File responseFile = new File("src/test/resources/responses"+path+".xml");
		
		if (path.matches("^/jobs/[^/]+$")) {
			// fix absolute file paths in job XML
			try {
				File tempFile = File.createTempFile("MockHttpClient", null);
				
				Scanner scanner = new Scanner(responseFile);
				String content = scanner.useDelimiter("\\Z").next();
				scanner.close();
				content = content.replaceAll("(?s) file=\"/jobs/", " file=\""+new File("src/test/resources/responses/").toURI().toString()+"jobs/");
				PrintWriter out = new PrintWriter(tempFile);
				out.println(content);
				out.close();
				
				responseFile = tempFile;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Pipeline2WS.logger().info("Reading mock response: "+responseFile.getAbsolutePath());
		try {
			return new FileInputStream(responseFile);
		} catch (FileNotFoundException e) {
			Pipeline2WS.logger().info("Unable to read mock response for: "+path);
			e.printStackTrace();
			return null;
		}
	}

}