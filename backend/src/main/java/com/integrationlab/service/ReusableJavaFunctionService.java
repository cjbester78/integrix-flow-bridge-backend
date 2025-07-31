package com.integrationlab.service;

import com.integrationlab.model.ReusableFunction;
import com.integrationlab.repository.ReusableFunctionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReusableJavaFunctionService {

    private final ReusableFunctionRepository repository;

    public ReusableJavaFunctionService(ReusableFunctionRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieve all reusable Java functions.
     * @return List of all functions
     */
    public List<ReusableFunction> findAll() {
        return repository.findAll();
    }

    /**
     * Find a reusable Java function by its unique ID.
     * @param id Function ID
     * @return Optional containing the function if found
     */
    public Optional<ReusableFunction> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Find a reusable Java function by its name.
     * @param name Function name
     * @return Optional containing the function if found, or empty if name is null or not found
     */
    public Optional<ReusableFunction> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return repository.findByName(name);
    }

    /**
     * Save or update a reusable Java function.
     * @param func The function entity
     * @return The saved function entity
     */
    public ReusableFunction save(ReusableFunction func) {
        return repository.save(func);
    }

    /**
     * Delete a reusable Java function by ID.
     * @param id Function ID to delete
     */
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
