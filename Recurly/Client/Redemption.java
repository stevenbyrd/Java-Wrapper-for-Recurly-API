package Recurly.Client;


import Recurly.Util;


@XMLNode(name = "redemption")
public class Redemption
{
	public String currency;
	public String single_use;
	public String total_discounted_in_cents;
	public String created_at;

	
	public static Redemption getCouponRedemptionForAccount(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Account code must not be null");
		}

		return RecurlyUtil.getSingleRecord(new Redemption(),
										   "https://api.recurly.com/v2/accounts/" + account_code + "/redemption",
										   null,
										   null);
	}
	
	
	public static Redemption redeemCouponForAccount(String coupon_code, String account_code, String currency) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(coupon_code))
		{
			throw new RecurlyException("You must specify an account_code, a coupon_code, and a currency");
		}

		RedemptionPost redemption = new RedemptionPost();
		
		redemption.account_code	= account_code;
		redemption.currency		= currency;

		RecurlyClient 	client		= RecurlyClient.getInstance();
		ResponsePage	page		= client.post("https://api.recurly.com/v2/coupons/" + coupon_code + "/redeem", null, null, redemption.buildXML());

		return RecurlyUtil.deserialize(page, new Redemption());
	}


	public static void deleteCouponFromAccount(String account_code) throws RecurlyException
	{
		if(RecurlyUtil.nullOrEmpty(account_code))
		{
			throw new RecurlyException("Redemption not associated with an account (account_code null)");
		}

		RecurlyClient client = RecurlyClient.getInstance();

		client.delete("https://api.recurly.com/v2/accounts/" + account_code + "/redemption", null, null);
	}
	
	
	@XMLNode(name = "redemption")
	public static class RedemptionPost
	{
		public String account_code;
		public String currency;
		
		public String buildXML() throws RecurlyException
		{
			if(RecurlyUtil.nullOrEmpty(account_code)	||
			   RecurlyUtil.nullOrEmpty(currency))
			{
				throw new RecurlyException("You must specify an account_code and a currency");
			}

			return Serializer.serialize(this);
		}
	}
}
