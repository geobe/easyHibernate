package de.geobe.architecture.test;

import de.geobe.architecture.persist.DaoHibernate;
import de.geobe.architecture.persist.DbHibernate;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * test class to verify all methods of DaoHibernate and DbHibernate.
 * TODO transform into unit tests
 */
public class Testground {

    static Server tcpServer;
    static Server webServer;
    static List<String> classes = new ArrayList<>();
    static DbHibernate db;
    static DaoHibernate<AddressBase> baseDao;
    static DaoHibernate<Communication> commDao;
    static DaoHibernate<OrganisationalAddress> orgaDao;
    static DaoHibernate<PersonalAddress> persDao;

    static {
        classes.add("de.geobe.architecture.test.AddressBase");
        classes.add("de.geobe.architecture.test.Communication");
        classes.add("de.geobe.architecture.test.OrganisationalAddress");
        classes.add("de.geobe.architecture.test.PersonalAddress");
    }

    static void runServer() {
        try {
            tcpServer = Server.createTcpServer("-baseDir", "~/H2", "-tcpAllowOthers", "-tcpDaemon", "-ifNotExists");
            if (!tcpServer.isRunning(true)) {
                tcpServer.start();
                System.out.println("tcpServer startet");
            }
        } catch (Exception ex) {
            System.out.println("Exception: tcpServer is already running");
        }
        try {
            webServer = Server.createWebServer("-baseDir", "~/H2", "-webAllowOthers", "-webDaemon");
            if (!webServer.isRunning(true)) {
                webServer.start();
                System.out.println("webserver startet");
            }
        } catch (Exception ex) {
            System.out.println("Exception: webServer is already running");
        }
    }

    static void stopServer() {
        tcpServer.shutdown();
        webServer.shutdown();
    }

    static void createDbConnection() {
        db = new DbHibernate(classes);
        baseDao = new DaoHibernate<>(AddressBase.class, db);
        commDao = new DaoHibernate<>(Communication.class, db);
        orgaDao = new DaoHibernate<>(OrganisationalAddress.class, db);
        persDao = new DaoHibernate<>(PersonalAddress.class, db);

    }

    static List<AddressBase> generateSample() {
        List<AddressBase> result = new ArrayList<>();
        Communication[] comms = {
                new Communication(CommType.MOBILE, "0167 345 6789", "Nikki Handy"),
                new Communication(CommType.MESSENGER, "telegram://Nico_Lausi_1234", "Nikki Telegram")
        };
        PersonalAddress nikki = new PersonalAddress(
                "Nikki", "Nico", "Lausi", Instant.parse("1234-12-06T06:00:00Z"));
        nikki.addComm(comms[0]);
        nikki.addComm(comms[1]);
        comms[0].setOwner(nikki);
        comms[1].setOwner(nikki);
        result.add(nikki);
        result.add(new PersonalAddress("Doggi", "Oggi", "Dalmatian", Instant.parse("2008-11-06T00:00:00Z")));
        result.add(new PersonalAddress("Pipa", "Pille", "Palle", Instant.parse("2000-01-01T00:00:00Z")));
        result.add(new PersonalAddress("Lups", "Luna", "Pudel", Instant.parse("2018-11-06T00:00:00Z")));
        result.add(new PersonalAddress("Lemmi", "Ein", "Lemming", Instant.now()));
        result.add(new OrganisationalAddress("Die Firma", "TBQ"));
        return result;
    }

    public static void main(String[] args) {
        runServer();
        createDbConnection();
        commDao.deleteAll();
        baseDao.deleteAll();
        baseDao.closeSession();
        generateSample().forEach(adb -> baseDao.save(adb));
        List<AddressBase> adbs = baseDao.fetchAll();
        assert adbs.size() == 6;
        PersonalAddress sample = new PersonalAddress();
        sample.setNickname("L%");
        List<PersonalAddress> qbe1 = persDao.findByExample(sample);
        assert qbe1.stream().map(PersonalAddress::getNickname).collect(Collectors.toList()).containsAll(Arrays.asList("Lups", "Lemmi"));
        PersonalAddress anAddress = qbe1.get(0);
        long id = anAddress.getId();
        anAddress.setNickname("Duffy");
        persDao.save(anAddress);
        AddressBase toFetch = baseDao.fetch(id);
        assert toFetch.getNickname().equals("Duffy");
        List<Object> cl = commDao.find("from Comm");
        assert cl.size() == 2;
        List<Object> hql = commDao.find("select owner from Comm where commtype = :ct", Map.of("ct", CommType.MOBILE.ordinal()));
        assert hql.size() == 1;
        assert hql.get(0) instanceof PersonalAddress;
        baseDao.commit();
        toFetch.setNickname("Schnuffy");
        baseDao.save(toFetch);
        baseDao.rollback();
        // rollback doesn't change the local object
        assert toFetch.getNickname().equals("Schnuffy");
        // so retrieve a new copy from db
        AddressBase newFetch = baseDao.fetch(toFetch.getId());
        // assert rollback succeeded
        assert newFetch.getNickname().equals("Duffy");
        baseDao.delete(newFetch);
        baseDao.commit();
        toFetch = baseDao.fetch(id);
        assert toFetch == null;
        baseDao.closeSession();
        persDao.closeSession();
        db.closeSession();
        System.out.println("Database " + db.toString() + ", baseDao " + baseDao.toString());
//        stopServer();
        db.closeDatabase();
//        while (true) {
//            Thread.sleep(1000);
//        }
    }
}
