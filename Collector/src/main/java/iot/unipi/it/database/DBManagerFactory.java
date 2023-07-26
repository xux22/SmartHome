package iot.unipi.it.database;

public class DBManagerFactory {
    private DBManagerFactory(){};
    private static DBManager dbInstance = null;

    public static DBManager createDBManager(String URL, String user, String password) {
        if(dbInstance == null)
            dbInstance = new DBManager(URL, user, password);
        return dbInstance;
    }

    public static DBManager getDbInstance(){
        return dbInstance;
    }
}
