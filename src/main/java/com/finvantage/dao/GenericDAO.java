package com.finvantage.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) interface.
 * Demonstrates OOP Generics, Abstraction, and the DAO Pattern.
 *
 * @param <T>  Entity domain type
 * @param <ID> Primary key identifier type
 */
public interface GenericDAO<T, ID> {

    T save(T entity) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean deleteById(ID id) throws SQLException;
}
