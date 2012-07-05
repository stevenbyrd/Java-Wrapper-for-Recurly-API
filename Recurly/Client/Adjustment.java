package Recurly.Client;


import Recurly.Util;
import java.util.HashMap;
import java.util.List;


@XMLNode(name = "adjustment")
public class Adjustment
{
	public String uuid;
	public String origin;
	public String discount_in_cents;
	public String tax_in_cents;
	public String total_in_cents;
	public String taxable;
	public String start_date;
	public String end_date;
	public String created_at;
	public String currency;
	public String unit_amount_in_cents;
	public String quantity;
	public String description;
	public String accounting_code;


	public static List<Adjustment> getAdjustments(final String type, final String state, int per_page, String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(type) || RecurlyUtil.nullOrEmpty(state) || RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Must have valid type, state, and account_code");
		}

		HashMap<String, String>	params = new HashMap<String, String>() {{ put("state", state); put("type", type); }};

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/accounts/" + account_code + "/adjustments", null, params, per_page, Adjustment.class);
	}


	public static Adjustment getAdjustment(String uuid) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(uuid))
		{
			throw new RecurlyException("UUID must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Adjustment(), "https://api.recurly.com/v2/adjustments/" + uuid, null, null);
	}


	public void createForAccount(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("An Adjustment MUST have an account_code before it can be created");
		}

		RecurlyClient 		client 		= RecurlyClient.getInstance();
		CreateAdjustment	adjustment	= new CreateAdjustment();

		adjustment.accounting_code		= this.accounting_code;
		adjustment.currency				= this.currency;
		adjustment.description			= this.description;
		adjustment.quantity				= this.quantity;
		adjustment.unit_amount_in_cents	= this.unit_amount_in_cents;

		String			xml			= adjustment.buildXML();
		ResponsePage	page		= client.post("https://api.recurly.com/v2/accounts/" + account_code + "/adjustments", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}

	public void delete() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.uuid))
		{
			throw new RecurlyException("UUID must not be null");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/adjustments/" + this.uuid, null, null);
	}


	@XMLNode(name = "adjustment")
	public static class CreateAdjustment
	{
		public String currency;
		public String unit_amount_in_cents;
		public String description;
		public String quantity;
		public String accounting_code;

		private String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.currency) 				||
			   RecurlyUtil.nullOrEmpty(this.unit_amount_in_cents))
			{
				throw new RecurlyException("An Adjustment MUST have a currency and unit_amount_in_cents before it can be created");
			}

			return Serializer.serialize(this);
		}
	}
}
