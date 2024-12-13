package dao;

import entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;


import java.util.List;

/**
 * Data Access Object (DAO) for managing Parameters entities.
 */
public class ParametersDao {

    /**
     * Persists a {@link Parameters} entity to the database.
     *
     * @param param the {@link Parameters} entity to be persisted.
     * @throws RuntimeException if the transaction fails or an exception occurs during persistence.
     */
    public void persist(Parameters param) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            em.getTransaction().begin();
            em.persist(param);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Retrieves a {@link Parameters} entity by its ID.
     *
     * @param id the primary key of the {@link Parameters} entity to retrieve.
     * @return the {@link Parameters} entity with the specified ID.
     */
    public Parameters findById(int id) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        Parameters param = em.find(Parameters.class, id);
        return param;
    }

    /**
     * Retrieves all {@link Parameters} entities from the database.
     *
     * @return a list of {@link Parameters} entities.
     */
    public List<Parameters> findAll() {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            TypedQuery<Parameters> query = em.createQuery("select e from Parameters e", Parameters.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
