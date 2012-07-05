package Recurly.Client;


import Recurly.Util;


@XMLNode(name = "billing_info")
public class Billing_Info
{
	public String first_name;
	public String last_name;
	public String address1;
	public String address2;
	public String city;
	public String state;
	public String country;
	public String zip;
	public String phone;
	public String vat_number;
	public String ip_address;
	public String ip_address_country;
	
	//credit card
	public String number;
	public String first_six;
	public String last_four;
	public String card_type;
	public String month;
	public String year;
	public String verification_value;
	
	//paypal
	public String billing_agreement_id;


	public static Billing_Info getBillingInfo(String accountCode) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(accountCode))
		{
			throw new RecurlyException("Account code must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Billing_Info(),
										   "https://api.recurly.com/v2/accounts/" + accountCode + "/billing_info",
										   null,
										   null);
	}


	public void update(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Insufficient information provided. Check account_code and/or see API for details");
		}


		UpdateBillingInfo update = new UpdateBillingInfo();
		
		update.address1				= this.address1;
		update.address2				= this.address2;
		update.city					= this.city;
		update.country				= this.country;
		update.first_name			= this.first_name;
		update.ip_address			= this.ip_address;
		update.last_name			= this.last_name;
		update.month				= this.month;
		update.number				= this.number;
		update.phone				= this.phone;
		update.state				= this.state;
		update.vat_number			= this.vat_number;
		update.verification_value	= this.verification_value;
		update.year					= this.year;
		update.zip					= this.zip;

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		String			xml		= update.buildXML();
		ResponsePage	page	= client.put("https://api.recurly.com/v2/accounts/" + account_code + "/billing_info", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}
	
	
	public static void clearBillingInfoForAccount(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Insufficient information provided. Check account_code and/or see API for details");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/accounts/" + account_code + "/billing_info", null, null);
	}


	@XMLNode(name = "billing_info")
	public static class UpdateBillingInfo
	{
		public String first_name;
		public String last_name;
		public String address1;
		public String address2;
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
			   RecurlyUtil.nullOrEmpty(this.last_name) 	||
			   RecurlyUtil.nullOrEmpty(this.number) 	||
			   RecurlyUtil.nullOrEmpty(this.month) 		||
			   RecurlyUtil.nullOrEmpty(this.year))
			{
				throw new RecurlyException("Insufficient information provided. Check account_code and/or see API for details");
			}

			return Serializer.serialize(this);
		}
	}
}
