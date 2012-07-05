package Recurly.Util;


import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;


public class XMLArrayHandler extends DefaultHandler
{
	private ArrayList<Object> 	result;
	private StringBuilder		buffer;
	private boolean 			inObject;
	private String				objectTag;
	private Class				objectClass;
	private int 				openTags;
	private boolean 			isRoot;
	public	boolean				error;


	public XMLArrayHandler(Class objClass)
	{
		this.result			= new ArrayList<Object>();
		this.buffer			= new StringBuilder();
		this.inObject		= false;
		this.objectClass	= objClass;
		this.openTags		= 0;
		this.isRoot			= true;
		
		Annotation[] annotations = objClass.getDeclaredAnnotations();

		for(Annotation annotation : annotations)
		{
			if(annotation instanceof XMLNode)
			{
				this.objectTag = ((XMLNode)annotation).name();

				break;
			}
		}
	}


	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		if(qName.equals("error") || qName.equals("errors"))
		{
			this.error = true;
		}
		else
		{
			if(this.isRoot == true)
			{
				this.isRoot = false;
			}
			else
			{
				this.openTags++;

				if(qName.equals(this.objectTag) && (this.inObject == false))
				{
					this.inObject 	= true;
					this.buffer		= new StringBuilder();
				}
				else if(this.inObject)
				{
					this.buffer.append("<" + qName + ">");
				}
			}
		}
	}


	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if(this.error == false)
		{
			openTags--;

			if(this.inObject && qName.equals(this.objectTag) && (this.openTags == 0))
			{
				try
				{
					Object				object		= this.objectClass.newInstance();
					String 				objectXML	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><" + this.objectTag + ">" + this.buffer.toString() + "</" + this.objectTag + ">";
					SAXParserFactory 	spf 		= SAXParserFactory.newInstance();
					SAXParser 			sp 			= spf.newSAXParser();
					XMLHandler 			handler 	= new XMLHandler(object);
					XMLReader 			xr 			= sp.getXMLReader();

					xr.setContentHandler(handler);
					xr.parse(new InputSource(new StringReader(objectXML)));

					this.result.add(object);

					this.inObject = false;
				}
				catch(ParserConfigurationException e)
				{
					e.printStackTrace();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				catch(IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch(InstantiationException e)
				{
					e.printStackTrace();
				}
			}
			else if(this.inObject)
			{
				this.buffer.append("</" + qName + ">");
			}
		}
	}


	@Override
	public void characters(char ch[], int start, int length)
	{
		if(this.inObject)
		{
			for(int i=start; i<start+length; i++)
			{
				this.buffer.append(ch[i]);
			}
		}
	}


	public ArrayList<Object> getResult()
	{
		return this.result;
	}
}
