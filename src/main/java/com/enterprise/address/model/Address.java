package com.enterprise.address.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "addresses")
public class Address {

    @Id
    private String id;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "type is required")
    @Pattern(regexp = "billing|shipping", message = "type must be either 'billing' or 'shipping'")
    private String type;

    @NotBlank(message = "line1 is required")
    private String line1;

    private String line2;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "state is required")
    private String state;

    @NotBlank(message = "country is required")
    private String country;

    @NotBlank(message = "postalCode is required")
    @Size(min = 3, max = 10, message = "postalCode must be between 3 and 10 characters")
    private String postalCode;
}

