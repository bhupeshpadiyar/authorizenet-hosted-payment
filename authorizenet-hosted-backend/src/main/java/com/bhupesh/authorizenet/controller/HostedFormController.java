package com.bhupesh.authorizenet.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhupesh.authorizenet.model.PaymentAmount;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.authorize.Environment;
import net.authorize.api.contract.v1.ArrayOfSetting;
import net.authorize.api.contract.v1.GetHostedPaymentPageRequest;
import net.authorize.api.contract.v1.GetHostedPaymentPageResponse;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.SettingType;
import net.authorize.api.contract.v1.TransactionRequestType;
import net.authorize.api.contract.v1.TransactionTypeEnum;
import net.authorize.api.controller.GetHostedPaymentPageController;
import net.authorize.api.controller.base.ApiOperationBase;

@RestController
@RequestMapping("/api/payment")
public class HostedFormController {

	@Value("${authorizenet.loginid}")
	private String apiLoginId;

	@Value("${authorizenet.transactionkey}")
	private String apiTransactionKey;

	@PostMapping(value = "/token")
	public ResponseEntity<Object> generateAPIToken(@RequestBody PaymentAmount paymentAmount)
			throws JsonProcessingException {

		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		Double amount = paymentAmount.getAmount();

		ApiOperationBase.setEnvironment(Environment.SANDBOX);

		MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
		merchantAuthenticationType.setName(apiLoginId);
		merchantAuthenticationType.setTransactionKey(apiTransactionKey);
		ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

		// Create the payment transaction request
		TransactionRequestType txnRequest = new TransactionRequestType();
		txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
		txnRequest.setAmount(new BigDecimal(amount).setScale(2, RoundingMode.CEILING));

		SettingType setting = new SettingType();

		setting.setSettingName("hostedPaymentReturnOptions");
		setting.setSettingValue(
				"{\"showReceipt\": true, \"url\": \"https://bhupeshpadiyar.com/poc/authnet/success.html\", \"urlText\": \"Continue\", \"cancelUrl\": \"https://bhupeshpadiyar.com/poc/authnet/error.html\", \"cancelUrlText\": \"Cancel\"}");

		SettingType setting1 = new SettingType();

		setting1.setSettingName("hostedPaymentButtonOptions");
		setting1.setSettingValue("{\"text\": \"Pay\"}");

		SettingType setting2 = new SettingType();
		setting2.setSettingName("hostedPaymentOrderOptions");
		setting2.setSettingValue("{\"show\": false}");

		SettingType setting3 = new SettingType();
		setting3.setSettingName("hostedPaymentStyleOptions");
		setting3.setSettingValue("{\"bgColor\": \"blue\"}");

		SettingType setting4 = new SettingType();
		setting4.setSettingName("hostedPaymentPaymentOptions");
		setting4.setSettingValue("{\"cardCodeRequired\": false, \"showCreditCard\": true, \"showBankAccount\": true}");

		SettingType setting5 = new SettingType();
		setting5.setSettingName("hostedPaymentSecurityOptions");
		setting5.setSettingValue("{\"captcha\": true}");

		SettingType setting6 = new SettingType();
		setting6.setSettingName("hostedPaymentShippingAddressOptions");
		setting6.setSettingValue("{\"show\": true, \"required\": true}");

		SettingType setting7 = new SettingType();
		setting7.setSettingName("hostedPaymentBillingAddressOptions");
		setting7.setSettingValue("{\"show\": true, \"required\": true}");

		SettingType setting8 = new SettingType();
		setting8.setSettingName("hostedPaymentCustomerOptions");
		setting8.setSettingValue("{\"showEmail\": true, \"requiredEmail\": true, \"addPaymentProfile\": true}");

		SettingType setting9 = new SettingType();
		setting9.setSettingName("hostedPaymentOrderOptions");
		setting9.setSettingValue("{\"show\": true, \"merchantName\": \"BhupeshPadiyar.Com Inc.\"}");

		SettingType setting10 = new SettingType();
		setting10.setSettingName("hostedPaymentIFrameCommunicatorUrl");
		setting10.setSettingValue("{\"url\": \"https://bhupeshpadiyar.com/\"}");

		ArrayOfSetting alist = new ArrayOfSetting();
		alist.getSetting().add(setting);
		alist.getSetting().add(setting1);
		alist.getSetting().add(setting3);

		alist.getSetting().add(setting4);
		alist.getSetting().add(setting5);
		alist.getSetting().add(setting6);

		alist.getSetting().add(setting7);
		alist.getSetting().add(setting8);
		alist.getSetting().add(setting9);
		alist.getSetting().add(setting10);

		GetHostedPaymentPageRequest apiRequest = new GetHostedPaymentPageRequest();
		apiRequest.setTransactionRequest(txnRequest);
		apiRequest.setHostedPaymentSettings(alist);

		GetHostedPaymentPageController controller = new GetHostedPaymentPageController(apiRequest);
		controller.execute();

		GetHostedPaymentPageResponse response = new GetHostedPaymentPageResponse();
		response = controller.getApiResponse();

		if (response != null) {

			if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

				responseMap.put("status", 200);
				responseMap.put("token", response.getToken());
				responseMap.put("message", response.getMessages());

			} else {
				responseMap.put("status", 201);
				responseMap.put("message", response.getMessages());

			}
		}

		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}

}
