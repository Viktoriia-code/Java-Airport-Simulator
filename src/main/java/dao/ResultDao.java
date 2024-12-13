package dao;

import entity.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object (DAO) for Result entities.
 */
public class ResultDao {

    /**
     * Persist a Result entity in the database.
     *
     * @param result the entity to persist
     */
    public void persist(Result result) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e; // rethrow the exception after rollback
        }
    }

    /**
     * Retrieves all {@link Result} entities from the database.
     *
     * @return a list of {@link Result} entities.
     */
    public List<Result> findAll() {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            TypedQuery<Result> query = em.createQuery("select e from Result e", Result.class);
            return query.getResultList();
        } finally {
            em.close(); // Important if EntityManager is not managed by a container
        }
    }

    /**
     * Deletes a {@link Result} entity from the database.
     *
     * @param result the {@link Result} entity to be deleted.
     * @throws RuntimeException if the transaction fails or an exception occurs during deletion.
     */
    public void delete(Result result) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            em.getTransaction().begin();
            if (!em.contains(result)) {
                result = em.merge(result); // Ensure entity is managed before removing
            }
            em.remove(result);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e; // rethrow the exception after rollback
        }
    }
}
