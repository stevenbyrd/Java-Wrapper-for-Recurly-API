package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Client;


import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@XMLNode(name = "coupon")
public class Coupon
{
	@Subserialize
	public Discount_In_Cents discount_in_cents;

	@Subserialize
	public Plan_Codes plan_codes;

	public String coupon_code;
	public String name;
	public String hosted_description;
	public String invoice_description;
	public String redeem_by_date;
	public String single_use;
	public String applies_for_months;
	public String max_redemptions;
	public String applies_to_all_plans;
	public String discount_type;								//percent or dollars
	public String discount_percent;
	public String created_at;
	public String state;										//redeemable, expired, maxed_out


	public Coupon()
	{
		this.plan_codes 		= new Plan_Codes();
		this.discount_in_cents	= new Discount_In_Cents();
	}


	public static List<Coupon> getCoupons(final String state, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state))
		{
			throw new RecurlyException("Must provide valid state");
		}

		HashMap<String, String>	params = new HashMap<String, String>() {{ put("state", state); }};

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/coupons", null, params, per_page, Coupon.class);
	}


	public static Coupon getCoupon(String couponCode) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(couponCode))
		{
			throw new RecurlyException("Coupon code must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Coupon(),
										   "https://api.recurly.com/v2/coupons/" + couponCode,
										   null,
										   null);
	}


	public void create() throws RecurlyException
	{
		RecurlyClient 	client 	= RecurlyClient.getInstance();
		CreateCoupon	coupon 	= new CreateCoupon();

		coupon.applies_for_months	= this.applies_for_months;
		coupon.applies_to_all_plans	= this.applies_to_all_plans;
		coupon.coupon_code			= this.coupon_code;
		coupon.discount_in_cents	= this.discount_in_cents;
		coupon.discount_percent		= this.discount_percent;
		coupon.discount_type		= this.discount_type;
		coupon.hosted_description	= this.hosted_description;
		coupon.invoice_description	= this.invoice_description;
		coupon.max_redemptions		= this.max_redemptions;
		coupon.name					= this.name;
		coupon.plan_codes			= this.plan_codes;
		coupon.redeem_by_date		= this.redeem_by_date;
		coupon.single_use			= this.single_use;

		String			xml		= coupon.buildXML();
		ResponsePage	page 	= client.post("https://api.recurly.com/v2/coupons", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void delete() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.coupon_code))
		{
			throw new RecurlyException("Coupon code must not be null");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/coupons/" + this.coupon_code, null, null);
	}


	@XMLNode(name = "discount_in_cents")
	public static class Discount_In_Cents
	{
		public String USD;
		public String EUR;
		public String GBP;
	}


	@XMLNode(name = "plan_codes")
	public static class Plan_Codes
	{
		public ArrayList<String> plan_code;

		public Plan_Codes()
		{
			this.plan_code = new ArrayList<String>();
		}
	}


	@XMLNode(name = "coupon")
	public static class CreateCoupon
	{
		@Subserialize
		public Discount_In_Cents discount_in_cents;

		@Subserialize
		public Plan_Codes plan_codes;

		public String coupon_code;
		public String name;
		public String hosted_description;
		public String invoice_description;
		public String redeem_by_date;
		public String single_use;
		public String applies_for_months;
		public String max_redemptions;
		public String applies_to_all_plans;
		public String discount_type;			//percent or dollars
		public String discount_percent;

		private String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.name) 			||
			   RecurlyUtil.nullOrEmpty(this.discount_type) 	||
			   RecurlyUtil.nullOrEmpty(this.coupon_code))
			{
				throw new RecurlyException("An Account MUST have a name, discount_type, and coupon_code before it can be updated");
			}

			return Serializer.serialize(this);
		}
	}
}
