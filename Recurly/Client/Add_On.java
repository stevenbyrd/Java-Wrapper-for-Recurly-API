package Recurly.Client;


import Recurly.Util;

import java.util.HashMap;
import java.util.List;


@XMLNode(name = "add_on")
public class Add_On
{
	@Subserialize
	public Unit_Amount_In_Cents unit_amount_in_cents;

	public String add_on_code;
	public String name;
	public String default_quantity;
	public String display_quantity_on_hosted_page;
	public String created_at;
	
	
	public Add_On()
	{
		this.unit_amount_in_cents = new Unit_Amount_In_Cents();
	}
	
	
	public static List<Add_On> getAddonsForPlan(String plan_code, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code))
		{
			throw new RecurlyException("Must specify a plan_code (see API)");
		}

		HashMap<String, String> params = new HashMap<String, String>();

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/plans/" + plan_code + "/add_ons", null, params, per_page, Add_On.class);
	}


	public static Add_On getAddonForPlan(String plan_code, String add_on_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code) || RecurlyUtil.nullOrEmpty(add_on_code))
		{
			throw new RecurlyException("Must specify an add_on_code and plan_code");
		}

		return RecurlyUtil.getSingleRecord(new Add_On(),
										   "https://api.recurly.com/v2/plans/" + plan_code + "/add_ons/" + add_on_code,
										   null,
										   null);
	}


	public void create(String plan_code, String accounting_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code))
		{
			throw new RecurlyException("Must include plan_code!");
		}

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		CreateAddOn		addOn	= new CreateAddOn();
		
		addOn.accounting_code					= accounting_code;
		addOn.add_on_code						= this.add_on_code;
		addOn.default_quantity					= this.default_quantity;
		addOn.display_quantity_on_hosted_page	= this.display_quantity_on_hosted_page;
		addOn.name								= this.name;
		addOn.unit_amount_in_cents				= this.unit_amount_in_cents;
		

		String			xml		= addOn.buildXML();
		ResponsePage 	page	= client.post("https://api.recurly.com/v2/plans/" + plan_code + "/add_ons", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void updateForPlan(String plan_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code))
		{
			throw new RecurlyException("Must include plan_code!");
		}

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		UpdateAddOn		addOn	= new UpdateAddOn();

		addOn.unit_amount_in_cents				= this.unit_amount_in_cents;
		addOn.name								= this.name;
		addOn.display_quantity_on_hosted_page	= this.display_quantity_on_hosted_page;
		addOn.default_quantity					= this.default_quantity;

		String			xml		= addOn.buildXML();
		ResponsePage 	page	= client.put("https://api.recurly.com/v2/plans/" + plan_code + "/add_ons/" + this.add_on_code, null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void deleteForPlan(String plan_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code) || RecurlyUtil.nullOrEmpty(this.add_on_code))
		{
			throw new RecurlyException("Must include plan_code and add_on_code!");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/plans/" + plan_code + "/add_ons/" + this.add_on_code, null, null);
	}


	@XMLNode(name = "unit_amount_in_cents")
	public static class Unit_Amount_In_Cents
	{
		public String USD;
		public String EUR;
		public String GBP;
	}


	@XMLNode(name = "add_on")
	public static class UpdateAddOn
	{
		@Subserialize
		public Unit_Amount_In_Cents unit_amount_in_cents;

		public String name;
		public String default_quantity;
		public String display_quantity_on_hosted_page;


		public String buildXML() throws RecurlyException
		{
			if(this.unit_amount_in_cents == null)
			{
				throw new RecurlyException("Must include add_on_code!");
			}

			return Serializer.serialize(this);
		}
	}
	
	
	@XMLNode(name = "add_on")
	public static class CreateAddOn
	{
		@Subserialize
		public Unit_Amount_In_Cents unit_amount_in_cents;

		public String add_on_code;
		public String name;
		public String default_quantity;
		public String display_quantity_on_hosted_page;
		public String accounting_code;


		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.add_on_code))
			{
				throw new RecurlyException("Must include add_on_code!");
			}

			return Serializer.serialize(this);
		}
	}
}
