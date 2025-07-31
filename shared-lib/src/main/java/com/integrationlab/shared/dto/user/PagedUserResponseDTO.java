package com.integrationlab.shared.dto.user;

import java.util.List;

/**
 * DTO for PagedUserResponseDTO.
 * Encapsulates data for transport between layers.
 */
public class PagedUserResponseDTO {
	
    private List<UserDTO> users;
    private int page;
    private int totalPages;
    private long totalElements;

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }


}