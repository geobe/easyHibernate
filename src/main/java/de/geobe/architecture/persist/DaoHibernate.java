/*
 *  The MIT License (MIT)
 *
 *                            Copyright (c) 2021. Georg Beier
 *
 *                            Permission is hereby granted, free of charge, to any person obtaining a copy
 *                            of this software and associated documentation files (the "Software"), to deal
 *                            in the Software without restriction, including without limitation the rights
 *                            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *                            copies of the Software, and to permit persons to whom the Software is
 *                            furnished to do so, subject to the following conditions:
 *
 *                            The above copyright notice and this permission notice shall be included in all
 *                            copies or substantial portions of the Software.
 *
 *                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *                            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *                            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *                            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *                            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *                            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *                            SOFTWARE.
 *
 *
 */

package de.geobe.architecture.persist;

import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dao implementation for Hibernate 5.x persistence layer
 * Encapsulate all database operations with hibernate persistence layer
 * @author georg beier
 *
 * @param <PersistType> a persisted type that is handled by this dao
 */
@SuppressWarnings("deprecated")
public class DaoHibernate<PersistType> implements DataAccess<PersistType> {

    // Attribute Definitions

    @SuppressWarnings("rawtypes")
    private Class accessedType;
    private DbHibernate dbAccess;

    // Constructors

    /**
     * create new Dao object
     *
     * @param persistType The class of the PersistType. Must be given because Java
     *                    eliminates generic type parameters at compile time and
     *                    Hibernate needs type information at runtime!
     * @param dbac        The Hibernate database access object
     */
    @SuppressWarnings("rawtypes")
    public DaoHibernate(Class persistType, DbHibernate dbac) {
        accessedType = persistType;
        dbAccess = dbac;
    }

    /**
     * commits a transaction on all daos in this thread that are linked to the
     * same dbAccess
     */
    public void commit() {
        Session s = dbAccess.getActiveSession();
        Transaction t = s.getTransaction();
        if (t != null) {
            t.commit();
        }
    }

    /**
     * performs a rollback on all daos in this thread that are linked to the
     * same dbAccess object which implies to the same session
     */
    public void rollback() {
        Session s = dbAccess.getActiveSession();
        Transaction t = s.getTransaction();
        if (t != null) {
            t.rollback();
        }
    }

    /**
     * closes the database session on this dao's connection to the database. <br>
     * affects all daos in the same thread that are linked to the same database
     * connection. <br>
     * closing the session will commit open transaction,
     *
     * @see DbHibernate#closeSession()
     */
    public void closeSession() {
        dbAccess.closeSession();
    }

