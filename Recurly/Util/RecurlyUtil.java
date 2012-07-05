package Recurly.Util;


import Recurly.Client.RecurlyClient;
import org.apache.http.Header;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RecurlyUtil
{
	public static <E> E getSingleRecord(E emptyObject, String endpoint, HashMap<String, String> headers, HashMap<String, String> params)
	throws RecurlyException
	{
		try
		{
			RecurlyClient 	client	= RecurlyClient.getInstance();
			ResponsePage 	page	= client.get(endpoint, headers, params);

			return deserialize(page, emptyObject);
		}
		catch (Exception e)
		{
			throw new RecurlyException(e.getMessage());
		}
	}


	public static <E> List<E> getMultipleRecords(String endpoint, HashMap<String, String> headers, HashMap<String, String> params,  int per_page, Class<E> arrayType)
	throws RecurlyException
	{
		ArrayList<E> retVal = new ArrayList<E>();

		if(per_page > 200)
		{
			per_page = 200;
		}
		else if (per_page < 1)
		{
			per_page = 1;
		}

		params.put("per_page", "" + per_page);

		try
		{
			RecurlyClient 		client			= RecurlyClient.getInstance();
			ResponsePage 		currentPage		= client.get(endpoint, headers, params);
			String				xml				= currentPage.getResponseText();
			Header[]			nextPageUrlList	= currentPage.getPageHeaders();
			SAXParserFactory 	spf 			= SAXParserFactory.newInstance();
			SAXParser 			sp 				= spf.newSAXParser();
			XMLArrayHandler	 	handler 		= new XMLArrayHandler(arrayType);
			XMLReader 			xr 				= sp.getXMLReader();

			xr.setContentHandler(handler);
			xr.parse(new InputSource(new StringReader(xml)));

			if(handler.error == false)
			{
				for(Object obj : handler.getResult())
				{
					retVal.add((E)obj);
				}

				while((nextPageUrlList != null) 	&&
					  (nextPageUrlList.length > 0) 	&&
					  (nextPageUrlList[0].getValue().contains("rel=\"next\"")))
				{
					String[] links = nextPageUrlList[0].getValue().split(" ");

					for(int i = 0; i < links.length; i++)
					{
						if(links[i].contains("rel=\"next\""))
						{
							String link 	= links[i - 1];
							String nextPage	= link.substring(1, link.length() - 2);
							currentPage		= client.get(nextPage, headers, params);
							xml				= currentPage.getResponseText();
							nextPageUrlList	= currentPage.getPageHeaders();
							spf 			= SAXParserFactory.newInstance();
							sp 				= spf.newSAXParser();
							handler 		= new XMLArrayHandler(arrayType);
							xr 				= sp.getXMLReader();

							xr.setContentHandler(handler);
							xr.parse(new InputSource(new StringReader(xml)));

							for(Object obj : handler.getResult())
							{
								retVal.add((E)obj);
							}

							break;
						}
					}
				}
			}
			else
			{
				String errorMessage = parseError(xml);

				throw new RecurlyException(errorMessage);
			}
		}
		catch (Exception e)
		{
			throw new RecurlyException(e.getMessage());
		}

		return retVal;
	}
	
	
	public static <E> E deserialize(ResponsePage page, E emptyObject) throws RecurlyException
	{
		try
		{
			String				xml		= page.getResponseText();
			SAXParserFactory 	spf 	= SAXParserFactory.newInstance();
			SAXParser 			sp 		= spf.newSAXParser();
			XMLHandler 			handler = new XMLHandler(emptyObject);
			XMLReader 			xr 		= sp.getXMLReader();

			xr.setContentHandler(handler);
			xr.parse(new InputSource(new StringReader(xml)));

			if(handler.error == false)
			{
				return emptyObject;
			}
			else
			{
				String errorMessage = parseError(xml);

				throw new RecurlyException(errorMessage);
			}
		}
		catch (Exception e)
		{
			throw new RecurlyException(e.getMessage());
		}
	}
	
	
	public static boolean nullOrEmpty(Object obj)
	{
		if(obj instanceof String)
		{
			String str = (String)obj;
			
			return ((str == null) || str.equals(""));
		}
		else
		{
			return obj == null;
		}
	}
	
	
	public static String parseError(String xml)
	{
		String retVal = "";

		if(xml.contains("<error>") || xml.contains("<errors>"))
		{
			String	openTag		= xml.contains("<error>") ? "<error>" : "<errors>";
			String	closeTag	= openTag.equals("<error>") ? "</error>" : "</errors>";
			int		start		= xml.indexOf(openTag);
			int		end			= xml.indexOf(closeTag);

			retVal = xml.substring(start, end) + closeTag;
		}
		
		return retVal;
	}
}
