package dao;

import entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;


import java.util.List;

public class ParametersDao {
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
    public Parameters findById(int id) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        Parameters param = em.find(Parameters.class, id);
        return param;
    }

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
