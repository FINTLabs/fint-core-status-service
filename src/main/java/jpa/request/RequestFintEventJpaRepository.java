package request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestFintEventJpaRepository extends JpaRepository<RequestFintEventEntity, Long> {
}
