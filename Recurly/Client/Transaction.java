package com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Client;


import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.*;
import java.util.HashMap;
import java.util.List;


@XMLNode(name = "transaction")
public class Transaction
{
	@Subserialize
	public Details details;

	public String uuid;
	public String status;
	public String source;
	public String action;
	public String currency;
	public String amount_in_cents;
	public String tax_in_cents;
	public String reference;
	public String test;
	public String refundable;
	public String voidable;
	public String cvv_result;
	public String avs_result;
	public String avs_result_street;
	public String avs_result_postal;
	public String created_at;
	
	
	public Transaction()
	{
		this.details = new Details();
	}


	public static Transaction getTransaction(String transaction_uuid) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(transaction_uuid))
		{
			throw new RecurlyException("UUID must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Transaction(),
										   "https://api.recurly.com/v2/transactions/" + transaction_uuid,
										   null,
										   null);
	}


	public static List<Transaction> getAllTransactions(String state, String type, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state) ||
		   RecurlyUtil.nullOrEmpty(type))
		{
			throw new RecurlyException("Must provide valid state and type");
		}

		return getTransactions("https://api.recurly.com/v2/transactions", state, type, per_page);
	}


	public static List<Transaction> getTransactionsForAccount(String account_code, String state, String type, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code) 	||
		   RecurlyUtil.nullOrEmpty(state) 			||
		   RecurlyUtil.nullOrEmpty(type))
		{
			throw new RecurlyException("Account code must not be null, must provide valid state and type");
		}

		return getTransactions("https://api.recurly.com/v2/accounts/" + account_code + "/transactions", state, type, per_page);
	}


	private static List<Transaction> getTransactions(String endpoint, final String state, final String type, int per_page) throws RecurlyException
	{
		HashMap<String, String>	params = new HashMap<String, String>() {{ put("state", state); put("type", type); }};

		return RecurlyUtil.getMultipleRecords(endpoint, null, params, per_page, Transaction.class);
	}
	
	
	public void create(String description) throws RecurlyException
	{
		if((this.details == null)							||
		   (this.details.account == null)				 	||
		   RecurlyUtil.nullOrEmpty(this.currency) 			||
		   RecurlyUtil.nullOrEmpty(this.amount_in_cents))
		{
			throw new RecurlyException("Insufficient information provided. See API for set of required information");
		}

		TransactionPost 	transactionPost 	= new TransactionPost();
		AccountSummary		accountSummary		= new AccountSummary();
		BillingInfoSummary	billingInfoSummary	= new BillingInfoSummary();

		//billing
		billingInfoSummary.first_name			= this.details.account.billing_info.first_name;
		billingInfoSummary.last_name			= this.details.account.billing_info.last_name;
		billingInfoSummary.address1				= this.details.account.billing_info.address1;
		billingInfoSummary.address2				= this.details.account.billing_info.address2;
		billingInfoSummary.company				= this.details.account.company_name;
		billingInfoSummary.city					= this.details.account.billing_info.city;
		billingInfoSummary.state				= this.details.account.billing_info.state;
		billingInfoSummary.country				= this.details.account.billing_info.country;
		billingInfoSummary.zip					= this.details.account.billing_info.zip;
		billingInfoSummary.phone				= this.details.account.billing_info.phone;
		billingInfoSummary.vat_number			= this.details.account.billing_info.vat_number;
		billingInfoSummary.ip_address			= this.details.account.billing_info.ip_address;
		billingInfoSummary.number				= this.details.account.billing_info.number;
		billingInfoSummary.month				= this.details.account.billing_info.month;
		billingInfoSummary.year					= this.details.account.billing_info.year;
		billingInfoSummary.verification_value	= this.details.account.billing_info.verification_value;

		//account
		accountSummary.account_code		= this.details.account.account_code;
		accountSummary.accept_language	= this.details.account.accept_language;
		accountSummary.company_name		= this.details.account.company_name;
		accountSummary.email			= this.details.account.email;
		accountSummary.first_name		= this.details.account.first_name;
		accountSummary.last_name		= this.details.account.last_name;
		accountSummary.username			= this.details.account.username;
		accountSummary.billing_info		= billingInfoSummary;

		//transaction
		transactionPost.description 	= description;
		transactionPost.account			= accountSummary;
		transactionPost.currency		= this.currency;
		transactionPost.amount_in_cents	= this.amount_in_cents;

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		String			xml		= transactionPost.buildXML();
		ResponsePage	page	= client.post("https://api.recurly.com/v2/transactions", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void refund(final String cents_to_refund) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.uuid))
		{
			throw new RecurlyException("A transaction refund requires a valid value for uuid");
		}

		RecurlyClient 			client 	= RecurlyClient.getInstance();
		HashMap<String, String> params	= new HashMap<String, String>() {{ put("amount_in_cents", cents_to_refund); }};
		ResponsePage			page	= client.delete("https://api.recurly.com/v2/transactions/" + this.uuid, null, params);

		RecurlyUtil.deserialize(page, this);
	}
	

	@XMLNode(name = "transaction")
	public static class TransactionPost
	{
		@Subserialize
		public AccountSummary account;

		public String description;
		public String currency;
		public String amount_in_cents;
		
		public String buildXML() throws RecurlyException
		{
			if((this.account == null) 							||
			   RecurlyUtil.nullOrEmpty(this.currency) 			||
			   RecurlyUtil.nullOrEmpty(this.amount_in_cents))
			{
				throw new RecurlyException("A transaction requires valid values for currency, amount_in_cents, and details.account");
			}
			
			return Serializer.serialize(this);
		}
	}


	@XMLNode(name = "details")
	public static class Details
	{
		@Subserialize
		public Account account;
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
