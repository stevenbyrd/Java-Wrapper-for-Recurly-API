package Recurly.Util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;


public class Serializer
{
	public static String serialize(Object obj)
	{
		StringBuilder 			xmlBuilder 			= new StringBuilder();
		Class 					targetClass 		= obj.getClass();
		String					className			= targetClass.getSimpleName().toLowerCase();
		Annotation[]			classAnnotations	= targetClass.getDeclaredAnnotations();

		for(Annotation annotation : classAnnotations)
		{
			if(annotation instanceof XMLNode)
			{
				className = ((XMLNode)annotation).name();

				break;
			}
		}
		
		xmlBuilder.append("<" + className + ">");

		for(Field field : targetClass.getDeclaredFields())
		{
			try
			{
				Object value = field.get(obj);

				if(RecurlyUtil.nullOrEmpty(value) == false)
				{
					if(List.class.isAssignableFrom(field.getType()))
					{
						List lst = (List)value;

						for(Object element : lst)
						{
							getXmlForObject(field, element, xmlBuilder);
						}
					}
					else
					{
						getXmlForObject(field, value, xmlBuilder);
					}
				}
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		xmlBuilder.append("</" + className + ">");
		
		return xmlBuilder.toString();
	}
	
	
	private static void getXmlForObject(Field field, Object obj, StringBuilder xmlBuilder) throws IllegalAccessException
	{
		Subserialize subserialize = null;

		if(field.getDeclaredAnnotations() != null)
		{
			for(Annotation annotation : field.getDeclaredAnnotations())
			{
				if(annotation instanceof Subserialize)
				{
					subserialize = (Subserialize)annotation;

					break;
				}
			}
		}

		if(subserialize != null)
		{
			String fieldXML = Serializer.serialize(obj);

			xmlBuilder.append(fieldXML);
		}
		else
		{
			xmlBuilder.append("<" + field.getName() + ">");
			xmlBuilder.append(obj.toString());
			xmlBuilder.append("</" + field.getName() + ">");
		}
	}
}
