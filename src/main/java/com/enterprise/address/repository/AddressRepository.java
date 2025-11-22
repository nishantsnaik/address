package com.enterprise.address.repository;

import com.enterprise.address.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByUserId(String userId);
}
