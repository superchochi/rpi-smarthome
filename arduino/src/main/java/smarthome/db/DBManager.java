package smarthome.db;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.exceptions.DatabaseException;

import smarthome.arduino.utils.Logger;

public class DBManager {

  private static final String TAG = "DBManager";
  private static final String PROPERTY_DELETE_OLDEST_COUNT = "smarthome.db.delete.oldest.count";
  private static final String PROPERTY_PAGE_SIZE = "smarthome.db.page.size";
  private static final String PROPERTY_PAGE_COUNT = "smarthome.db.page.count";

  private static EntityManagerFactory entityManagerFactory;

  public static void open() {
    entityManagerFactory = Persistence.createEntityManagerFactory("smarthome", System.getProperties());
    setPragmas();
    dumpPragmas();
  }

  public static void close() {
    entityManagerFactory.close();
    entityManagerFactory = null;
  }

  private static synchronized void setPragmas() {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      int pageSize = Integer.getInteger(PROPERTY_PAGE_SIZE, 1024).intValue();
      em.createNativeQuery("PRAGMA page_size=" + pageSize).executeUpdate();
      int pageCount = Integer.getInteger(PROPERTY_PAGE_COUNT, 1000).intValue();
      em.createNativeQuery("PRAGMA max_page_count=" + pageCount).getSingleResult();
      em.getTransaction().commit();
    } catch (Exception e) {
      Logger.error(TAG, "Error setting pragmas!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  private static EntityManager getEntityManager() {
    if (entityManagerFactory == null) {
      open();
    }
    return entityManagerFactory.createEntityManager();
  }

  public static synchronized void dumpPragmas() {
    EntityManager em = null;
    try {
      em = getEntityManager();
      Logger.debug(TAG, "page_size: " + em.createNativeQuery("PRAGMA page_size").getSingleResult());
      Logger.debug(TAG, "max_page_count: " + em.createNativeQuery("PRAGMA max_page_count").getSingleResult());
    } catch (Exception e) {
      Logger.error(TAG, "Error getting pragmas!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public static synchronized void persistObject(Object object) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      try {
        em.getTransaction().begin();
        em.persist(object);
        em.getTransaction().commit();
      } catch (RollbackException e) {
        if (isDBFullException(e)) {
          em.close();
          freeSpace();
          em = getEntityManager();
          em.getTransaction().begin();
          em.persist(object);
          em.getTransaction().commit();
        } else {
          throw e;
        }
      }
    } catch (Exception e) {
      Logger.error(TAG, "Object: " + object + " not persisted!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public static synchronized void mergeObject(Object object) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      try {
        em.getTransaction().begin();
        em.merge(object);
        em.getTransaction().commit();
      } catch (RollbackException e) {
        if (isDBFullException(e)) {
          em.close();
          freeSpace();
          em = getEntityManager();
          em.getTransaction().begin();
          em.merge(object);
          em.getTransaction().commit();
        } else {
          throw e;
        }
      }
    } catch (Exception e) {
      Logger.error(TAG, "Object: " + object + " not merged!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public static synchronized void deleteObject(Object object) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      em.remove(object);
      em.getTransaction().commit();
    } catch (Exception e) {
      Logger.error(TAG, "Object: " + object + " not removed!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  public static synchronized <T> T getObjectById(Class<T> clazz, Object id) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      T t = em.find(clazz, id);
      return t;
    } catch (Exception e) {
      Logger.error(TAG, "Error getting object with id: " + id + "!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
    return null;
  }

  public static synchronized <T> List<T> getObjects(String namedQuery, Class<T> clazz, Map<String, ?> params) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
      if (params != null) {
        for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
          String key = it.next();
          Object value = params.get(key);
          query.setParameter(key, value);
        }
      }
      return query.getResultList();
    } catch (Exception e) {
      Logger.error(TAG, "Error getting objects from query: " + namedQuery + "!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
    return null;
  }

  public static synchronized <T> void updateObject(String namedQuery, Class<T> clazz, Map<String, ?> params) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
      if (params != null) {
        for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
          String key = it.next();
          Object value = params.get(key);
          query.setParameter(key, value);
        }
      }
      query.executeUpdate();
      em.getTransaction().commit();
    } catch (Exception e) {
      Logger.error(TAG, "Error updating object from query: " + namedQuery + "!", e);
    } finally {
      if (em != null && em.isOpen()) {
        try {
          em.close();
        } catch (Exception e) {
        }
      }
    }
  }

  private static boolean isDBFullException(RollbackException e) {
    if (e.getCause() instanceof DatabaseException) {
      DatabaseException dbe = (DatabaseException) e.getCause();
      if (dbe.getErrorCode() == DatabaseException.SQL_EXCEPTION) {
        SQLException sqle = (SQLException) dbe.getCause();
        if (sqle.getErrorCode() == 13) {
          return true;
        }
      }
    }
    return false;
  }

  private static void freeSpace() {
    int num = Integer.getInteger(PROPERTY_DELETE_OLDEST_COUNT, 100).intValue();
    Logger.info(TAG, "DB is full! Delete oldest " + num + " entries...", null);
    EntityManager em = getEntityManager();
    em.getTransaction().begin();
    em.createNativeQuery(
        "delete from statisticentry where id in (select id from statisticentry order by timestamp asc limit " + num
            + ")").executeUpdate();
    em.close();
    setPragmas();
  }

}
