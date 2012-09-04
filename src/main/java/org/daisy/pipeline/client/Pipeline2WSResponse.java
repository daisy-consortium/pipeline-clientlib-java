package org.daisy.pipeline.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class Pipeline2WSResponse {
	
	public int status;
	public String statusName;
	public String statusDescription;
	private InputStream bodyStream;
	private String bodyText;
	private Document bodyXml;
	
	/**
	 * Creates a new Pipeline2WSResponse with the given HTTP status code, status name, status description and content body.
	 * 
	 * @param status
	 * @param statusName
	 * @param statusDescription
	 * @param bodyStream
	 */
	public Pipeline2WSResponse(int status, String statusName, String statusDescription, InputStream bodyStream) {
		this.status = status;
		this.statusName = statusName;
		this.statusDescription = statusDescription;
		this.bodyStream = bodyStream;
		this.bodyXml = null;
	}
	
	/**
	 * Returns the response body as a InputStream.
	 * @return
	 * @throws Pipeline2WSException 
	 */
	public InputStream asStream() throws Pipeline2WSException {
		if (bodyStream != null)
			return bodyStream;
		
		if (bodyText != null) {
			try {
				return new ByteArrayInputStream(bodyText.getBytes("utf-8"));
	        } catch(UnsupportedEncodingException e) {
	            throw new Pipeline2WSException("Unable to open body string as stream", e);
	        }
		}
		
		return null;
	}
	
	/**
	 * Returns the response body as a String.
	 * @return
	 * @throws Pipeline2WSException 
	 */
	public String asText() throws Pipeline2WSException {
		if (bodyText != null)
			return bodyText;
		
		if (bodyStream != null) {
            Writer writer = new StringWriter();
 
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(bodyStream, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
				// unable to open stream
				e.printStackTrace();
			} catch (IOException e) {
				// unable to read buffer
				e.printStackTrace();
			} finally {
            	try {
					bodyStream.close();
					bodyStream = null;
				} catch (IOException e) {
					throw new Pipeline2WSException("Unable to close stream while reading response body", e);
				}
            }
            bodyText = writer.toString();
        }
		
		else if (bodyXml != null) {
	    	try {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(bodyXml);
				transformer.transform(source, result);
				bodyText = result.getWriter().toString();
				
			} catch (TransformerException e) {
				throw new Pipeline2WSException("Unable to serialize body XML Document as string", e);
			}
		}
		
		return bodyText;
	}
	
	/**
	 * Returns the response body as an XML Document.
	 * @return
	 * @throws Pipeline2WSException 
	 */
	public Document asXml() throws Pipeline2WSException {
		if (bodyXml != null)
			return bodyXml;
		
		InputStream xmlStream = asStream();
		
		if (xmlStream == null)
			return null;
		
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		
		try {
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Pipeline2WSException(e);
		}
		
		try {
			InputSource is = new InputSource(xmlStream);
			is.setEncoding("utf-8");
			bodyXml = builder.parse(is);
		} catch (Exception e) {
			String errorMessage = asText();
			if (errorMessage != null) {
				if (errorMessage.length() > 1000)
					errorMessage = errorMessage.substring(0, 1000);
				errorMessage = ": "+errorMessage;
			}
			errorMessage = "Unable to parse body as XML: "+errorMessage;
			throw new Pipeline2WSException(errorMessage, e);
		}
		
		return bodyXml;
	}
	
}
