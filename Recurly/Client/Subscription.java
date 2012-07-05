package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Client;


import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@XMLNode(name = "subscription")
public class Subscription
{
	@Subserialize
	public Subscription_Add_Ons<Subscription_Add_On> subscription_add_ons;

	@Subserialize
	public Pending_Subscription	pending_subscription;

	@Subserialize
	public NestedPlan 			plan;

	public String 				uuid;
	public String 				state;
	public String 				unit_amount_in_cents;
	public String 				quantity;
	public String 				currency;
	public String 				activated_at;
	public String 				canceled_at;
	public String 				expires_at;
	public String 				current_period_started_at;
	public String 				current_period_ends_at;
	public String 				trial_started_at;
	public String 				trial_ends_at;
	public String				total_billing_cycles;
	public String				remaining_billing_cycles;


	public Subscription()
	{
		this.plan 					= new NestedPlan();
		this.pending_subscription 	= new Pending_Subscription();
		this.subscription_add_ons 	= new Subscription_Add_Ons();
	}


	public static List<Subscription> getSubscriptions(final String state, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state))
		{
			throw new RecurlyException("Must specify a valid state (see API)");
		}

		HashMap<String, String> params = new HashMap<String, String>() {{ put("state", state); }};

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/subscriptions", null, params, per_page, Subscription.class);
	}


	public static List<Subscription> getSubscriptionsForAccount(final String account_code, final String state, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state) || RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Must specify a valid state and account code (see API)");
		}

		HashMap<String, String> params = new HashMap<String, String>() {{ put("state", state); }};

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/accounts/" + account_code + "/subscriptions", null, params, per_page, Subscription.class);
	}


	public static Subscription getSubscription(String uuid) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(uuid))
		{
			throw new RecurlyException("Must specify a valid uuid (see API)");
		}

		return RecurlyUtil.getSingleRecord(new Subscription(),
										   "https://api.recurly.com/v2/subscriptions/" + uuid,
										   null,
										   null);
	}


	public void createForAccount(Account account, String coupon_code, String starts_at, String total_billing_cycles, String first_renewal_date) throws RecurlyException
	{
		if((this.plan == null) 						||
		   (account == null) 						||
		   RecurlyUtil.nullOrEmpty(this.currency))
		{
			throw new RecurlyException("Insufficient data supplied for creating an account. See the Recurly API");
		}

		RecurlyClient 		client 				= RecurlyClient.getInstance();
		Posted_Subscription	subscription		= new Posted_Subscription();
		AccountSummary		accountSummary		= new AccountSummary();
		BillingInfoSummary	billingInfoSummary	= new BillingInfoSummary();
		Add_Ons				add_ons				= new Add_Ons();

		//add ons
		add_ons.subscription_add_on.addAll(this.subscription_add_ons);

		//billing
		billingInfoSummary.first_name			= account.billing_info.first_name;
		billingInfoSummary.last_name			= account.billing_info.last_name;
		billingInfoSummary.address1				= account.billing_info.address1;
		billingInfoSummary.address2				= account.billing_info.address2;
		billingInfoSummary.company				= account.company_name;
		billingInfoSummary.city					= account.billing_info.city;
		billingInfoSummary.state				= account.billing_info.state;
		billingInfoSummary.country				= account.billing_info.country;
		billingInfoSummary.zip					= account.billing_info.zip;
		billingInfoSummary.phone				= account.billing_info.phone;
		billingInfoSummary.vat_number			= account.billing_info.vat_number;
		billingInfoSummary.ip_address			= account.billing_info.ip_address;
		billingInfoSummary.number				= account.billing_info.number;
		billingInfoSummary.month				= account.billing_info.month;
		billingInfoSummary.year					= account.billing_info.year;
		billingInfoSummary.verification_value	= account.billing_info.verification_value;

		//account
		accountSummary.account_code				= account.account_code;
		accountSummary.accept_language			= account.accept_language;
		accountSummary.company_name				= account.company_name;
		accountSummary.email					= account.email;
		accountSummary.first_name				= account.first_name;
		accountSummary.last_name				= account.last_name;
		accountSummary.username					= account.username;
		accountSummary.billing_info				= billingInfoSummary;

		subscription.plan_code					= this.plan.plan_code;
		subscription.account 					= accountSummary;
		subscription.add_ons 					= add_ons;
		subscription.coupon_code				= coupon_code;
		subscription.unit_amount_in_cents		= this.unit_amount_in_cents;
		subscription.currency					= this.currency;
		subscription.quantity					= this.quantity;
		subscription.trial_ends_at				= this.trial_ends_at;
		subscription.starts_at					= starts_at;
		subscription.total_billing_cycles		= total_billing_cycles;
		subscription.first_renewal_date			= first_renewal_date;


		String			xml		= subscription.buildXML();
		ResponsePage 	page	= client.post("https://api.recurly.com/v2/subscriptions", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void updateInTimeframe(String timeframe) throws RecurlyException
	{
		RecurlyClient 		client 	= RecurlyClient.getInstance();
		Subscription_Update	update	= new Subscription_Update();
		Add_Ons				add_ons	= new Add_Ons();

		//add ons
		add_ons.subscription_add_on.addAll(this.subscription_add_ons);

		update.timeframe			= timeframe;
		update.plan_code			= this.plan.plan_code;
		update.quantity				= this.quantity;
		update.unit_amount_in_cents	= this.unit_amount_in_cents;
		update.add_ons 				= add_ons;

		String			xml		= update.buildXML();
		ResponsePage    page	= client.put("https://api.recurly.com/v2/subscriptions/" + this.uuid, null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void cancel() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.uuid))
		{
			throw new RecurlyException("No uuid supplied in Subscription object -- cannot cancel");
		}

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		ResponsePage    page	= client.put("https://api.recurly.com/v2/subscriptions/" + this.uuid + "/cancel", null, null, null);
	}


	public void reactivate() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.uuid))
		{
			throw new RecurlyException("No uuid supplied in Subscription object -- cannot reactivate");
		}

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		ResponsePage    page	= client.put("https://api.recurly.com/v2/subscriptions/" + this.uuid + "/reactivate", null, null, null);
	}


	public void terminate(String refundType) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.uuid))
		{
			throw new RecurlyException("No uuid supplied in Subscription object -- cannot cancel");
		}
		
		if((refundType == null) || 
		   (refundType.equals("partial") || refundType.equals("full") || refundType.equals("none")) == false)
		{
			refundType = "none";
		}

		final String		refund_type	= refundType;
		RecurlyClient 		client		= RecurlyClient.getInstance();
		Map<String, String>	params		= new HashMap<String, String>() {{ put("refund_type", refund_type);}};
		ResponsePage    	page		= client.put("https://api.recurly.com/v2/subscriptions/" + this.uuid + "/terminate", null, params, null);
	}


	@XMLNode(name = "plan")
	public static class NestedPlan
	{
		public String plan_code;
		public String name;

		public String buildXML() throws RecurlyException
		{
			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "pending_subscription")
	public static class Pending_Subscription
	{
		@Subserialize
		public NestedPlan plan;

		@Subserialize
		public List<Subscription_Add_On> subscription_add_ons;

		public String unit_amount_in_cents;
		public String quantity;


		public String buildXML() throws RecurlyException
		{
			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "subscription")
	public static class Subscription_Update
	{
		@Subserialize
		public Add_Ons add_ons;

		public String timeframe;	//"now" or "renewal"
		public String plan_code;
		public String quantity;
		public String unit_amount_in_cents;


		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.timeframe))
			{
				throw new RecurlyException("Insufficient data supplied for creating an account. See the Recurly API");
			}

			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "subscription")
	public static class Posted_Subscription
	{
		@Subserialize
		public AccountSummary account;

		@Subserialize
		public Add_Ons add_ons;

		public String plan_code;
		public String coupon_code;
		public String unit_amount_in_cents;
		public String currency;
		public String quantity;
		public String trial_ends_at;
		public String starts_at;
		public String total_billing_cycles;
		public String first_renewal_date;
		
		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.plan_code)	||
			   (this.account == null) 					||
			   RecurlyUtil.nullOrEmpty(this.currency))
			{
				throw new RecurlyException("Insufficient data supplied for creating an account. See the Recurly API");
			}

			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "subscription_add_on")
	public static class Subscription_Add_On
	{
		public String add_on_code;
		public String quantity;
		public String unit_amount_in_cents;
		
		
		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.add_on_code))
			{
				throw new RecurlyException("Must specify an add on code");
			}

			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "subscription_add_ons")
	public static class Subscription_Add_Ons<Subscription_Add_On> extends ArrayList<Subscription_Add_On>
	{

	}


	@XMLNode(name = "subscription_add_ons")
	public static class Add_Ons
	{
		@Subserialize
		public ArrayList<Subscription_Add_On> subscription_add_on;

		public Add_Ons()
		{
			this.subscription_add_on = new ArrayList<Subscription_Add_On>();
		}

		public void add(Subscription_Add_On add_on)
		{
			if(this.subscription_add_on == null)
			{
				this.subscription_add_on = new ArrayList<Subscription_Add_On>();
			}

			this.subscription_add_on.add(add_on);
		}
	}


	@XMLNode(name = "account")
	public static class AccountSummary
	{
		@Subserialize
		public BillingInfoSummary billing_info;

		public String account_code;
		public String username;
		public String email;
		public String first_name;
		public String last_name;
		public String company_name;
		public String accept_language;

		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.account_code))
			{
				throw new RecurlyException("A transaction requires valid values for account code");
			}

			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "billing_info")
	public static class BillingInfoSummary
	{
		public String first_name;
		public String last_name;
		public String address1;
		public String address2;
		public String company;
		public String city;
		public String state;
		public String country;
		public String zip;
		public String phone;
		public String vat_number;
		public String ip_address;
		public String number;
		public String month;
		public String year;
		public String verification_value;

		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.first_name) ||
			   RecurlyUtil.nullOrEmpty(this.last_name)	||
			   RecurlyUtil.nullOrEmpty(this.number) 	||
			   RecurlyUtil.nullOrEmpty(this.month) 		||
			   RecurlyUtil.nullOrEmpty(this.year))
			{
				throw new RecurlyException("Insufficient billing information (see API)");
			}

			return Serializer.serialize(this);
		}
	}
}
