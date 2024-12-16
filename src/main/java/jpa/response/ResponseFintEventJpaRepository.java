package jpa.response;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseFintEventJpaRepository extends JpaRepository<ResponseFintEventEntity, Long> {
}