package nicepay.paymentmodule.payment.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity
//@AllArgsConstructor
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String resultCode;
    private String resultMsg;
    private String TID;
    private String BID;
    private String authDate;
    private String cardName;
    private String cardNo;

//    public PaymentCard() {
//        this.resultCode = resultCode;
//        this.resultMsg = resultMsg;
//        this.TID = TID;
//        this.BID = BID;
//        this.authDate = authDate;
//        this.cardName = cardName;
//        this.cardNo = cardNo;
//    }

}
