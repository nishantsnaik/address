package com.enterprise.address.service;

import com.enterprise.address.exception.ResourceNotFoundException;
import com.enterprise.address.model.Address;
import com.enterprise.address.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository repository;

    public AddressService(AddressRepository repository) {
        this.repository = repository;
    }

    // Create a new address
    public Address createAddress(Address address) {
        return repository.save(address);
    }

    // Update an existing address
    public Address updateAddress(String id, Address address) {
        Address existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        existing.setUserId(address.getUserId());
        existing.setType(address.getType());
        existing.setLine1(address.getLine1());
        existing.setLine2(address.getLine2());
        existing.setCity(address.getCity());
        existing.setState(address.getState());
        existing.setCountry(address.getCountry());
        existing.setPostalCode(address.getPostalCode());

        return repository.save(existing);
    }

    // Delete an address
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // Get address by ID
    public Optional<Address> getById(String id) {
        return repository.findById(id);
    }

    // Get all addresses for a user
    public List<Address> getAddressesByUser(String userId) {
        return repository.findAll().stream()
                .filter(a -> a.getUserId().equals(userId))
                .toList();
    }
}