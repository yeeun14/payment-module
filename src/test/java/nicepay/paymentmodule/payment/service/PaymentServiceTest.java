package nicepay.paymentmodule.payment.service;

import nicepay.paymentmodule.payment.domain.Payment;
import nicepay.paymentmodule.payment.domain.PaymentCard;
import nicepay.paymentmodule.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;

    // 이거 쓰려면 PaymentCard 클래스에 @Builder, @AllArgsConstructor와 생성자 추가해야 함
//    @Test
//    public void cardSave() {
//        PaymentCard paymentCard = PaymentCard.builder()
//                .resultCode("F100")
//                .resultMsg("정상 등록")
//                .TID("nicetest04m0123")
//                .BID("nicetest04m0123")
//                .authDate("20230615")
//                .cardName("국민")
//                .cardNo("414011******1234")
//                .build();
//
//        Long save = paymentService.insertCard(paymentCard);
//        assertThat(paymentCard.getId()).isEqualTo(save);
//
//    }

    @Test
    public void find() {
        List<PaymentCard> result = paymentService.findCard();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void paymentSave() {
        String resultCode = "3001";
        String resultMsg = "결제 성공";
        String reusltTID = "nicetest04m123123123123";
        String resultMoid = "qwerqwe";
        String resultAmt = "1000";
        String authCode = "01";
        String authDate = "20230617";
        String acquCardCode = "04";
        String cardNo = "123412******1234";
        String cardCode = "01";
        String cardQuoto = "004";
        String cardCI = "0";
        String ccPartCI = "00";

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
        payment.setCardQuota(cardQuoto);
        payment.setCardCl(cardCI);
        payment.setCcPartCl(ccPartCI);
        paymentService.insertPayment(payment);
    }

}