package introsde.rest.ehealth.test.model;

import static org.junit.Assert.*;
import introsde.rest.ehealth.model.LifeStatus;
import introsde.rest.ehealth.model.MeasureDefinition;
import introsde.rest.ehealth.model.Person;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LifeStatusTest {

    @Test
    public void readAllLifeStatusListDaoTest() {
        System.out.println("--> TEST: readAllLifeStatusWithDao");
        List<LifeStatus> mList = LifeStatus.getAll();
        assertTrue("LifeStatus not empty in DB", mList.size()>0);
    }

    @Test
    public void readLifeStatusInPersonTest() {
        System.out.println("--> TEST: readLifeStatusPersonRelationship");
        // setting weight for an existing person with existing measures
        Person person = Person.getPersonById(1);
        assertTrue("Person should have at least one measurement", person.getLifeStatus().size()>0);
        LifeStatus l = person.getLifeStatus().get(1);
        assertNotNull("LifeStatus measure was created", l.getIdMeasure());
    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("Testing JPA on lifecoach database using 'introsde-jpa' persistence unit");
        emf = Persistence.createEntityManagerFactory("introsde-jpa");
        em = emf.createEntityManager();
    }

    @AfterClass
    public static void afterClass() {
        em.close();
        emf.close();
    }

    @Before
    public void before() {
        tx = em.getTransaction();
    }

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private EntityTransaction tx;
}