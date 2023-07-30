package nicepay.paymentmodule.payment.domain;

import lombok.Data;

@Data
public class CardInput {
    private String card1;
    private String card2;
    private String card3;
    private String card4;
    private String yy;
    private String mm;
    private String birth;
    private String password;
}
