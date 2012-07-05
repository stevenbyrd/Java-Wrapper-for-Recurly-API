package Recurly.Util;


import Recurly.Client.Account;
import org.apache.commons.lang.WordUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Stack;


public class XMLHandler extends DefaultHandler
{
	private StringBuilder	buffer;
	private Stack<Object>	objectStack;
	private Stack<Field> 	fieldStack;
	private boolean			atRoot;
	private boolean 		invalidTagFound;
	public	boolean			error;


	public XMLHandler(Object emptyObject)
	{
		this.objectStack 		= new Stack<Object>();
		this.fieldStack 		= new Stack<Field>();
		this.atRoot				= true;
		this.invalidTagFound	= false;
		this.error				= false;
		
		this.objectStack.push(emptyObject);
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
			try
			{
				if(this.atRoot)
				{
					this.atRoot	= false;
				}
				else
				{
					Object 	currentObject 	= this.objectStack.peek();
					Class	objectClass		= currentObject.getClass();

					if(List.class.isAssignableFrom(objectClass))
					{
						try
						{
							Type type = this.fieldStack.peek().getGenericType();

							if(type instanceof ParameterizedType)
							{
								ParameterizedType listType = (ParameterizedType)this.fieldStack.peek().getGenericType();

								objectClass		= (Class)listType.getActualTypeArguments()[0];
								currentObject	= objectClass.newInstance();

								this.objectStack.push(currentObject);
							}
						}
						catch (InstantiationException e)
						{
							e.printStackTrace();
						}
						catch (IllegalAccessException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						//THE BELOW IS AN UGLY HACK THAT IS NECESSARY DUE TO AN INCONSISTENCY IN HOW ACCOUNT OBJECTS ARE
						//REPRESENTED IN TRANSACTION.DETAILS.ACCOUNT XML.
						if((objectClass == Account.class) && qName.equals("company"))
						{
							qName = "company_name";
						}

						Field targetField = objectClass.getField(qName);

						this.fieldStack.push(targetField);

						if(targetField.getDeclaredAnnotations() != null)
						{
							for(Annotation annotation : targetField.getDeclaredAnnotations())
							{
								if(annotation instanceof Subserialize)
								{
									try
									{
										Class	fieldClass	= targetField.getType();
										Object	newObject	= fieldClass.newInstance();

										this.objectStack.push(newObject);
									}
									catch (InstantiationException instantiationException)
									{
										instantiationException.printStackTrace();
									}
									catch (IllegalAccessException illegalAccessException)
									{
										illegalAccessException.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
			catch (NoSuchFieldException noSuchFieldException)
			{
				//we need to ignore the next close-tag
				this.invalidTagFound = true;
			}
		}

		this.buffer = new StringBuilder();
	}


	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if(this.error == false)
		{
			if(this.invalidTagFound)
			{
				this.invalidTagFound = false;

				return;
			}

			String 			className 			= WordUtils.capitalize(qName, new char[] {'_', ' '});
			Class			currentObjectClass	= this.objectStack.peek().getClass();
			Object			value				= this.buffer.toString();
			Annotation[]	classAnnotations	= currentObjectClass.getDeclaredAnnotations();

			if(classAnnotations != null)
			{
				for(Annotation annotation : classAnnotations)
				{
					if(annotation instanceof XMLNode)
					{
						className = ((XMLNode)annotation).name();

						break;
					}
				}
			}

			if(qName.equals(className))
			{
				value = this.objectStack.pop();
			}

			try
			{
				if(this.fieldStack.size() > 0)
				{
					Object currentObject = this.objectStack.peek();

					if(List.class.isAssignableFrom(currentObject.getClass()))
					{
						((List)currentObject).add(value);
					}
					else
					{
						Field	currentField	= this.fieldStack.pop();
						Class	fieldType		= currentField.getType();

						if(value.getClass().isAssignableFrom(fieldType))
						{
							currentField.set(currentObject, value);
						}
						else if(List.class.isAssignableFrom(fieldType))
						{
							List list = (List)currentField.get(currentObject);

							list.add(value);
						}
					}
				}
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}


	@Override
	public void characters(char ch[], int start, int length)
	{
		for(int i = start; i < (start + length); i++)
		{
			this.buffer.append(ch[i]);
		}
	}
}