    /**
     * (non-Javadoc)
     *
     * @see DataAccess#save(Object)
     * also starts a transaction, if none is active
     */
    public boolean save(PersistType obj) {
        Session s = dbAccess.getActiveSession();
        try {
            s.saveOrUpdate(obj);
            return true;
        } catch (StaleObjectStateException stex) {
            s.refresh(obj);
            return false;
        } catch (RuntimeException rex) {
            Transaction t = s.getTransaction();
            if (t != null) {
                t.rollback();
            }
            s.close();
            throw rex;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#fetch(java.io.Serializable)
     * also starts a transaction, if none is active
     */
    @SuppressWarnings("unchecked")
    public PersistType fetch(Serializable id) {
        Session s = dbAccess.getActiveSession();
        return (PersistType) s.get(accessedType, id);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#fetchAll()
     * also starts a transaction, if none is active
     */
    @SuppressWarnings("unchecked")
    public List<PersistType> fetchAll() {
        Session s = dbAccess.getActiveSession();
        return (List<PersistType>) s.createQuery(
                "from " + accessedType.getCanonicalName()).list();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#find(java.lang.String)
     * also starts a transaction, if none is active
     */
    @SuppressWarnings("unchecked")
    public List<Object> find(String query) {
        Session s = dbAccess.getActiveSession();
        return s.createQuery(query).list();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#find(java.lang.String,
     *      java.util.Map)
     * also starts a transaction, if none is active
     */
    @SuppressWarnings("unchecked")
    public List<Object> find(String query, Map<String, Object> params) {
        Session s = dbAccess.getActiveSession();
        Query<?> hibernateQuery = s.createQuery(query, accessedType);
        for (String pname : params.keySet()) {
            hibernateQuery.setParameter(pname, params.get(pname));
        }
        return (List<Object>) hibernateQuery.list();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#findByExample(java.lang.Object)
     * also starts a transaction, if none is active<br>
     * Only single valued attributes are considered, no arrays or collections
     */
    public List<PersistType> findByExample(PersistType sample) {
        return findByExample(sample, new ArrayList<>());
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#findByExample(java.lang.Object,
     *      java.util.Collection)
     * also starts a transaction, if none is active<br>
     * Only single valued attributes are considered, no arrays or collections
     */
    @SuppressWarnings("unchecked")
    public List<PersistType> findByExample(PersistType sample,
                                                Collection<String> excluded) {
        Session s = dbAccess.getActiveSession();
        CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
        CriteriaQuery<PersistType> criteriaQuery = criteriaBuilder.createQuery(accessedType);
        Root<PersistType> root = criteriaQuery.from(accessedType);
        EntityType<PersistType> entityType = root.getModel();
        Set<Attribute<? super PersistType, ?>> attributes = entityType.getAttributes();
        List<Attribute<? super PersistType, ?>> basicAttributes = attributes.stream().filter(attribute ->
                attribute instanceof SingularAttribute
                        && !excluded.contains(attribute.getName())
                        && !attribute.getJavaType().isArray()
        ).collect(Collectors.toList());
        // a list of names of all attributes that are mapped to the db
        List<String> attributeNames = basicAttributes.stream().
                map(Attribute::getName).collect(Collectors.toList());
        // a list of directly accessible field names of sample object
        List<String> filedNames = Arrays.stream(accessedType.getFields()).
                map(Field::getName).collect(Collectors.toList());
        // a list of all accessible getter method names of sample object
        List<String> getterNames = Arrays.stream(accessedType.getMethods()).
                filter(method ->
                    method.getName().startsWith("get") && method.getParameterCount() == 0
                ).map(Method::getName).collect(Collectors.toList());
        // build a map to collect all not null attributes in sample object
        Map<String, Object> qbeValues = new HashMap<>();
        // loop over all persistent attributes
        attributeNames.forEach(attName -> {
            try {
                Object val = null;
                // direct field access possible?
                if (filedNames.contains(attName)) {
                    val = accessedType.getField(attName).get(sample);
                } else if (getterNames.contains(getterFor(attName))) {
                    // there is a getter method?
                    val = accessedType.getMethod(getterFor(attName)).invoke(sample);
                }
                if (val != null) {
                    qbeValues.put(attName, val);
                }
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        // now we  can create a list of query predicates
        List<Predicate> predicateList = new ArrayList<>(qbeValues.size());
        qbeValues.keySet().forEach(key -> {
            Object val = qbeValues.get(key);
            if (val instanceof String) {
                String stval = (String) val;
                predicateList.add(criteriaBuilder.like(root.get(key), stval));
            } else {
                predicateList.add(criteriaBuilder.equal(root.get(key), val));
            }
        });
        Predicate[] predicates = predicateList.toArray(Predicate[]::new);
        // all predicates of the list must be met
        criteriaQuery.select(root).where(predicates);
        // finally create and execute the query
        Query<PersistType> query = s.createQuery(criteriaQuery);
        return query.getResultList();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#delete(java.lang.Object)
     * also starts a transaction, if none is active
     */
    public void delete(PersistType obj) {
        Session s = dbAccess.getActiveSession();
        s.delete(obj);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.geobe.architecture.persist.DataAccess#deleteAll()
     * also starts a transaction, if none is active
     */
    public void deleteAll() {
        Session s = dbAccess.getActiveSession();
        s.createQuery("delete " + accessedType.getCanonicalName())
                .executeUpdate();
    }

    // Attribute Accessors

    @SuppressWarnings("rawtypes")
    public Class getAccessedType() {
        return accessedType;
    }

    @SuppressWarnings({"unused", "rawtypes"})
    private void setAccessedType(Class pAccessedType) {
        accessedType = pAccessedType;
    }

    public DbHibernate getDbAccess() {
        return dbAccess;
    }

    @SuppressWarnings("unused")
    private void setDbAccess(DbHibernate pDbAccess) {
        dbAccess = pDbAccess;
    }

    /**
     * build getter name for attribute name
     * @param attName name of jpa attribute
     * @return name of corresponding getter method
     */
    private String getterFor(String attName) {
        return "get" + attName.substring(0, 1).toUpperCase() + attName.substring(1);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "(accessedType: " + getAccessedType() + ")" + "(dbAccess: "
                + getDbAccess() + ")";
    }

}
