package com.enterprise.address.service;

import com.enterprise.address.exception.ResourceNotFoundException;
import com.enterprise.address.model.Address;
import com.enterprise.address.repository.AddressRepository;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Redis CLI Commands to inspect caches:
 * 1. List all keys: KEYS *
 * 2. Check specific cache:
 *    - For 'address' cache: HGETALL address::<id>
 *    - For 'addresses' cache: LRANGE addresses 0 -1
 *    - For 'userAddresses' cache: HGETALL userAddresses::<userId>
 *    - For 'addressByPostalCode' cache: HGETALL addressByPostalCode::<postalCode>
 *    - For 'addressesByCity' cache: LRANGE addressesByCity::<city> 0 -1
 *
 * 3. Check TTL of a key: TTL "address::1"
 * 4. Delete a key: DEL "address::1"
 * 5. Flush all caches: FLUSHALL
 */

/**
 * Service class for managing addresses with Redis caching.
 * Uses Spring's caching annotations to optimize database access.
 */
@Service
public class AddressService {

    private final AddressRepository repository;

    public AddressService(AddressRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new address and updates relevant caches.
     * - Caches the new address with its ID as the key
     * - Invalidates the 'addresses' and 'userAddresses' caches since they're now stale
     * 
     * @param address The address to create
     * @return The created address with generated ID
     */
    /**
     * Creates a new address and caches it.
     * - Caches the new address with its ID as the key ('address' cache)
     * - Invalidates the 'addresses' and 'userAddresses' caches since they're now stale
     * 
     * Redis CLI: HGETALL address::<new_id>
     */
    @CachePut(value = "address", key = "#result.id")
    @CacheEvict(value = {"addresses", "userAddresses"}, allEntries = true)
    public Address createAddress(Address address) {
        return repository.save(address);
    }

    /**
     * Updates an existing address and updates relevant caches.
     * - Updates the cache for this specific address
     * 
     * Redis CLI: HGETALL address::<id>
     */
    @CachePut(value = "address", key = "#id")
    @CacheEvict(value = {"addresses", "userAddresses"}, allEntries = true)
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

    /**
     * Deletes an address and clears relevant caches.
     * - Removes the address from all caches
     * - Invalidates 'address', 'addresses', and 'userAddresses' caches
     * 
     * @param id The ID of the address to delete
     */
    @CacheEvict(value = {"address", "addresses", "userAddresses"}, allEntries = true)
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Address not found with id: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Retrieves all addresses with caching.
     * - Caches the complete list of addresses under the 'addresses' cache
     * - Subsequent calls will return the cached result until the cache is invalidated
     * 
     * @return List of all addresses
     */
    @Cacheable("addresses")
    public List<Address> getAllAddresses() {
        return repository.findAll();
    }

    /**
     * Retrieves a single address by ID with caching.
     * - Caches individual addresses with their ID as the cache key
     * - Subsequent calls for the same ID will return the cached result
     * 
     * @param id The ID of the address to retrieve
     * @return The requested address
     */
    @Cacheable(value = "address", key = "#id")
    public Address getAddressById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    /**
     * Retrieves all addresses for a specific user with caching.
     * - Caches user-specific address lists using the user ID as the cache key
     * - Useful for quick lookups of all addresses belonging to a user
     * 
     * @param userId The ID of the user
     * @return List of addresses for the specified user
     */
    @Cacheable(value = "userAddresses", key = "#userId")
    public List<Address> getAddressesByUserId(String userId) {
        return repository.findByUserId(userId);
    }
}