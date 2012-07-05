package Recurly.Client;


import Recurly.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@XMLNode(name = "invoice")
public class Invoice
{
	@Subserialize
	public Line_Items<Adjustment> line_items;

	@Subserialize
	public Transactions<Transaction> transactions;

	public String uuid;
	public String state;
	public String invoice_number;
	public String po_number;
	public String vat_number;
	public String subtotal_in_cents;
	public String total_in_cents;
	public String currency;
	public String created_at;


	public Invoice()
	{
		this.line_items		= new Line_Items<Adjustment>();
		this.transactions	= new Transactions<Transaction>();
	}
	
	
	public static Invoice getInvoice(String invoice_number) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(invoice_number))
		{
			throw new RecurlyException("Invoice number must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Invoice(),
										   "https://api.recurly.com/v2/invoices/" + invoice_number,
										   null,
										   null);
	}


	public static List<Invoice> getAllInvoices(final String state, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(state))
		{
			throw new RecurlyException("Must specify a valid state (see API)");
		}

		HashMap<String, String> params = new HashMap<String, String>() {{ put("state", state); }};

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/invoices", null, params, per_page, Invoice.class);
	}


	public static List<Invoice> getInvoicesForAccount(String account_code, int per_page) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Must specify a valid state (see API)");
		}

		HashMap<String, String> params = new HashMap<String, String>();

		return RecurlyUtil.getMultipleRecords("https://api.recurly.com/v2/accounts/" + account_code + "/invoices", null, params, per_page, Invoice.class);
	}


	public static Invoice invoiceAccount(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Must specify a valid state (see API)");
		}

		RecurlyClient	client	= RecurlyClient.getInstance();
		ResponsePage 	page	= client.post("https://api.recurly.com/v2/accounts/" + account_code + "/invoices", null, null, null);

		return RecurlyUtil.deserialize(page, new Invoice());
	}

	//TODO: what to do with the "get pdf for invoice" api call?


	@XMLNode(name = "transactions")
	public static class Transactions<Transaction> extends ArrayList
	{
	}


	@XMLNode(name = "line_items")
	public static class Line_Items<Adjustment> extends ArrayList<Adjustment>
	{
	}
}
