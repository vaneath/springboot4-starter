package com.valome.starter.repository.jpa.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface that filters out soft-deleted records.
 * 
 * All entities extending BaseModel should use repositories extending this
 * interface
 * to automatically filter out records where deletedAt IS NOT NULL.
 * 
 * @param <T>  the entity type
 * @param <ID> the ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find all non-deleted entities.
     * 
     * @return list of entities where deletedAt IS NULL
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    @Override
    List<T> findAll();

    /**
     * Find all non-deleted entities sorted.
     * 
     * @param sort the sort specification
     * @return list of entities where deletedAt IS NULL, sorted
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    @Override
    List<T> findAll(Sort sort);

    /**
     * Find all non-deleted entities with pagination.
     * 
     * @param pageable the pagination specification
     * @return page of entities where deletedAt IS NULL
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    @Override
    Page<T> findAll(Pageable pageable);

    /**
     * Find a non-deleted entity by ID.
     * 
     * @param id the entity ID
     * @return Optional containing the entity if found and not deleted, empty otherwise
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = ?1 AND e.deletedAt IS NULL")
    @Override
    Optional<T> findById(ID id);

    /**
     * Count all non-deleted entities.
     * 
     * @return count of entities where deletedAt IS NULL
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    @Override
    long count();
}
