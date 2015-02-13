package smarthome.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import smarthome.arduino.utils.Logger;

public class DBManager {

  private static final String TAG = "DBManager";

  private static EntityManagerFactory entityManagerFactory;

  static {
    entityManagerFactory = Persistence.createEntityManagerFactory("smarthome", System.getProperties());
  }

  private static EntityManager getEntityManager() {
    return entityManagerFactory.createEntityManager();
  }

  public static synchronized void persistObject(Object object) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      em.persist(object);
      em.getTransaction().commit();
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
      em.getTransaction().begin();
      em.merge(object);
      em.getTransaction().commit();
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

}
