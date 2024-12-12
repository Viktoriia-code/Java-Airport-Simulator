package datasource;

import jakarta.persistence.*;

/**
 * A singleton class that provides a connection to a MariaDB database using JPA.
 */
public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;
    private static EntityManager em = null;

    public static synchronized EntityManager getInstance() {

        if (em==null) {
            if (emf==null) {
                emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
            }
            em = emf.createEntityManager();
        }
        return em;
    }
}
