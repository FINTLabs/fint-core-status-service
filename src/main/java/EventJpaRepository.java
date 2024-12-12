package no.fintlabs.event;

import jdk.jfr.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventJpaRepository extends JpaRepository<Event, Long> {
}
