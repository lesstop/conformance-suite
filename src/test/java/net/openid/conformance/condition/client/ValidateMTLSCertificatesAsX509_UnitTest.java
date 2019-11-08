package net.openid.conformance.condition.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.ConditionError;
import net.openid.conformance.logging.TestInstanceEventLog;
import net.openid.conformance.testmodule.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ValidateMTLSCertificatesAsX509_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ValidateMTLSCertificatesAsX509 cond;

	private String cert = "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";

	private String key = "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";

	// issuing first, then root
	private String caString = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlGYVR" +
		"DQ0ExR2dBd0lCQWdJRVdXZFUwekFOQmdrcWhraUc5dzBCQVFzRkFEQmpNUXN3Q1FZ" +
		"RFZRUUdFd0pIDQpRakVkTUJzR0ExVUVDaE1VVDNCbGJpQkNZVzVyYVc1bklFeHBiV" +
		"2wwWldReEVUQVBCZ05WQkFzVENGUmxjM1FnDQpVRXRKTVNJd0lBWURWUVFERXhsUG" +
		"NHVnVJRUpoYm10cGJtY2dWR1Z6ZENCU2IyOTBJRU5CTUI0WERURTNNRGN4DQpOakl" +
		"3TlRZek9Wb1hEVEkzTURjeE5qSXhNall6T1Zvd1pqRUxNQWtHQTFVRUJoTUNSMEl4" +
		"SFRBYkJnTlZCQW9UDQpGRTl3Wlc0Z1FtRnVhMmx1WnlCTWFXMXBkR1ZrTVJFd0R3W" +
		"URWUVFMRXdoVVpYTjBJRkJMU1RFbE1DTUdBMVVFDQpBeE1jVDNCbGJpQkNZVzVyYV" +
		"c1bklGUmxjM1FnU1hOemRXbHVaeUJEUVRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCDQp" +
		"CUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLUnFua05RU2VBRHhJVjU4U05DLzNCOUEzR3Rn" +
		"Z09vTkllOHRiZ2dWSjlTDQpwR3Q0WVI3dG1uK2lqL3U2dEtWYXVjNFphMmQ4UkR3O" +
		"WFvaDY2cjBlbUVNbGNTeEZNN1BsQXNRZVFYTGUyL1E5DQpGQmxSWm9qQWN1NHB5bn" +
		"o4cERqRWJjSzk0ZGNVNjFjaGN0VmYwNGlkc0J0UWxSSnFoc1dkT3pIb0J1ZVZoOUp" +
		"RDQp1RG9tNVcwKzQrdVNDM1lxdXVpa1F4RlJPWW1PQXozcVpSUVhHNm5Yc3cyYXJk" +
		"TVVSak9sZkhCTXUvbDJ6SkdGDQowRy9oVEZXdkZHTUI3ZzdKVXRHZlpmTG5nUTFaL" +
		"01vbFc2bzNjdDhOeXhBSjY3NXdzT0xlUDBMaUZGTHM2VEc0DQpvWFV4U21sdWtEcE" +
		"w3bWdjTy9iWFdOb0d5TUppWGVKTXZNUDBwVmhscFpVQ0F3RUFBYU9DQVNBd2dnRWN" +
		"NQTRHDQpBMVVkRHdFQi93UUVBd0lCQmpBU0JnTlZIUk1CQWY4RUNEQUdBUUgvQWdF" +
		"QU1JRzFCZ05WSFI4RWdhMHdnYW93DQpMS0Fxb0NpR0ptaDBkSEE2THk5dllpNTBjb" +
		"lZ6ZEdsekxtTnZiUzl2WW5SbGMzUnliMjkwWTJFdVkzSnNNSHFnDQplS0IycEhRd2" +
		"NqRUxNQWtHQTFVRUJoTUNSMEl4SFRBYkJnTlZCQW9URkU5d1pXNGdRbUZ1YTJsdVp" +
		"5Qk1hVzFwDQpkR1ZrTVJFd0R3WURWUVFMRXdoVVpYTjBJRkJMU1RFaU1DQUdBMVVF" +
		"QXhNWlQzQmxiaUJDWVc1cmFXNW5JRlJsDQpjM1FnVW05dmRDQkRRVEVOTUFzR0ExV" +
		"UVBeE1FUTFKTU1UQWZCZ05WSFNNRUdEQVdnQlRTWWtQemJVdjVDNjlZDQpMQzBycU" +
		"kwMWo5cllXVEFkQmdOVkhRNEVGZ1FVRHdIQUwraG9iUGNqdjQ1bGJva054cWFGZDd" +
		"jd0RRWUpLb1pJDQpodmNOQVFFTEJRQURnZ0lCQUR6czBuU2UweXFnb3h6cDQvVkM2" +
		"L0hrSFVqMDJNalVCNWN3cU9tanIyMjJYQlpEDQpHYTlBRXpnZ3N6YWxoT0tHUHIra" +
		"zZqWWtyRkl2L2hjNHFCU0ZhUXl0aStuaWhKV3pISnVsUHM5Ukp5bm5BcmczDQpHdF" +
		"hKUGtCdEFIOWJJOTRFYWpTVENXcVdSTlUxTjVJWS9mTVJSK1N6WVBCVzBMN3FYRzd" +
		"5bCtCZ29NUDdiMElTDQpUamVCbmgvMnVZdFdNWWNWR1VnZ2lkMWgvMEZodThvRGNh" +
		"dU1OeWkrR3dYNWJhNEVGdGhXWE5MR0Y3NTRTaDlwDQpCM1c2UGJRNjA2YXRQclExV" +
		"HQvTVBDNFBVczl1R0E3cy9YdWRxd3NSSUo3SEU5dExNeFh3aXZSbk5xd1JjNkYrDQ" +
		"phMGJzalpoU0NZbHhIQkdFanBTbGpQNnM0ZWg1TDMzUDZWUW5EdEl3ZXVQWk5LVVB" +
		"NbndQajNsUm1tOTEvOU1mDQo3NTdQTUpVNHkvSHBMODBQTFVHeS9kNDNJOTZYNlU4" +
		"VmVlTEFpU3NSREZsVzlUOWVUY0lFRzZleGc2c01FZVpyDQorK0wrSGFiYzZBdnZsb" +
		"GtLSy80WlNJcERMU29ibTdZSWo4QnJ2ZzVmQm5wYkJOeDlQTjQrTG1yVkJRN1I4dn" +
		"JODQpKRmtwYnZYZk4rMmJHdisrMmxvQWlzaGhPUnVnMTB5TTFnU1B0QmhNVXN4N29" +
		"uNC9NcUxTeTBGZE5wdkxqSnA1DQp0N0pVSEN6SUQ5dVBkelZORFpsVGhOMFlza1R4" +
		"OVhYM1R3cVFvQ2VDWHg0dnNjTjB5Y2lOYmRKa05taWxNSkhuDQo3K2UyVjlnbFRKb" +
		"zN4dEZyUUIrc2VRaGxOTUNvcDhWZEM4dGcxSURrazdHUFhMaHlqS2VqTmdSRU40Nm" +
		"8NCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0NCi0tLS0tQkVHSU4gQ0VSVElGSUN" +
		"BVEUtLS0tLQ0KTUlJRjFqQ0NBNzZnQXdJQkFnSUVXV2RVSWpBTkJna3Foa2lHOXcw" +
		"QkFRc0ZBREJqTVFzd0NRWURWUVFHRXdKSA0KUWpFZE1Cc0dBMVVFQ2hNVVQzQmxia" +
		"UJDWVc1cmFXNW5JRXhwYldsMFpXUXhFVEFQQmdOVkJBc1RDRlJsYzNRZw0KVUV0Sk" +
		"1TSXdJQVlEVlFRREV4bFBjR1Z1SUVKaGJtdHBibWNnVkdWemRDQlNiMjkwSUVOQk1" +
		"CNFhEVEUzTURjeA0KTXpFd016WXlNMW9YRFRNM01EY3hNekV4TURZeU0xb3dZekVM" +
		"TUFrR0ExVUVCaE1DUjBJeEhUQWJCZ05WQkFvVA0KRkU5d1pXNGdRbUZ1YTJsdVp5Q" +
		"k1hVzFwZEdWa01SRXdEd1lEVlFRTEV3aFVaWE4wSUZCTFNURWlNQ0FHQTFVRQ0KQX" +
		"hNWlQzQmxiaUJDWVc1cmFXNW5JRlJsYzNRZ1VtOXZkQ0JEUVRDQ0FpSXdEUVlKS29" +
		"aSWh2Y05BUUVCQlFBRA0KZ2dJUEFEQ0NBZ29DZ2dJQkFMWU9GQVU3ZGpDSFNqUUgz" +
		"cnRwdWNrS0FHNnhIZDhmaEpmRzBENnVJWmJLU3RRaQ0KY21OSld0MjcxMmNLbGlFa" +
		"HdUeFJOZjFOcTZpdUJZcHlKanB5dThUS0hLWDNrMG9kMzl0czJxWktLSWt3RmdMMw" +
		"0KMVVVRWRqU2I0MUpWaWpqYU1Fd0Z4b3dKZG9qOXRHR3VNQ2FhT1k4VFZTbEdqbEx" +
		"MK1Mya1p4c1RLekJmTnVPTQ0KVkM0SHoyNWFNeXhpamVsU3N5UlpINHFtMHVhZnU5" +
		"dkNXTS9WdmRHT21zbm9BSC9zL0x0R0NwQlVOVlB4WUxoLw0KQXh6M2FiRCthUTlyN" +
		"29WcHFPV0E5SGdSOHZuSUlYVDhDRUZUR2xRMGdXZmR2UFh2T3hHcnMwSFkyQXVxU3" +
		"BpMg0KbWgxcm9oazE0b2dRRk15ZldRbU0zdkQ5SlVQWmczWldwbDRhUlFkTFJicUF" +
		"IV3h4aS9oTDl2amVMTkhzZldMKw0KcStseDdHU05QZ3JOTTM1bEhWaklIWnArbG9W" +
		"R2xOSGR1SmpvSCtoc3YwdjdsaSttVzE3NFIvaElpcXpDa1FhLw0KTTQxOWcvZ3h4Y" +
		"0pMaXFhbGovaEFFVjBXZlIyUUVLelU1YnF5Uk9xN2UycFRJalJwT1p2WThuZlhWeU" +
		"lXZ1hWNg0KdFhKSDV5c2theFUrSGw5Wm9BNXhzaWpaZ0Q2aVVQeDlxRGFxQlVHd0Y" +
		"zK2svOWFMVXhKdFJ6MTJuN091VmVjWg0KWmRKVEtDMkhyU09pYmJCY2ZsUU9NNHlZ" +
		"b0MvY3BoRHRRSlNWcU1SdW12aVJoQms0SURMcVNlVDJ4WnBXdkZ3eQ0KQmZYVTlsZ" +
		"3dTeXdqR3N6U09pOWd4SFVNUnhTa0JpYkxFTWFxODBycS9TdzY5SXBFMEVsOVFFeU" +
		"hTUE0vQWdNQg0KQUFHamdaRXdnWTR3RGdZRFZSMFBBUUgvQkFRREFnRUdNQThHQTF" +
		"VZEV3RUIvd1FGTUFNQkFmOHdLd1lEVlIwUQ0KQkNRd0lvQVBNakF4TnpBM01UTXhN" +
		"RE0yTWpOYWdROHlNRE0zTURjeE16RXhNRFl5TTFvd0h3WURWUjBqQkJndw0KRm9BV" +
		"TBtSkQ4MjFMK1F1dldDd3RLNmlOTlkvYTJGa3dIUVlEVlIwT0JCWUVGTkppUS9OdF" +
		"Mva0xyMWdzTFN1bw0KalRXUDJ0aFpNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUNBUUJ" +
		"tOXNxUThXKzh1Z0FpVmNiMTN0eGJ5MzJEcTlHLw0KeHpNWUZYK1ltRTFhbS9IN3lQ" +
		"aWxRc0hjNEVMRVhHSkxVZi9GbWlKOUs0ajlhNVRwclJjR1RjRzF3aWlET3VGMQ0KY" +
		"3MrOUxDWkFvbkxORXhWQjBXbVBTbDJScElDdVRnbm95K1RLb2EvWm12M21XanFtWG" +
		"twUUlHcFA5ajhERGs1Zg0KRVpQYnNBcDk5dHpJVkRkSzRoK1kvU2R6N3A0TGZaWEt" +
		"PTmtsNkRua1ZDeUlVWWNIVURtOEhFZ09UWU5wZHhrRw0KclFoTWtHdkJvdVZHSmNa" +
		"ek5WWTJQandZa3ZtS0xJeEhDcllKMzJiVmJqV1dWdUhWMmg4b2RRd0lnVStiNmU2a" +
		"g0KejErUTNZVUl1RmRHUERKN1FPaW01dVdheDhUZENVMnhEb3ZvYW5HKzhCYUFjeV" +
		"puREt5bkl0Y3crRks1TjBHbg0KeGUvL1BrTlZ6eWt6SGhIOHltVFphTkVscVZwQyt" +
		"Ud05IS3E1eUZ0ZnF6WjFnZ0J1cDhoM0xYNjVmMWd5NjFxLw0KN0xCY2NWUTJySWY3" +
		"TnhMeFIydUdTNjFqeWgrK0czSTF4bzRXSUhDMlpFTTROSjNtQzBtb2RiUDNiZlpLT" +
		"3pMTg0KVDErd2h1eTZ5Ylowa0hpenhyTzA2UXFMZHp1ZzIwcWQ5TEIrTmN0bnlIS2" +
		"03K0pBMVo3Tkc5ajdGKzltTklpUA0KcE1NSkJFbzJKNWd5SGdCREpBbDVMS0k2YjZ" +
		"FamRsNXNxRHB5NFFkMzdkdTdWeWRZYkdncDdjY0h4R0MrMFVYVQ0KODZuSmt4VDBp" +
		"V1VtdE9Ld1c1UkNGQitzMy81TFZRRHZTTjhyTGovNTEvNkRoZ0J6OGtXaFlvUXlUa" +
		"E9helM3RQ0KeGJGQlZkcUFrZ0E3dmc9PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ0K";

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new ValidateMTLSCertificatesAsX509();

		cond.setProperties("UNIT-TEST", eventLog, ConditionResult.INFO);

	}

	/**
	 * Test method for {@link ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(Environment)}.
	 */
	@Test
	public void testEvaluate_noError() {

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"" + key + "\","
			+ "\"ca\":\"" + caString + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);

		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "cert");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "key");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "ca");

		//assertThat(env.getString("mutual_tls_authentication", "cert")).isEqualTo(cert);
		//assertThat(env.getString("mutual_tls_authentication", "key")).isEqualTo(key);
	}

	@Test
	public void testEvaluate_noError1() {
		cert = "MIIFqzCCBJOgAwIBAgIEWWwGkjANBgkqhkiG9w0BAQsFADBmMQswCQYDVQQGEwJHQj" +
			"EdMBsGA1UEChMUT3BlbiBCYW5raW5nIExpbWl0ZWQxETAPBgNVBAsTCFRlc3QgUE" +
			"tJMSUwIwYDVQQDExxPcGVuIEJhbmtpbmcgVGVzdCBJc3N1aW5nIENBMB4XDTE3MT" +
			"IyMTEyMzgzOVoXDTE4MTIyMTEzMDgzOVowajELMAkGA1UEBhMCR0IxHTAbBgNVBA" +
			"oTFE9wZW4gQmFua2luZyBMaW1pdGVkMRswGQYDVQQLExJOakVENTNtcWxiY3JySF" +
			"I5cW0xHzAdBgNVBAMTFjZpcnFzVVJialJ5amZWejFVbE96NXYwggEiMA0GCSqGSI" +
			"b3DQEBAQUAA4IBDwAwggEKAoIBAQDqzxv7kIg+iFISyl1d4U2ny03vHCGcgEJ8d8" +
			"v/N1CapyVTMpURXntn62AjuuDQXBlchxDZRcj0cCJp6huMJPyOtyiCnH8/eMM6AQ" +
			"9HUbVbjJYCK4ZEdJ7v7KzioeNogHok1o0JOKn44TQMvf2ABCmv6a6op2vZqMCXuN" +
			"7fz7GQkiz6Yu540WneWI1/YX35RZZL4fPWb4UNOfLCkfDj2iizKxwvPC/ixMBHQm" +
			"EQrUM3QemwXU7tfijHljZ+FCK2Flz/aYrpQQVE1EcnoNOPFQt4nX5CSIt8mxlxOL" +
			"izy/IBCdTE4/utjuc5eykfxTHCx6ShkPK1gx4AI/6+8OBO10nVAgMBAAGjggJbMI" +
			"ICVzAOBgNVHQ8BAf8EBAMCB4AwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQ" +
			"UFBwMCMIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHCMCoGCCsGAQUFBw" +
			"IBFh5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMGCCsGAQUFBwICMI" +
			"GGDIGDVXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0ZXMgYWNjZXB0YW" +
			"5jZSBvZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZpY2F0aW9uIFBvbG" +
			"ljaWVzIGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1lbnQwOgYIKwYBBQ" +
			"UHAQEELjAsMCoGCCsGAQUFBzABhh5odHRwOi8vb2J0ZXN0LnRydXN0aXMuY29tL2" +
			"9jc3AwgcMGA1UdHwSBuzCBuDA3oDWgM4YxaHR0cDovL29idGVzdC50cnVzdGlzLm" +
			"NvbS9wa2kvb2J0ZXN0aXNzdWluZ2NhLmNybDB9oHugeaR3MHUxCzAJBgNVBAYTAk" +
			"dCMR0wGwYDVQQKExRPcGVuIEJhbmtpbmcgTGltaXRlZDERMA8GA1UECxMIVGVzdC" +
			"BQS0kxJTAjBgNVBAMTHE9wZW4gQmFua2luZyBUZXN0IElzc3VpbmcgQ0ExDTALBg" +
			"NVBAMTBENSTDgwHwYDVR0jBBgwFoAUDwHAL+hobPcjv45lbokNxqaFd7cwHQYDVR" +
			"0OBBYEFGlsj6ID6uFtflXurQ6GYidXtKhTMA0GCSqGSIb3DQEBCwUAA4IBAQB4OZ" +
			"j9HF3eFZU0AfA+eBrkIHa8YKr0dMdmN6yYHXpx4/LjedDktLDc8thR+ynLjwOggG" +
			"wPKHH3LtBJ7o+bIsb6DBPozDx3Ecvjp9djHFdeRtPf90UKxrXkF+GyXVXwunVGFI" +
			"rS1dBXMy0WiW6/vYrEo54jTTAEIoAJBhE8Fdanjks8hwtymNpFuP0QjRY1NaCVhP" +
			"kOcHpNCgnDESrVBYoRL/05Q71FfeQR4iKE9HciDe2LIWmS2HTDZ1RtSTGmbO0yfH" +
			"SpsM553siRwl6yaKm9F+i0j58uCC5el93ctV0BrmWvhQRmgca6ZW9zthvTzlzqBS" +
			"jigEDgh5qLOtZunOMP";

		key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDqzxv7kIg+iFISyl1d4U2n" +
			"y03vHCGcgEJ8d8v/N1CapyVTMpURXntn62AjuuDQXBlchxDZRcj0cCJp6huMJPyO" +
			"tyiCnH8/eMM6AQ9HUbVbjJYCK4ZEdJ7v7KzioeNogHok1o0JOKn44TQMvf2ABCmv" +
			"6a6op2vZqMCXuN7fz7GQkiz6Yu540WneWI1/YX35RZZL4fPWb4UNOfLCkfDj2iiz" +
			"KxwvPC/ixMBHQmEQrUM3QemwXU7tfijHljZ+FCK2Flz/aYrpQQVE1EcnoNOPFQt4" +
			"nX5CSIt8mxlxOLizy/IBCdTE4/utjuc5eykfxTHCx6ShkPK1gx4AI/6+8OBO10nV" +
			"AgMBAAECggEAOtg8gwbtnf4700mzrFtSQsLIfSvnoHjkMJ01GniaUqegywDaKsgq" +
			"1Rg/t7SVsqHiGTYgGtNKUTfo5/IrhSufv4RXOqRNn3J8OoUZdx4a/R52WYTe2TkA" +
			"u8/sY79+ZioMcj7yiWUR87U8fhT++p4QdG1zwAB1Hs8TtPI3qI88SKZegtxOC63u" +
			"VOSb1vamRPbFGraGlrbBm45n7n3uCuhMdBeLe0b8Ax6NQ+CIpWahwe8m5n5VJQFT" +
			"lE14T2lWmpnvTnkibWe4aXxFlGGchnN1TX25nu2WayDEoSPcA2gXhQD93bC3wOOK" +
			"MwoHuG6Dh27sRrm17/cjYGSY7Y4FYMUR4QKBgQD7NRXrwzUx8IanrsMV/t7eVz99" +
			"cWkdtP3dbIF41MIFwsvM15rkw5TUb+WB+ZrIqSCg3K7cbuakFF0LYw+GeHspwNjd" +
			"Z2nTn1JpJF7S6MVqKam4nZBbW37DHVMuJXKSlIxi6iPx49CA5ti6fUpHBi0Zz8JX" +
			"JZolTKiX94kIhc9WJwKBgQDvSe7Z7yhYTknX6wPzlLIgjVSB6N6e7D+FPIj6mifb" +
			"q1p0FWDWP9FFdaLBUfCTHISnYgCQnmm++EnOQYXzV2jrCg+6f5AG221uhlNSMxe1" +
			"PrHSah/54Cgv5ElXkSczE9LRBIuUzG+LQTxjX6u8Ycl+otQFhvpAiHZkoCSWFu15" +
			"owKBgBsrMqO9CRPNn8Ki9BZ86j3B0ewPR/8ehBZeleIroJBe8QvGhcoYHRCcyYW1" +
			"KThqkkzTkVXl6Kv2R9njbNpuTWPGp63KcXeh05frhxXodvF0cBa3c9Vtn9gaY9Sp" +
			"2CpiRoysJhcTPIm0bdw9kLr9wAL6pVonhvRhxhope3iggDwhAoGAeUGj0bgJX8Y8" +
			"UTf8hqBhK3Gy0ynoNexNu/yTBTq82+oXKh/zNF6ec25LDV+yYzneVtuooaBEwcsb" +
			"y3MUp90xg3lTwxQFLhRffdR/wHW3m9arUY8JqRvYAXzTVZZuoMl42QZOnRaDp7Nl" +
			"II5IfmunKY7lle9yPOVp6U/lelEgAbUCgYEA9gqE88DoMnJK2CmwrqcUDTTZ6cJt" +
			"0qqSUd9FfWel6QDvzB1DofCjemmF6OSjcxPXVZI7rz5RR7cZdBU84PZr1VP2no39" +
			"XJPuCbkvA1VF/tOjbkKLiuhR/63MLxThj6kzSJGHJgpG9h72+Bsenk3NXsTNQlMY" +
			"T95KA5yt48kKQhI=";

		// issuing first, then root
		caString = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlGYVRDQ0ExR2dBd0lCQWdJ" +
			"RVdXZFUwekFOQmdrcWhraUc5dzBCQVFzRkFEQmpNUXN3Q1FZRFZRUUdFd0pIDQpR" +
			"akVkTUJzR0ExVUVDaE1VVDNCbGJpQkNZVzVyYVc1bklFeHBiV2wwWldReEVUQVBC" +
			"Z05WQkFzVENGUmxjM1FnDQpVRXRKTVNJd0lBWURWUVFERXhsUGNHVnVJRUpoYm10" +
			"cGJtY2dWR1Z6ZENCU2IyOTBJRU5CTUI0WERURTNNRGN4DQpOakl3TlRZek9Wb1hE" +
			"VEkzTURjeE5qSXhNall6T1Zvd1pqRUxNQWtHQTFVRUJoTUNSMEl4SFRBYkJnTlZC" +
			"QW9UDQpGRTl3Wlc0Z1FtRnVhMmx1WnlCTWFXMXBkR1ZrTVJFd0R3WURWUVFMRXdo" +
			"VVpYTjBJRkJMU1RFbE1DTUdBMVVFDQpBeE1jVDNCbGJpQkNZVzVyYVc1bklGUmxj" +
			"M1FnU1hOemRXbHVaeUJEUVRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCDQpCUUFEZ2dF" +
			"UEFEQ0NBUW9DZ2dFQkFLUnFua05RU2VBRHhJVjU4U05DLzNCOUEzR3RnZ09vTkll" +
			"OHRiZ2dWSjlTDQpwR3Q0WVI3dG1uK2lqL3U2dEtWYXVjNFphMmQ4UkR3OWFvaDY2" +
			"cjBlbUVNbGNTeEZNN1BsQXNRZVFYTGUyL1E5DQpGQmxSWm9qQWN1NHB5bno4cERq" +
			"RWJjSzk0ZGNVNjFjaGN0VmYwNGlkc0J0UWxSSnFoc1dkT3pIb0J1ZVZoOUpRDQp1" +
			"RG9tNVcwKzQrdVNDM1lxdXVpa1F4RlJPWW1PQXozcVpSUVhHNm5Yc3cyYXJkTVVS" +
			"ak9sZkhCTXUvbDJ6SkdGDQowRy9oVEZXdkZHTUI3ZzdKVXRHZlpmTG5nUTFaL01v" +
			"bFc2bzNjdDhOeXhBSjY3NXdzT0xlUDBMaUZGTHM2VEc0DQpvWFV4U21sdWtEcEw3" +
			"bWdjTy9iWFdOb0d5TUppWGVKTXZNUDBwVmhscFpVQ0F3RUFBYU9DQVNBd2dnRWNN" +
			"QTRHDQpBMVVkRHdFQi93UUVBd0lCQmpBU0JnTlZIUk1CQWY4RUNEQUdBUUgvQWdF" +
			"QU1JRzFCZ05WSFI4RWdhMHdnYW93DQpMS0Fxb0NpR0ptaDBkSEE2THk5dllpNTBj" +
			"blZ6ZEdsekxtTnZiUzl2WW5SbGMzUnliMjkwWTJFdVkzSnNNSHFnDQplS0IycEhR" +
			"d2NqRUxNQWtHQTFVRUJoTUNSMEl4SFRBYkJnTlZCQW9URkU5d1pXNGdRbUZ1YTJs" +
			"dVp5Qk1hVzFwDQpkR1ZrTVJFd0R3WURWUVFMRXdoVVpYTjBJRkJMU1RFaU1DQUdB" +
			"MVVFQXhNWlQzQmxiaUJDWVc1cmFXNW5JRlJsDQpjM1FnVW05dmRDQkRRVEVOTUFz" +
			"R0ExVUVBeE1FUTFKTU1UQWZCZ05WSFNNRUdEQVdnQlRTWWtQemJVdjVDNjlZDQpM" +
			"QzBycUkwMWo5cllXVEFkQmdOVkhRNEVGZ1FVRHdIQUwraG9iUGNqdjQ1bGJva054" +
			"cWFGZDdjd0RRWUpLb1pJDQpodmNOQVFFTEJRQURnZ0lCQUR6czBuU2UweXFnb3h6" +
			"cDQvVkM2L0hrSFVqMDJNalVCNWN3cU9tanIyMjJYQlpEDQpHYTlBRXpnZ3N6YWxo" +
			"T0tHUHIrazZqWWtyRkl2L2hjNHFCU0ZhUXl0aStuaWhKV3pISnVsUHM5Ukp5bm5B" +
			"cmczDQpHdFhKUGtCdEFIOWJJOTRFYWpTVENXcVdSTlUxTjVJWS9mTVJSK1N6WVBC" +
			"VzBMN3FYRzd5bCtCZ29NUDdiMElTDQpUamVCbmgvMnVZdFdNWWNWR1VnZ2lkMWgv" +
			"MEZodThvRGNhdU1OeWkrR3dYNWJhNEVGdGhXWE5MR0Y3NTRTaDlwDQpCM1c2UGJR" +
			"NjA2YXRQclExVHQvTVBDNFBVczl1R0E3cy9YdWRxd3NSSUo3SEU5dExNeFh3aXZS" +
			"bk5xd1JjNkYrDQphMGJzalpoU0NZbHhIQkdFanBTbGpQNnM0ZWg1TDMzUDZWUW5E" +
			"dEl3ZXVQWk5LVVBNbndQajNsUm1tOTEvOU1mDQo3NTdQTUpVNHkvSHBMODBQTFVH" +
			"eS9kNDNJOTZYNlU4VmVlTEFpU3NSREZsVzlUOWVUY0lFRzZleGc2c01FZVpyDQor" +
			"K0wrSGFiYzZBdnZsbGtLSy80WlNJcERMU29ibTdZSWo4QnJ2ZzVmQm5wYkJOeDlQ" +
			"TjQrTG1yVkJRN1I4dnJODQpKRmtwYnZYZk4rMmJHdisrMmxvQWlzaGhPUnVnMTB5" +
			"TTFnU1B0QmhNVXN4N29uNC9NcUxTeTBGZE5wdkxqSnA1DQp0N0pVSEN6SUQ5dVBk" +
			"elZORFpsVGhOMFlza1R4OVhYM1R3cVFvQ2VDWHg0dnNjTjB5Y2lOYmRKa05taWxN" +
			"SkhuDQo3K2UyVjlnbFRKbzN4dEZyUUIrc2VRaGxOTUNvcDhWZEM4dGcxSURrazdH" +
			"UFhMaHlqS2VqTmdSRU40Nm8NCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0NCi0t" +
			"LS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQ0KTUlJRjFqQ0NBNzZnQXdJQkFnSUVX" +
			"V2RVSWpBTkJna3Foa2lHOXcwQkFRc0ZBREJqTVFzd0NRWURWUVFHRXdKSA0KUWpF" +
			"ZE1Cc0dBMVVFQ2hNVVQzQmxiaUJDWVc1cmFXNW5JRXhwYldsMFpXUXhFVEFQQmdO" +
			"VkJBc1RDRlJsYzNRZw0KVUV0Sk1TSXdJQVlEVlFRREV4bFBjR1Z1SUVKaGJtdHBi" +
			"bWNnVkdWemRDQlNiMjkwSUVOQk1CNFhEVEUzTURjeA0KTXpFd016WXlNMW9YRFRN" +
			"M01EY3hNekV4TURZeU0xb3dZekVMTUFrR0ExVUVCaE1DUjBJeEhUQWJCZ05WQkFv" +
			"VA0KRkU5d1pXNGdRbUZ1YTJsdVp5Qk1hVzFwZEdWa01SRXdEd1lEVlFRTEV3aFVa" +
			"WE4wSUZCTFNURWlNQ0FHQTFVRQ0KQXhNWlQzQmxiaUJDWVc1cmFXNW5JRlJsYzNR" +
			"Z1VtOXZkQ0JEUVRDQ0FpSXdEUVlKS29aSWh2Y05BUUVCQlFBRA0KZ2dJUEFEQ0NB" +
			"Z29DZ2dJQkFMWU9GQVU3ZGpDSFNqUUgzcnRwdWNrS0FHNnhIZDhmaEpmRzBENnVJ" +
			"WmJLU3RRaQ0KY21OSld0MjcxMmNLbGlFaHdUeFJOZjFOcTZpdUJZcHlKanB5dThU" +
			"S0hLWDNrMG9kMzl0czJxWktLSWt3RmdMMw0KMVVVRWRqU2I0MUpWaWpqYU1Fd0Z4" +
			"b3dKZG9qOXRHR3VNQ2FhT1k4VFZTbEdqbExMK1Mya1p4c1RLekJmTnVPTQ0KVkM0" +
			"SHoyNWFNeXhpamVsU3N5UlpINHFtMHVhZnU5dkNXTS9WdmRHT21zbm9BSC9zL0x0" +
			"R0NwQlVOVlB4WUxoLw0KQXh6M2FiRCthUTlyN29WcHFPV0E5SGdSOHZuSUlYVDhD" +
			"RUZUR2xRMGdXZmR2UFh2T3hHcnMwSFkyQXVxU3BpMg0KbWgxcm9oazE0b2dRRk15" +
			"ZldRbU0zdkQ5SlVQWmczWldwbDRhUlFkTFJicUFIV3h4aS9oTDl2amVMTkhzZldM" +
			"Kw0KcStseDdHU05QZ3JOTTM1bEhWaklIWnArbG9WR2xOSGR1SmpvSCtoc3Ywdjds" +
			"aSttVzE3NFIvaElpcXpDa1FhLw0KTTQxOWcvZ3h4Y0pMaXFhbGovaEFFVjBXZlIy" +
			"UUVLelU1YnF5Uk9xN2UycFRJalJwT1p2WThuZlhWeUlXZ1hWNg0KdFhKSDV5c2th" +
			"eFUrSGw5Wm9BNXhzaWpaZ0Q2aVVQeDlxRGFxQlVHd0YzK2svOWFMVXhKdFJ6MTJu" +
			"N091VmVjWg0KWmRKVEtDMkhyU09pYmJCY2ZsUU9NNHlZb0MvY3BoRHRRSlNWcU1S" +
			"dW12aVJoQms0SURMcVNlVDJ4WnBXdkZ3eQ0KQmZYVTlsZ3dTeXdqR3N6U09pOWd4" +
			"SFVNUnhTa0JpYkxFTWFxODBycS9TdzY5SXBFMEVsOVFFeUhTUE0vQWdNQg0KQUFH" +
			"amdaRXdnWTR3RGdZRFZSMFBBUUgvQkFRREFnRUdNQThHQTFVZEV3RUIvd1FGTUFN" +
			"QkFmOHdLd1lEVlIwUQ0KQkNRd0lvQVBNakF4TnpBM01UTXhNRE0yTWpOYWdROHlN" +
			"RE0zTURjeE16RXhNRFl5TTFvd0h3WURWUjBqQkJndw0KRm9BVTBtSkQ4MjFMK1F1" +
			"dldDd3RLNmlOTlkvYTJGa3dIUVlEVlIwT0JCWUVGTkppUS9OdFMva0xyMWdzTFN1" +
			"bw0KalRXUDJ0aFpNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUNBUUJtOXNxUThXKzh1" +
			"Z0FpVmNiMTN0eGJ5MzJEcTlHLw0KeHpNWUZYK1ltRTFhbS9IN3lQaWxRc0hjNEVM" +
			"RVhHSkxVZi9GbWlKOUs0ajlhNVRwclJjR1RjRzF3aWlET3VGMQ0KY3MrOUxDWkFv" +
			"bkxORXhWQjBXbVBTbDJScElDdVRnbm95K1RLb2EvWm12M21XanFtWGtwUUlHcFA5" +
			"ajhERGs1Zg0KRVpQYnNBcDk5dHpJVkRkSzRoK1kvU2R6N3A0TGZaWEtPTmtsNkRu" +
			"a1ZDeUlVWWNIVURtOEhFZ09UWU5wZHhrRw0KclFoTWtHdkJvdVZHSmNaek5WWTJQ" +
			"andZa3ZtS0xJeEhDcllKMzJiVmJqV1dWdUhWMmg4b2RRd0lnVStiNmU2ag0KejEr" +
			"UTNZVUl1RmRHUERKN1FPaW01dVdheDhUZENVMnhEb3ZvYW5HKzhCYUFjeVpuREt5" +
			"bkl0Y3crRks1TjBHbg0KeGUvL1BrTlZ6eWt6SGhIOHltVFphTkVscVZwQytUd05I" +
			"S3E1eUZ0ZnF6WjFnZ0J1cDhoM0xYNjVmMWd5NjFxLw0KN0xCY2NWUTJySWY3TnhM" +
			"eFIydUdTNjFqeWgrK0czSTF4bzRXSUhDMlpFTTROSjNtQzBtb2RiUDNiZlpLT3pM" +
			"Tg0KVDErd2h1eTZ5Ylowa0hpenhyTzA2UXFMZHp1ZzIwcWQ5TEIrTmN0bnlIS203" +
			"K0pBMVo3Tkc5ajdGKzltTklpUA0KcE1NSkJFbzJKNWd5SGdCREpBbDVMS0k2YjZF" +
			"amRsNXNxRHB5NFFkMzdkdTdWeWRZYkdncDdjY0h4R0MrMFVYVQ0KODZuSmt4VDBp" +
			"V1VtdE9Ld1c1UkNGQitzMy81TFZRRHZTTjhyTGovNTEvNkRoZ0J6OGtXaFlvUXlU" +
			"aE9helM3RQ0KeGJGQlZkcUFrZ0E3dmc9PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ0K";

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"" + key + "\","
			+ "\"ca\":\"" + caString + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);

		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "cert");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "key");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "ca");
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_noKey() {

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"" + cert + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_badKey() {

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"bad key value\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_noCert() {

		JsonObject config = new JsonParser().parse("{"
			+ "\"key\":\"" + key + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_badCert() {

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"bad cert value\","
			+ "\"key\":\"" + key + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);
	}

	@Test
	public void testEvaluate_wrongOrderCaAndAutoCorrectIt() {

		cert = "MIIDkjCCAnqgAwIBAgIUGkjiqITUg70nf+y2wVWEjsgBm5IwDQYJKoZIhvcNAQE" +
			"LBQAwezELMAkGA1UEBhMCVUsxDTALBgNVBAgTBEF2b24xEDAOBgNVBAcTB0JyaXN" +
			"0b2wxEjAQBgNVBAoTCUZvcmdlUm9jazEcMBoGA1UECxMTZm9yZ2Vyb2NrLmZpbmF" +
			"uY2lhbDEZMBcGA1UEAxMQb2JyaS1leHRlcm5hbC1jYTAgFw0xNzA4MjEyMDIxMDJ" +
			"aGA8yMTE4MDcyODIwMjEwMlowgYgxCzAJBgNVBAYTAlVLMQ0wCwYDVQQIEwRBdm9" +
			"uMRAwDgYDVQQHEwdCcmlzdG9sMRIwEAYDVQQKEwlGb3JnZVJvY2sxITAfBgNVBAs" +
			"TGDViNGIxMTA0YjA5MzQ2NmViNmU0YWU5MzEhMB8GA1UEAxMYNWI0YjExMDhiMDk" +
			"zNDY2ZWI2ZTRhZTk0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtHf" +
			"fvYlB0jxOrUcaom9q+n1o4qG8qVqh8JZYRXYLsO/ElVLA9P+87xd2Qcwx1nSjRRF" +
			"MeQZ448L9+abVdFy+gqTVGMEjQmhJRq90kVGiCTHENwGZHraEbwAJ+VA4PmtXITi" +
			"P98tsuEU7NQH/wEeP/0C5hdq6uQVB3q6jn4NwRegZaY/9xCn3aI2zFqlP8OyIboC" +
			"29nzbB/DbN4UpHJPn0B7lmKGmtzpWXZmah5lhhaLueqZh3+f+mrwVyLmBPfkSRo8" +
			"Fn20xgSl0/ev0YZpfJg7/muXsH1NfPa/WFicaK8QKg7PR/nwLfw96BxxNIw3uB7E" +
			"s6u8d2hFd+bOaHcbQdwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQAQ9UOTKPo2r33" +
			"24c9SLj9C2EeUBeUxwauc1TNCTtUgOEf0ASs3LJQPW3d/dzTwDyo1HWQsT7bui2x" +
			"CMmyT8H/e/6Qv+JQ8WMuca8CnD4V48WycAKF5rgJUFCurWmJokWGWu53QAQw+K13" +
			"HqB1gScdFBajH3O8sssuQQGK1J98fkZ2vPcWLW2P8zxJgd69lHYBmxmQgmITboxB" +
			"gogwHAIonuvWPnf6s6PoZXLNUFrcb3El8LiOfmJ0SatQIZRp7wYqUxvkyGCtWSb3" +
			"DNU7Jazru/+cY8YAyEJlK5XHORA7jugev+y4vLNUErYc7cvQ54/rqftd58Qw76t3ccflkGCil";

		key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0d9+9iUHSPE6t" +
			"Rxqib2r6fWjiobypWqHwllhFdguw78SVUsD0/7zvF3ZBzDHWdKNFEUx5Bnjjwv35" +
			"ptV0XL6CpNUYwSNCaElGr3SRUaIJMcQ3AZketoRvAAn5UDg+a1chOI/3y2y4RTs1" +
			"Af/AR4//QLmF2rq5BUHerqOfg3BF6Blpj/3EKfdojbMWqU/w7IhugLb2fNsH8Ns3" +
			"hSkck+fQHuWYoaa3OlZdmZqHmWGFou56pmHf5/6avBXIuYE9+RJGjwWfbTGBKXT9" +
			"6/Rhml8mDv+a5ewfU189r9YWJxorxAqDs9H+fAt/D3oHHE0jDe4HsSzq7x3aEV35" +
			"s5odxtB3AgMBAAECggEAUif8UGHK+DWCL7od8vK8BdrPw9STeZmL/WXEO6L4wYz1" +
			"tnDc7Ov7i/DnYh7eVV318fxlDveWU0Gys1Ny+y05gWoepM1QCuxE54v6iilalcr1" +
			"I4FzvpqigZHH+Jq/cBMXsET6rXkLPnwt7vzA/DzX3gEONFSWCqLNEN36GSUSGJBC" +
			"UNyjvAAh8L32CQRUBe0Mh8rhMi/Y5l3zzY2Zcc1rcJV+A6jdcJjUyYO6/wu47UH9" +
			"LGchXTGzAA8U2jBzldqKUs4BvNHCqHQ/TURgahlrX/i0rFx2NEBcww1gYGTkCx0W" +
			"SBtcYiU3LiJsSv4Csf/TSPB29oJ/K9PwaSjzia2TYQKBgQD3jV9BtcbkXqKCYG7b" +
			"+6zO82z231232vNfP+YNscY2asymvx5HptDv89Q/ZhHOhrRrStlkwKpj7Qrz2ODi" +
			"WDv5o0s4ho7nF0kaJg6xS4Z75Moc24JpxkEFJh218rvI0+ts799Kd/Xea1oCD0qe" +
			"BtSlmeRujI34l5yfPSTudVXWsQKBgQC6oHQANLEOAMgDFK39SYJSmgPXl4DKqD37" +
			"i/mxFXioaXUpweaNg95zyYiiw1xYgtlzvzru28EXLOf2f/ziSUe/WIjCGTCrEBLL" +
			"FKbJepvKZNFIia6Z4NG/wCa1+TWVLkW7Dvjur0+RGxMJHNdo8El40QbyOJQMR2XA" +
			"ufRNBsqzpwKBgEMWpCcpvVIst3v/6GtjxIx9eh1ZoYtvIh/BMHarpB0hSn8yMmGW" +
			"uu8ctHvODKtc5E5jhZEBiev01NALCb8tzGs2Lu+Nv/Ku49fbUoYIAtVWxtOVRb2m" +
			"xf5AOzMRKoUdsaLfSzNiWVbC3yzJuh4jmcObdkXjf6JMbFzXdEfPssixAoGAY1Wy" +
			"SMXDFAQAcftY3L96uYK21tMSP0wrqa+ImOQn0+RF85L8p4hwKEs2CgRYK9iB6+T2" +
			"dlrLvQ0u7nqTXNLKeKOkL0P/Lp4gkq2MVSOItsLQzn5STB9pJVoegK+EAMKfZQCE" +
			"KW9wt3ikui36ziVPxhnkS5sn2h1KoLmZukIKNHMCgYEA3NDDxM3nd9Hg1QK7EUns" +
			"sCscj0E+zy7sNRA6piynB/qZk61js74weXKZ1K9vfRCbBqfHw7TTvo0mxGxczYeD" +
			"fE3JPFf8Lif3qvKOk3yh01dFjhlNv/bgtrMTrO+cB1BxrpVdnHm/QIRKUpbvcpbbKB8Ju0LUXouckvnFyBQVjQg=";

		// root first, then issuing
		caString = "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" +
			"A4IBDwAwggEKAoIBAQCyyrRg2jF01jXhX3IR44p338ZBozn8WkZaCN8MB+AlBfuX" +
			"HD6mC/0v+N/Z4XI6E5pzArmTho8D6a6JDpAHmmefqGSqOXVbclYv1tHFjmC1FtKq" +
			"kFHTTMyhl41nEMo0dnvWA45bMsGm0yMi/tEM5Vb5dSY4Zr/2LWgUTDFUisgUbyII" +
			"HT+L6qxPUPCpNuEd+AWVc9K0SlmhaC+UIfVO83gE1+9ar2dONSFaK/a445Us6Mnq" +
			"gKvfkvKdaR06Ok/EhGgiAZORcyZ61EYFVVzJewy5NrFSF3mwiPYvMxoT5bxcwAEv" +
			"xqBXpTDv8njQfR+cgZDeloeK1UqmW/DpR+jj3KNHAgMBAAGjggHwMIIB7DAOBgNV" +
			"HQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADCB4AYDVR0gBIHYMIHVMIHS" +
			"BgsrBgEEAah1gQYBZDCBwjAqBggrBgEFBQcCARYeaHR0cDovL29iLnRydXN0aXMu" +
			"Y29tL3BvbGljaWVzMIGTBggrBgEFBQcCAjCBhgyBg1VzZSBvZiB0aGlzIENlcnRp" +
			"ZmljYXRlIGNvbnN0aXR1dGVzIGFjY2VwdGFuY2Ugb2YgdGhlIE9wZW5CYW5raW5n" +
			"IFJvb3QgQ0EgQ2VydGlmaWNhdGlvbiBQb2xpY2llcyBhbmQgQ2VydGlmaWNhdGUg" +
			"UHJhY3RpY2UgU3RhdGVtZW50MGoGCCsGAQUFBwEBBF4wXDAyBggrBgEFBQcwAoYm" +
			"aHR0cDovL29iLnRydXN0aXMuY29tL29idGVzdHJvb3RjYS5jcnQwJgYIKwYBBQUH" +
			"MAGGGmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vY3NwMDcGA1UdHwQwMC4wLKAqoCiG" +
			"Jmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9wcF9yb290Y2EuY3JsMB8GA1UdIwQY" +
			"MBaAFOw4jgva8/k3PpDefV9q5mDNeUKDMB0GA1UdDgQWBBRQc5HGIXLTd/T+ABIG" +
			"gVx5eW4/UDANBgkqhkiG9w0BAQsFAAOCAgEAdRg2H9uLwzlGqvHGjIz0ydM1tElu" +
			"jEcWJp5MeiorikK0rMOlxVU6ZFBlXPfO1APu0cZXxfHwWs91zoNCpGXebC6tiDFQ" +
			"3+mI4qywtippjBqb6Sft37NlkXDzQETomsY7wETuUJ31xFA0FccI8WlAUzUOBE8O" +
			"AGo5kAZ4FTa/nkd8c2wmuwSp+9/s+gQe0K9BkxywoP1WAEdUAaKW3RE9yuTbHA/Z" +
			"F/zz4/Rpw/FB/hYhOxvDV6qInl5B7ErSH4r4v4D2jiE6apAcn5LT+e0aBa/EgGAx" +
			"gyAgrYpw1s+TCUJot+227xRvXxeeZzXa2igsd+C845BGiSlthzr0mqYDYEWJMfAp" +
			"Z+BlMtxa7K9T3D2l6XMv12RoNnEWe6H5xazTvBLiTibW3c5ij8WWKJNtQbgmooRP" +
			"aKJIl+0rm54MFH0FDxJ+P4mAR6qa8JS911nS26iCsE9FQVK51djuct349FYBOVM5" +
			"95/GkkTz9k1vXw1BdD71lNjI00Yjf73AAtvL/X4CpRz92NagshS2Ia5a3qjjFrjx" +
			"7z4h7QtMJGjuUsjTI/c+yjIYwAZ5gelF5gz7l2dn3g6B40pu7y1EewlfIQh/HVMF" +
			"0ZpF29XL6+7siYQCGhP5cNJ04fotzqDPaT2XlOhE3yNkjp82uzCWvhLUJgE3D9V9PL0XD/ykNEP0Fio=";

		String expectCaString = getCorrectOrderCaString(caString);

		JsonObject config = new JsonParser().parse("{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"" + key + "\","
			+ "\"ca\":\"" + caString + "\""
			+ "}").getAsJsonObject();

		env.putObject("mutual_tls_authentication", config);

		cond.execute(env);

		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "cert");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "key");
		verify(env, atLeastOnce()).getString("mutual_tls_authentication", "ca");

		assertThat(env.getString("mutual_tls_authentication", "key")).isEqualTo(key);
		assertThat(env.getString("mutual_tls_authentication", "cert")).isEqualTo(cert);
		assertThat(env.getString("mutual_tls_authentication", "ca")).isEqualTo(expectCaString);
	}

	private String getCorrectOrderCaString(String ca) {
		byte[] caBytes = Base64.getDecoder().decode(ca);
		try {
			List<X509Certificate> chainList = generateCertificateChainFromDER(caBytes);
			X509Certificate rootCa = chainList.get(0);
			X509Certificate issuingCa = chainList.get(1);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(issuingCa.getEncoded(), 0, issuingCa.getEncoded().length);
			out.write(rootCa.getEncoded(), 0, rootCa.getEncoded().length);

			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (CertificateException e) {
			return "";
		}
	}

	private List<X509Certificate> generateCertificateChainFromDER(byte[] chainBytes) throws CertificateException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");

		ArrayList<X509Certificate> chain = new ArrayList<>();
		ByteArrayInputStream in = new ByteArrayInputStream(chainBytes);
		while (in.available() > 0) {
			chain.add((X509Certificate) factory.generateCertificate(in));
		}

		return chain;
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_valueMissing() {

		cond.execute(env);
	}

}
