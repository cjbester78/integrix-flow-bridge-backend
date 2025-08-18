package com.integrixs.data.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Entity
@Table(name = "roles")
/**
 * Entity representing Role.
 * This maps to the corresponding table in the database.
 */
public class Role {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    /** Unique identifier (UUID) for the entity */
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    /** Name of the component */
    private String name;

    @Column(columnDefinition = "json")
    private String permissions;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
