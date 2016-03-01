# ThingDroid

Utilities library for Android. Some Features:

AlarmTask: Schedule task to run periodically. You only have to worry about annotate their methods.
Example of use:

AlarmTask(interval = {INTERVAL_IN_MILLI}, wakeUp = {BOOLEAN})
void foo(){}

GenericDao: Abstraction About OrmLite(http://ormlite.com/). 
Extend this class and have access to several common methods of database.(Create, Update, Delete, FindAll, Count, etc)

Logcoletor: Request Logcat logs remotely by Loglevel and date range.

DataMerge: Merge your WebService data with Local data.

