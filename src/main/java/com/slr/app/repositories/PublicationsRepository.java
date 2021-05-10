package com.slr.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Publications;

@RepositoryRestResource
public interface PublicationsRepository extends JpaRepository<Publications, Long>{

}
