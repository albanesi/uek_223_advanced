package ch.course223.advanced.core;

import java.util.List;

public interface ExtendedService<T extends ExtendedEntity> {

	T save(T entity);

	T updateById(String id, T entity);

	void deleteById(String id);

	List<T> findAll();

	T findById(String id);

	boolean existsById(String id);
}