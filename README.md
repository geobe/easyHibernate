# easyHibernate

A minimalistic interface framework library to hibernate persistence for simple programs 
not using a JEE container or Spring.

## Classes

### DbHibernate

Manage hibernate database access for a single database and hide subtleties of the database system and hibernate
from application code. More then one DbHibernate object can be used to
connect to different databases. Basic configuration is defined in an xml configuration file
(default hibernate.cfg.xml), other files can be passed to the constructor. Most additional configuration
can (and should) be made using JPA annotations.<br>
This is a "heavy weight" object because connectiong to a database is a complex operation. Thus objects of this
class should be constructed at program start and closed before its end.

Database sessions are provided by this class. These are "light weight" objects that typically group
a set of cohesive database operations. In most cases, session management can be delegated to DaoHibernate
objects. So session objects usually don't show up in application code when using this library.

### DbAccess
This interface defines the basic CRUD database operations. It follows the popular Data Access Object (DAO) pattern.
Using an interface allows a "real" database backed implementation DaoHibernate and a mock implementation
DaoTransientListfor some test settings.

### DaoHibernate

Dao implementation for Hibernate 5.x persistence layer. Goal is to encapsulate all database operations 
with hibernate persistence layer, manage sessions and transactions and provide several query methods
including Query By Example (QBE).<br>
Every persistent class usually has its own DaoHibernate object. Sessions are created, if not already existent,
for all operations. Transactions are automatically started for update operations, if not existent. If a
transition is already running, further updates are automatically added to the transaction scope. This
works across all DAO objects bound to the same DbHibernate! So don't forget to commit or rollback your changes 
at the end of a group of cohesive updates! Calling commit() on any of the affected DAOs is sufficient.

### DaoTransientList

An incomplete implementation of the DbAccess interface for some testing purposes.
