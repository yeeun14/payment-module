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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String resultCode;
    private String resultMsg;
    private String resultTID;
    private String resultMoid;
    private String resultAmt;
    private String authCode;
    private String authDate;
    private String acquCardCode;
    private String cardNo;
    private String cardCode;
    private String cardQuota;
    private String cardCl;
    private String ccPartCl;

}
