package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Client;


import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.*;

import java.util.HashMap;
import java.util.List;


@XMLNode(name = "plan")
public class Plan
{
	@Subserialize
	public Setup_Fee_In_Cents setup_fee_in_cents;

	@Subserialize
	public Unit_Amount_In_Cents unit_amount_in_cents;

	public String plan_code;
	public String name;
	public String description;
	public String accounting_code;
	public String plan_interval_unit;
	public String plan_interval_length;
	public String total_billing_cycles;
	public String unit_name;
	public String display_quantity;
	public String success_url;
	public String cancel_url;
	public String display_donation_amounts;
	public String display_phone_number;
	public String bypass_hosted_confirmation;
	public String payment_page_tos_link;
	public String trial_interval_length;
	public String trial_interval_unit;
	public String created_at;


	public Plan()
	{
		this.unit_amount_in_cents 	= new Unit_Amount_In_Cents();
		this.setup_fee_in_cents		= new Setup_Fee_In_Cents();
	}


	public static List<Plan> getAllPlans(int per_page) throws RecurlyException
	{
		HashMap<String, String> params = new HashMap<String, String>();

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/plans", null, params, per_page, Plan.class);
	}
	
	
	public static Plan getPlan(String plan_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(plan_code))
		{
			throw new RecurlyException("Must specify an account code");
		}

		return RecurlyUtil.getSingleRecord(new Plan(),
										   "https://api.recurly.com/v2/plans/" + plan_code,
										   null,
										   null);
	}


	public void create() throws RecurlyException
	{
		RecurlyClient 	client 	= RecurlyClient.getInstance();
		CreatePlan		plan	= new CreatePlan();
		
		plan.accounting_code		= this.accounting_code;
		plan.cancel_url				= this.cancel_url;
		plan.description			= this.description;
		plan.display_quantity		= this.display_quantity;
		plan.name					= this.name;
		plan.plan_code				= this.plan_code;
		plan.plan_interval_length	= this.plan_interval_length;
		plan.plan_interval_unit		= this.plan_interval_unit;
		plan.setup_fee_in_cents		= this.setup_fee_in_cents;
		plan.success_url			= this.success_url;
		plan.total_billing_cycles	= this.total_billing_cycles;
		plan.trial_interval_length	= this.trial_interval_length;
		plan.trial_interval_unit	= this.trial_interval_unit;
		plan.unit_amount_in_cents	= this.unit_amount_in_cents;
		plan.unit_name				= this.unit_name;
		
		String			xml		= plan.buildXML();
		ResponsePage	page	= client.post("https://api.recurly.com/v2/plans", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void update() throws RecurlyException
	{
		RecurlyClient 	client 	= RecurlyClient.getInstance();
		CreatePlan		plan	= new CreatePlan();

		plan.accounting_code		= this.accounting_code;
		plan.cancel_url				= this.cancel_url;
		plan.description			= this.description;
		plan.display_quantity		= this.display_quantity;
		plan.name					= this.name;
		plan.plan_code				= this.plan_code;
		plan.plan_interval_length	= this.plan_interval_length;
		plan.plan_interval_unit		= this.plan_interval_unit;
		plan.setup_fee_in_cents		= this.setup_fee_in_cents;
		plan.success_url			= this.success_url;
		plan.total_billing_cycles	= this.total_billing_cycles;
		plan.trial_interval_length	= this.trial_interval_length;
		plan.trial_interval_unit	= this.trial_interval_unit;
		plan.unit_amount_in_cents	= this.unit_amount_in_cents;
		plan.unit_name				= this.unit_name;

		String			xml		= plan.buildXML();
		ResponsePage    page	= client.put("https://api.recurly.com/v2/plans/" + this.plan_code, null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void delete() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.plan_code))
		{
			throw new RecurlyException("An Plan MUST have an plan code before it can be updated");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/plans/" + this.plan_code, null, null);
	}


	@XMLNode(name = "unit_amount_in_cents")
	public static class Unit_Amount_In_Cents
	{
		public String USD;
		public String EUR;
		public String GBP;
	}


	@XMLNode(name = "setup_fee_in_cents")
	public static class Setup_Fee_In_Cents
	{
		public String USD;
		public String EUR;
		public String GBP;
	}


	@XMLNode(name = "plan")
	public static class CreatePlan
	{
		@Subserialize
		public Setup_Fee_In_Cents setup_fee_in_cents;

		@Subserialize
		public Unit_Amount_In_Cents unit_amount_in_cents;

		public String plan_code;
		public String name;
		public String description;
		public String accounting_code;
		public String plan_interval_unit;
		public String plan_interval_length;
		public String unit_name;
		public String display_quantity;
		public String success_url;
		public String cancel_url;
		public String total_billing_cycles;
		public String trial_interval_length;
		public String trial_interval_unit;

		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.plan_code) || RecurlyUtil.nullOrEmpty(name) || (unit_amount_in_cents == null))
			{
				throw new RecurlyException("New plans must have at minimum a plan code, a name, and a unit amount in cents");
			}

			return Serializer.serialize(this);
		}
	}
}
