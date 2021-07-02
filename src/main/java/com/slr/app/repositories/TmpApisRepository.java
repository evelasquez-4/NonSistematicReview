package com.slr.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.TmpApis;

@RepositoryRestResource
public interface TmpApisRepository extends JpaRepository<TmpApis, Long>{

}
