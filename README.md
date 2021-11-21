# easyHibernate
A minimalistic interface framework library to hibernate persistence for simple programs that need
a database but are small enough to make using Spring or a JEE container just overkill.

Read [Hibernate documentation](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html) 
to understand schema generation, [transactions](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#transactions)
and [optimistic vs. pessimistic locking](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#locking).

All this can mostly be done using annotations and a minimal hibernate configuration file.
The testing project [easyHibernateTest](https://github.com/geobe/easyHibernateTest) holds examples for 
configuration files and annotation based schema generation.
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
### DaoHibernate
Dao implementation for Hibernate 5.x persistence layer. Goal is to encapsulate a reasonable set of database operations 
with hibernate persistence layer, manage sessions and transactions and provide several query methods
including Query By Example (QBE).<br>
Every persistent class usually has its own DaoHibernate object. Inheritance is fully supported. 
Sessions are created, if not already existent,
for all operations. Transactions are automatically started for update operations, if not existent. If a
transition is already running, further updates are automatically added to the transaction scope. This
works across all DAO objects bound to the same DbHibernate! So don't forget to commit or rollback your changes 
at the end of a group of cohesive updates! Calling commit() on any of the affected DAOs is sufficient.
## Using this library
## Use jar file
Just download the latest jar from the libs directory and use in your project.
### No Maven artifact
Sorry, I don't spend the effort to publish this project on a maven repository. If you like this library 
and are experienced with maven, you are welcome to join and do it.
### Using Gradle multi-project builds
Gradle documentation explains how to 
[work with several subprojects](https://docs.gradle.org/current/userguide/multi_project_builds.html)
in one root project.
### Automatic integration into a Gradle project with Alex Vasilkovs GradleGitDependencyPlugin
[This plugin](https://github.com/alexvasilkov/GradleGitDependenciesPlugin) clones a selected version of
a git based project into a directory (typically libs) of a local Gradle project and adds it as an
implementation dependency. The testing project uses this scheme an can be used as a template for own projects.

To use the plugin, two files must be extended. The very beginning of `settings.gradle` must specify the plugin:
```
/*
    plugins statement has to be at the very top of this file
 */
plugins {
    id 'com.alexvasilkov.git-dependencies' version '2.0.4'
}
```
The `build.gradle` file references the git library you want to import:
```
git {
    implementation 'https://github.com/geobe/easyHibernate.git', {
        name 'easyHibernate'
        tag 'V1.1.2'
    }
}
```
That's all. Gradle will clone the referenced projects and include them in its builds, using reasonable defaults.
On Alex' site you can find details about further configuration options.
One advantage of this approach is that source code is automatically available, can be tailored to
your own needs and saved in your own git repository. A slight disadvantage is that imported source code is
checked for changes in every build.

## Testing
QA tests are laid off to a separate project [easyHibernateTest](https://github.com/geobe/easyHibernateTest). 
This was done for following reasons:
1. Project size - some of the tests are run against a quite large database with more than 300,000 rows. 
Using the preferred method of integration (see above) into other projects would involve cloning all
these test data.
1. Integration example - the test project integrates this library as sources into its gradle project structure.
So it can easily be used as a template for own integrations.