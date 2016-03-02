# ThinDroid

Utilities library for Android. Some Features:

AlarmTask: Schedule task to run periodically. You only have to worry about annotate their methods.
Example of use:
```
@AlarmTask(interval = {INTERVAL_IN_MILLI}, wakeUp = {BOOLEAN})
void foo(){
    System.out.print("Executing this code every {INTERVAL_IN_MILLI} milliseconds")
}
```
GenericDao: Abstraction About OrmLite(http://ormlite.com/). 
Extend this class and have access to several common methods of database.(Create, Update, Delete, FindAll, Count, etc)
Example:

```
@DatabaseTable
class Book extends GenericDao<Book>{

  @DatabaseField(generatedId = true)
  private Long id;
  @DatabaseField
  private String name;
  
  public AppStatistics() {super(Book.class);}
  
  private void testGenericDao(){
    Book book = new Book();
    //Create new book"
    book.createOrUpdate();
    //Get book
    findById(Book.class, id);
    //Get all books
    List<Book> books = getAll(); //Or static getAll(Book.class)
    //Get count
    long count = book.getCount(); //Or static getCount(Book.class)
    //Getting dao
    Dao<Book, Object> dao = getDao(); //Or static getDao(Book.class)
  }
}
```

Logcoletor: Request Logcat logs remotely by Loglevel and date range.

DataMerge: Merge your WebService data with Local data.

<<<<<<< HEAD
=======
**And More...[In documentation process]**

>>>>>>> fb8c2c8b76db17f02f9c1b2f6549d6a46e073f01
# Getting start:

####Adding main library:
######Add on toplevel build.gradle:

    ```
    allprojects {
        repositories {
            jcenter()
            maven{
                url 'https://dl.bintray.com/carlosearaujo/maven/'
            }
        }
    }
    ```

######And on module build.gradle:
    ```
    dependencies {
      ...
      compile('org.java.thindroid:commons:1.0.0')
    }
    ```
    
####Adding compiler module (If you are using annotations(like AlarmTask))
  
  Add apt plugin on toplevel build.gradle:
  
  ```
  dependencies {
  	...
  	classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
  }
  ```
  
  Copy compiler-annotations module:

  ![alt tag](https://raw.githubusercontent.com/carlosedinazioaraujo/ThingDroid/master/resources/usage-1.png)
  
  Add compiler on build.gradle module:

  ```
  apply plugin: 'com.neenbedankt.android-apt'
  
  ...
  //Resolve annotations on compilation time - add this line if you are using AlarmTask annotation
  apt project(':annotations-compiler') 
  ```

##Extends 'Application' class and add on Manifest:
```
public class SampleApplication extends org.java.thindroid.commons.Application {
}
```

```
Manifest.xml:
<application
        android:name=".SampleApplication">
...
```
