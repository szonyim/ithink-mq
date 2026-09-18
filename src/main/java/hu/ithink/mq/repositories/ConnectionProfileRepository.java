package hu.ithink.mq.repositories;

import hu.ithink.mq.entities.ConnectionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionProfileRepository extends JpaRepository<ConnectionProfile, Long> {
}
