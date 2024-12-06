package dao;

import entity.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ResultDao {

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

    public List<Result> findAll() {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        try {
            TypedQuery<Result> query = em.createQuery("select e from Result e", Result.class);
            return query.getResultList();
        } finally {
            em.close(); // Important if EntityManager is not managed by a container
        }
    }

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
