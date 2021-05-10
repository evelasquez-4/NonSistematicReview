package com.slr.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.AuthorPublications;

@RepositoryRestResource
public interface AuthorPublicationsRepository extends JpaRepository<AuthorPublications, Long> {

}
