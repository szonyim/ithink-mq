package hu.ithink.mq.services;

import java.util.List;

import hu.ithink.mq.entities.ConnectionProfile;
import hu.ithink.mq.repositories.ConnectionProfileRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ConnectionProfileService {

  private final ConnectionProfileRepository connectionProfileRepository;

  public ConnectionProfileService(ConnectionProfileRepository connectionProfileRepository) {
    this.connectionProfileRepository = connectionProfileRepository;
  }

  public List<ConnectionProfile> findAll() {
    return connectionProfileRepository.findAll(Sort.by("name"));
  }

  public ConnectionProfile findById(Long id) {
    return connectionProfileRepository.findById(id).orElse(null);
  }

  public ConnectionProfile save(ConnectionProfile connectionProfile) {
    return connectionProfileRepository.save(connectionProfile);
  }

  public void deleteById(Long id) {
    connectionProfileRepository.deleteById(id);
  }
}
