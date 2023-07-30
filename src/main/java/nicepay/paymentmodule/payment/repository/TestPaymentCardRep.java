package nicepay.paymentmodule.payment.repository;

import nicepay.paymentmodule.payment.domain.PaymentCard;

import java.util.List;
import java.util.Optional;

public interface TestPaymentCardRep {
    PaymentCard save(PaymentCard paymentCard);

    Optional<PaymentCard> findById(Long id);

    Optional<PaymentCard> findByTID(String name);

    List<PaymentCard> findAll();
}
