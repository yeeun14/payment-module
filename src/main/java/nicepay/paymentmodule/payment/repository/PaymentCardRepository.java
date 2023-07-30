package nicepay.paymentmodule.payment.repository;

import nicepay.paymentmodule.payment.domain.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, String> {

    Optional<PaymentCard> findById(Long id);

}
