package nicepay.paymentmodule.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nicepay.paymentmodule.payment.domain.CardInput;
import nicepay.paymentmodule.payment.domain.Payment;
import nicepay.paymentmodule.payment.domain.PaymentCard;
import nicepay.paymentmodule.payment.domain.PaymentInput;
import nicepay.paymentmodule.payment.service.PaymentService;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @RequestMapping(value = "/payment/cardregister", method =  RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public JSONObject card(@RequestBody CardInput cardInput) {
        JSONObject result = new JSONObject();
        log.info("request={}", cardInput);

        String card1 = cardInput.getCard1();
        String card2 = cardInput.getCard2();
        String card3 = cardInput.getCard3();
        String card4 = cardInput.getCard4();
        String year = cardInput.getYy();
        String month = cardInput.getMm();
        String birth = cardInput.getBirth();
        String password = cardInput.getPassword();

        String Moid = UUID.randomUUID().toString();
        String CardNo  = card1 + card2 + card3 + card4;
        String ExpYear = year;
        String ExpMonth = month;
        String IDNo = birth;
        String CardPw = password;
        String CardNum = card1 + card2.substring(0, 2) + "**" + "****" + card4;

        JSONObject param = new JSONObject();

        param.put("Moid", Moid);
        param.put("CardNo", CardNo);
        param.put("ExpYear", ExpYear);
        param.put("ExpMonth", ExpMonth);
        param.put("IDNo", IDNo);
        param.put("CardPw", CardPw);

        JSONObject response = paymentService.billkeyRegister(param);

        String resultCode= (String) response.get("resultCode");
        String resultMsg= (String) response.get("resultMsg");
        String TID= (String) response.get("TID");
        String BID= (String) response.get("BID");
        String authDate= (String) response.get("authDate");
        String cardName= (String) response.get("cardName");

        if (resultCode.equals("F100")) {
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setResultCode(resultCode);
            paymentCard.setResultMsg(resultMsg);
            paymentCard.setTID(TID);
            paymentCard.setBID(BID);
            paymentCard.setAuthDate(authDate);
            paymentCard.setCardName(cardName);
            paymentCard.setCardNo(CardNum);
            paymentService.insertCard(paymentCard);

        } else {
            log.info("카드 등록 에러코드={}", response.get("ResultCode"));
        }
        result.put("resultCode", resultCode);
        log.info("resultCode={}", result);
        return result;
    }

    @RequestMapping(value = "/payment/cardlist", method = RequestMethod.GET)
    public List<PaymentCard> cardList() {
        List<PaymentCard> result = paymentService.findCard();
        log.info("service reuslt={}", result);
        return result;
    }

    @RequestMapping(value = "/payment/charge", method = RequestMethod.POST)
    public JSONObject payment(@RequestBody PaymentInput paymentInput) {
        JSONObject result = new JSONObject();
        Long cardId = paymentInput.getId();
        String price = paymentInput.getResultAmt();

        JSONObject response = paymentService.billingApprove(cardId, price);
        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");
        String reusltTID = (String) response.get("reusltTID");
        String resultMoid = (String) response.get("resultMoid");
        String resultAmt = (String) response.get("resultAmt");
        String authCode = (String) response.get("authCode");
        String authDate = (String) response.get("authDate");
        String acquCardCode = (String) response.get("acquCardCode");
        String cardNo = (String) response.get("cardNo");
        String cardCode = (String) response.get("cardCode");
        String cardQuota = (String) response.get("cardQuota");
        String cardCl = (String) response.get("cardCl");
        String ccPartCl = (String) response.get("ccPartCl");

        if (resultCode.equals("3001")) {
            Payment payment = new Payment();
            payment.setResultCode(resultCode);
            payment.setResultMsg(resultMsg);
            payment.setResultTID(reusltTID);
            payment.setResultMoid(resultMoid);
            payment.setResultAmt(resultAmt);
            payment.setAuthCode(authCode);
            payment.setAuthDate(authDate);
            payment.setAcquCardCode(acquCardCode);
            payment.setCardNo(cardNo);
            payment.setCardCode(cardCode);
            payment.setCardQuota(cardQuota);
            payment.setCardCl(cardCl);
            payment.setCcPartCl(ccPartCl);
            paymentService.insertPayment(payment);
        }
        result.put("resultCode", resultCode);
        log.info("resultCode={}", result);
        return result;
    }
}
