package Recurly.Client;

import Recurly.Util;

import java.util.HashMap;
import java.util.List;


@XMLNode(name = "account")
public class Account
{
	@Subserialize
	public Billing_Info billing_info;

	public String account_code;
	public String username;
	public String email;
	public String first_name;
	public String last_name;
	public String company_name;
	public String accept_language;			//An ISO 639-1 language code from the user's browser, indicating their preferred language and locale.
	public String state;					//"active" or "closed"
	public String hosted_login_token;		//Unique token for automatically logging the account in to the hosted management pages
	public String created_at;				//2011-10-25T12:00:00


	public static Account getAccount(String accountCode) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(accountCode))
		{
			throw new RecurlyException("Must specify an account code");
		}

		return RecurlyUtil.getSingleRecord(new Account(),
										   "https://api.recurly.com/v2/accounts/" + accountCode,
										   null,
										   null);
	}


	public static List<Account> getAccounts(final String state, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state))
		{
			throw new RecurlyException("Must specify a valid state (see API)");
		}

		HashMap<String, String>	params = new HashMap<String, String>() {{ put("state", state); }};
		
		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/accounts", null, params, per_page, Account.class);
	}


	public void create() throws RecurlyException
	{
		RecurlyClient 	client 	= RecurlyClient.getInstance();
		CreateAccount	account	= new CreateAccount();
		
		account.accept_language	= this.accept_language;
		account.account_code	= this.account_code;
		account.billing_info	= this.billing_info;
		account.company_name	= this.company_name;
		account.email			= this.email;
		account.first_name		= this.first_name;
		account.last_name		= this.last_name;
		account.username		= this.username;

		String			xml		= account.buildXML();
		ResponsePage	page	= client.post("https://api.recurly.com/v2/accounts", null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}
	
	
	public void update() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.account_code))
		{
			throw new RecurlyException("An Account MUST have an account code before it can be updated");
		}

		RecurlyClient 	client 	= RecurlyClient.getInstance();
		UpdateAccount	account	= new UpdateAccount();

		account.accept_language	= this.accept_language;
		account.company_name	= this.company_name;
		account.email			= this.email;
		account.first_name		= this.first_name;
		account.last_name		= this.last_name;
		account.username		= this.username;

		String			xml		= Serializer.serialize(account);
		ResponsePage    page	= client.put("https://api.recurly.com/v2/accounts/" + this.account_code, null, null, xml);

		RecurlyUtil.deserialize(page, this);
	}


	public void close() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.account_code))
		{
			throw new RecurlyException("An Account MUST have an account code before it can be updated");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/accounts/" + this.account_code, null, null);
	}


	public void reopen() throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(this.account_code))
		{
			throw new RecurlyException("An Account MUST have an account code before it can be updated");
		}

		RecurlyClient	client	= RecurlyClient.getInstance();
		ResponsePage	page	= client.put("https://api.recurly.com/v2/accounts/" + this.account_code + "/reopen", null, null, "");

		RecurlyUtil.deserialize(page, this);
	}


	@XMLNode(name = "account")
	public static class UpdateAccount
	{
		public String username;
		public String email;
		public String first_name;
		public String last_name;
		public String company_name;
		public String accept_language;			//An ISO 639-1 language code from the user's browser, indicating their preferred language and locale.
	}


	@XMLNode(name = "account")
	public static class CreateAccount
	{
		@Subserialize
		public Billing_Info billing_info;

		public String account_code;
		public String username;
		public String email;
		public String first_name;
		public String last_name;
		public String company_name;
		public String accept_language;			//An ISO 639-1 language code from the user's browser, indicating their preferred language and locale.

		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(this.account_code))
			{
				throw new RecurlyException("An Account MUST have an account code before it can be updated");
			}

			return Serializer.serialize(this);
		}
	}
}
