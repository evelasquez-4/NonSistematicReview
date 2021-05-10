package com.slr.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Authors;

@RepositoryRestResource
public interface AuthorsRepository extends JpaRepository<Authors, Long>  
{

}
