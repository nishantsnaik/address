package com.enterprise.address.controller;

import com.enterprise.address.service.CacheService;
import com.enterprise.address.exception.ResourceNotFoundException;
import com.enterprise.address.model.Address;
import com.enterprise.address.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing addresses.
 * Provides endpoints for CRUD operations on addresses.
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    // Service layer for address operations
    private final AddressService service;
    
    // Service for managing caches
    private final CacheService cacheService;

    public AddressController(AddressService service, CacheService cacheService) {
        this.service = service;
        this.cacheService = cacheService;
    }

    /**
     * Creates a new address.
     *
     * @param address The address to create
     * @return The created address with HTTP 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Address createAddress(@Valid @RequestBody Address address) {
        return service.createAddress(address);
    }

    /**
     * Updates an existing address.
     *
     * @param id The ID of the address to update
     * @param address The updated address data
     * @return The updated address
     */
    @PutMapping("/{id}")
    public Address updateAddress(@PathVariable String id, @Valid @RequestBody Address address) {
        return service.updateAddress(id, address);
    }

    /**
     * Retrieves all addresses.
     *
     * @return List of all addresses
     */
    @GetMapping
    public List<Address> getAll() {
        return service.getAllAddresses();
    }

    /**
     * Retrieves an address by ID.
     *
     * @param id The ID of the address to retrieve
     * @return The requested address
     * @throws ResourceNotFoundException if the address is not found
     */
    @GetMapping("/{id}")
    public Address getById(@PathVariable String id) {
        return service.getAddressById(id);
    }

    /**
     * Retrieves all addresses for a specific user.
     *
     * @param userId The ID of the user
     * @return List of addresses for the specified user
     */
    @GetMapping("/user/{userId}")
    public List<Address> getByUser(@PathVariable String userId) {
        return service.getAddressesByUserId(userId);
    }

    /**
     * Deletes an address by ID.
     *
     * @param id The ID of the address to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clears all caches in the application.
     * 
     * <p>This endpoint clears all caches, including:</p>
     * <ul>
     *   <li>Individual address caches (by ID)</li>
     *   <li>Complete list of all addresses</li>
     *   <li>User-specific address lists</li>
     *   <li>Redis data store</li>
     * </ul>
     * 
     * <p>This is useful for:</p>
     * <ul>
     *   <li>Development and debugging</li>
     *   <li>Forcing fresh data to be loaded from the database</li>
     *   <li>Recovering from potential cache inconsistencies</li>
     * </ul>
     *
     * @return A response indicating the result of the operation
     * @apiNote Requires ADMIN role (if security is enabled)
     * @see CacheService#clearAllCaches()
     */
    @PostMapping("/clear-cache")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> clearCache() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok("✅ All caches have been cleared successfully");
    }
}