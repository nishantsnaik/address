package com.enterprise.address.controller;

import com.enterprise.address.model.Address;
import com.enterprise.address.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@Valid @RequestBody Address address) {
        Address saved = service.createAddress(address);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable String id, @Valid @RequestBody Address address) {
        Address updated = service.updateAddress(id, address);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAll() {
        List<Address> addresses = service.getAddressesByUser(null); // fetch all users if userId is null
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new com.enterprise.address.exception.ResourceNotFoundException("Address not found with id: " + id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getByUser(@PathVariable String userId) {
        List<Address> addresses = service.getAddressesByUser(userId);
        return ResponseEntity.ok(addresses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}