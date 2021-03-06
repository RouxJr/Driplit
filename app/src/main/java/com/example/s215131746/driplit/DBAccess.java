package com.example.s215131746.driplit;

import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.os.StrictMode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import viewmodels.Business;
import viewmodels.ItemUsageModel;
import viewmodels.PersonModel;
import viewmodels.ReportLeakModel;
import viewmodels.ResidentUsageModel;
import viewmodels.Surburb;
import viewmodels.TipModel;
import viewmodels.UspMobGetPersonItemTotal;
import viewmodels.UspMobGetPersonTotalUsage;
import viewmodels.uspMobGetSnitches;

/**
 * Created by s216127904 on 2018/09/06.
 */
public class DBAccess {

    private ResultSet outerResultSet;

    private static class DBHelper {
        private static String conString;
        private static Connection connection;
        private static PreparedStatement st;
        private static ResultSet innerResultSet;
        private static String forName;
        static String us;
        static String password;

        private static boolean Connect() {
            boolean isConnecting = false;
            BackGroundConnection connectionProperties = new BackGroundConnection();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                String[] properties = connectionProperties.ConnectionProperties();

                conString = properties[0];
                forName = properties[1];
                us = properties[2];
                password = properties[3];
                Class.forName(forName).newInstance();
                connection = DriverManager.getConnection(conString, us, password);
                if (connection != null)
                    isConnecting = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isConnecting;
        }

        private static void Close() {
           if(connection!=null||innerResultSet!=null)
            try {
                innerResultSet.close();
                st.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        static String SetParaToPass(String sql, Object[] parameters) {
            String usp = "{ CALL " + sql + "( ?";
            for (int i = 1; i < parameters.length; i++) {
                usp += ",?";
            }
            usp += " )}";
            return usp;
        }

        static boolean NonQuery(String sql, Object[] parameters) {
            if (Connect()) {
                int i = 0;
                try {
                    st = connection.prepareStatement(SetParaToPass(sql, parameters));
                    int count = 1;
                    for (Object para : parameters) {
                        BindParameter(count, para, st);
                        count++;
                    }
                    i = st.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            return i == 1;
            } else {
                return false;
            }

        }

        static ResultSet Select(String sql) {
            try {
                if(Connect()) {
                    st = connection.prepareStatement(sql);
                    innerResultSet = st.executeQuery();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return innerResultSet;
        }

        static ResultSet SelectPara(String sql, Object[] parameters) {
            try {
                if(Connect()) {
                    st = connection.prepareStatement(SetParaToPass(sql, parameters));
                    int i = 1;
                    for (Object para : parameters) {
                        BindParameter(i, para, st);
                        i++;
                    }
                    innerResultSet = st.executeQuery();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return innerResultSet;
        }

        private static void BindParameter(int parameterIndex, Object parameterObj, PreparedStatement preparedStatement) throws SQLException {
            //Hate this, I really do But the is no other way
            if (parameterObj == null) {
                preparedStatement.setNull(parameterIndex, java.sql.Types.OTHER);
            } else {
                if (parameterObj instanceof String) {
                    preparedStatement.setString(parameterIndex, (String) parameterObj);
                } else if (parameterObj instanceof Integer) {
                    preparedStatement.setInt(parameterIndex, ((Integer) parameterObj).intValue());
                } else if (parameterObj instanceof Long) {
                    preparedStatement.setLong(parameterIndex, ((Long) parameterObj).longValue());
                } else if (parameterObj instanceof Float) {
                    preparedStatement.setFloat(parameterIndex, ((Float) parameterObj).floatValue());
                } else if (parameterObj instanceof Boolean) {
                    preparedStatement.setBoolean(parameterIndex, ((Boolean) parameterObj).booleanValue());
                } else if (parameterObj instanceof Byte) {
                    preparedStatement.setInt(parameterIndex, ((Byte) parameterObj).intValue());
                } else if (parameterObj instanceof BigDecimal) {
                    preparedStatement.setBigDecimal(parameterIndex, (BigDecimal) parameterObj);
                } else if (parameterObj instanceof Short) {
                    preparedStatement.setShort(parameterIndex, ((Short) parameterObj).shortValue());
                } else if (parameterObj instanceof Double) {
                    preparedStatement.setDouble(parameterIndex, ((Double) parameterObj).doubleValue());
                } else if (parameterObj instanceof byte[]) {
                    preparedStatement.setBytes(parameterIndex, (byte[]) parameterObj);
                } else if (parameterObj instanceof java.sql.Date) {
                    preparedStatement.setDate(parameterIndex, (java.sql.Date) parameterObj);
                } else if (parameterObj instanceof Time) {
                    preparedStatement.setTime(parameterIndex, (Time) parameterObj);
                } else if (parameterObj instanceof Timestamp) {
                    preparedStatement.setTimestamp(parameterIndex, (Timestamp) parameterObj);
                } else if (parameterObj instanceof BigInteger) {
                    preparedStatement.setString(parameterIndex, parameterObj.toString());
                } else {
                    preparedStatement.setObject(parameterIndex, parameterObj);
                }
            }
        }
    }

    public boolean isConnecting() {
        return DBHelper.Connect();
    }

    public PersonModel LoginPerson(PersonModel person) {
        //this object acts like a sqlparameter like in c#
        Object[] paras = {person.email, person.userPassword};
        //pass the stored procedure name and the paras if you have parameter
        outerResultSet = DBHelper.SelectPara("uspMobGetPerson", paras);
        //the is alot that could go wrong when trying to connect to the database that is why the is a try catch
        try {
            //the outerResulSet is the table returned from the execution of the stored procedure
            outerResultSet.next();//Moves from row of Heading to row record
            person.id = outerResultSet.getInt("PersonID");//you need to specify not only the colunm name but also the data type to be return
            person.fullName = outerResultSet.getString("FullName");
            person.email = outerResultSet.getString("Email");
            person.userPassword = outerResultSet.getString("UserPassword");
            DBHelper.Close();//to closes the connection
        } catch (SQLException e) {
            e.printStackTrace();// I propable should inform the user
        }
        return person;
    }

    public ArrayList<ItemUsageModel> GetItems() {
        ArrayList<ItemUsageModel> itemUsageModel = new ArrayList<>();
        try {
            outerResultSet = DBHelper.Select("{CALL uspMobGetWaterUsageItmes}");
            while (outerResultSet.next()) {
                ItemUsageModel item = new ItemUsageModel();
                try {
                    DownLoadPicture d = new DownLoadPicture("icon/" + outerResultSet.getString("Icon"));
                    item.ItemIcon = d.doInBackground();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                item.ItemID = outerResultSet.getInt("ItemID");
                item.ItemDescription = outerResultSet.getString("ItemDescription");
                item.ItemAverage = outerResultSet.getFloat("ItemAverageAmount");

                //item.ItemIcon = resultSet.getByte();
                itemUsageModel.add(item);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemUsageModel;
    }

    public ArrayList<UspMobGetPersonTotalUsage> GetPersonTotalUsageGetItems(String email) {
        ArrayList<UspMobGetPersonTotalUsage> usages = new ArrayList<>();
       if(!email.equals(null)) {
           try {
               Object[] paras = {email};
               outerResultSet = DBHelper.SelectPara(" uspMobGetPersonTotalUsage ", paras);
               while (outerResultSet.next()) {
                   UspMobGetPersonTotalUsage usage = new UspMobGetPersonTotalUsage();
                   usage.UsageDay = outerResultSet.getString("UsageDay");
                   usage.UsageAmount = outerResultSet.getFloat("UsageAmount");
                   //item.ItemIcon = resultSet.getByte();
                   usages.add(usage);
               }
               DBHelper.Close();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }
        return usages;

    }
    public ArrayList<UspMobGetPersonTotalUsage> MobGetOverallTrend() {
        ArrayList<UspMobGetPersonTotalUsage> usages = new ArrayList<>();
        try {

            outerResultSet = DBHelper.Select("{CALL uspMobGetOverallTrend }");
            while (outerResultSet.next()) {
                UspMobGetPersonTotalUsage usage = new UspMobGetPersonTotalUsage();
                usage.UsageDay = outerResultSet.getString("UsageDay");
                usage.UsageAmount = outerResultSet.getFloat("UsageAmount");
                usages.add(usage);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usages;

    }
    public ArrayList<UspMobGetPersonItemTotal> uspMobGetPersonItemTotal(String userEmail) {
        ArrayList<UspMobGetPersonItemTotal> TotalUsage = new ArrayList<>();
        try {
            Object[] paras = {userEmail};
            outerResultSet = DBHelper.SelectPara(" uspMobGetPersonItemTotal ", paras);
            while (outerResultSet.next()) {
                UspMobGetPersonItemTotal use = new UspMobGetPersonItemTotal();
                use.ItemID = outerResultSet.getInt("ItemID");
                use.ItemName = outerResultSet.getString("Item");
                use.UsageAmount = outerResultSet.getFloat("TotalUsageForItem");
                TotalUsage.add(use);
            }

            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return TotalUsage;
    }

    public ArrayList<UspMobGetPersonItemTotal> uspMobGetPersonItemTotalDate(String userEmail, String date) {
        ArrayList<UspMobGetPersonItemTotal> TotalUsage = new ArrayList<>();
        try {
            Object[] paras = {userEmail, date};
            outerResultSet = DBHelper.SelectPara(" uspMobGetPersonItemTotalDate ", paras);
            while (outerResultSet.next()) {
                UspMobGetPersonItemTotal use = new UspMobGetPersonItemTotal();
                use.ItemID = outerResultSet.getInt("ItemID");
                use.ItemName = outerResultSet.getString("Item");
                use.UsageAmount = outerResultSet.getFloat("TotalUsageForItem");
                TotalUsage.add(use);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return TotalUsage;
    }

    public ArrayList<ResidentUsageModel> MobGetPersonUsage(String userEmail) {
        ArrayList<ResidentUsageModel> TotalUsage = new ArrayList<>();
        try {
            Object[] paras = {userEmail};
            outerResultSet = DBHelper.SelectPara(" uspMobGetPersonItemTotal ", paras);
            while (outerResultSet.next()) {
                ResidentUsageModel use = new ResidentUsageModel();
                use.ItemID = outerResultSet.getInt("ItemID");
                use.AmountUsed = outerResultSet.getFloat("AmountUsed");
                use.dfDate = outerResultSet.getString("dfDate");
                use.PersonID = outerResultSet.getInt("PersonID");
                use.ResTime = outerResultSet.getTime("AmountUsed");
                use.ResDate = outerResultSet.getDate("Date");
                TotalUsage.add(use);
            }

            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return TotalUsage;
    }
    public ArrayList<TipModel> GetTips() {
        ArrayList<TipModel> Tips = new ArrayList<>();
        try {
            outerResultSet = DBHelper.Select("{CALL uspMobGetTips}");
            while (outerResultSet.next()) {
                TipModel tip = new TipModel();
                tip.ID = outerResultSet.getInt("TTID");
                tip.PersonName = outerResultSet.getString("FullName");
                tip.CatID = outerResultSet.getInt("CatID");
                tip.PersonID = outerResultSet.getInt("PersonID");
                tip.DatePosted = outerResultSet.getDate("DatePosted");
                tip.FullName = outerResultSet.getString("FullName");
                tip.TipDescription = outerResultSet.getString("TTdescription");

                //item.ItemIcon = resultSet.getByte();
                Tips.add(tip);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Tips;
    }

    public ArrayList<TipModel> GetAdminTips() {
        ArrayList<TipModel> Tips = new ArrayList<>();
        try {
            outerResultSet = DBHelper.Select("{CALL uspMobAdminGetTips}");
            while (outerResultSet.next()) {
                TipModel tip = new TipModel();
                tip.ID = outerResultSet.getInt("TTID");
                tip.PersonName = "Adim";
                tip.CatID = outerResultSet.getInt("CatID");
                tip.PersonID = outerResultSet.getInt("PersonID");
                tip.DatePosted = outerResultSet.getDate("DatePosted");
                tip.FullName = outerResultSet.getString("FullName");
                tip.TipDescription = outerResultSet.getString("TTdescription");
                tip.Approved = outerResultSet.getBoolean("Approved");
                tip.Readed = outerResultSet.getBoolean("Readed");
                //item.ItemIcon = resultSet.getByte();
                Tips.add(tip);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Tips;
    }

    public boolean MobUpdatePerson(PersonModel person) {
        Object[] paras = {person.id, person.fullName, person.userPassword};
        boolean isWorking = DBHelper.NonQuery(" uspMobUpdatePerson ", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean MobAddPerson(PersonModel person) {
        Object[] paras = {person.fullName, person.email, person.userPassword};
        boolean isWorking = DBHelper.NonQuery(" uspMobAddPerson", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean Test(int personID, String image) {
        Object[] paras = {personID, image};
        boolean isWorking = DBHelper.NonQuery(" uspMobTest", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean MobAddTip(TipModel tip) {
        Object[] paras = {tip.PersonID, tip.TipDescription};
        boolean isWorking = DBHelper.NonQuery(" uspMobAddTip ", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean MobDeletePerson(String email) {
        Object[] paras = {email};
        boolean isWorking = DBHelper.NonQuery(" uspMobAddPerson", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean MobAddResidentUsage(ResidentUsageModel ResUsage) {
        Object[] paras = {ResUsage.PersonID, ResUsage.ResDate, ResUsage.ResTime, ResUsage.AmountUsed, ResUsage.ItemID};
        boolean isWorking = DBHelper.NonQuery(" uspMobAddResidentUsage", paras);
        DBHelper.Close();
        return isWorking;
    }

    public boolean MobAddLeak(ReportLeakModel leak) {
        Object[] paras = {leak.Latitude, leak.Longitude, leak.PersonID, leak.picPath, leak.Location};
        boolean isWorking = DBHelper.NonQuery(" uspMobAddLeak", paras);
        DBHelper.Close();
        return isWorking;
    }

    public TipModel GetRandomTips() {
        TipModel tip = new TipModel();
        try {
            outerResultSet = DBHelper.Select(" uspMobGetRandomTips");
            if(outerResultSet!=null) {
                outerResultSet.next();
                tip.ID = outerResultSet.getInt("TTID");
                tip.TipDescription = outerResultSet.getString("TTdescription");
                //item.ItemIcon = resultSet.getByte();
                DBHelper.Close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tip;
    }

    public ArrayList<ReportLeakModel> GetReportedLeaks(int personID) {

        ArrayList<ReportLeakModel> leaks = new ArrayList<>();
        try {
            Object[] para = {personID};
            outerResultSet = DBHelper.SelectPara(" uspMobGetLeaks ",para);
            DownLoadPicture DownPic = new DownLoadPicture("icon/leak101");
            Bitmap bitPic = DownPic.doInBackground();
            while (outerResultSet.next()) {
                ReportLeakModel leak = new ReportLeakModel();
                leak.Location = outerResultSet.getString("Location");
                leak.status = outerResultSet.getInt("StatusID");
                leak.date = outerResultSet.getDate("DateReported");
                leak.picPath = outerResultSet.getString("PicPath");
                leak.PersonID = outerResultSet.getInt("PersonID");
                if(leak.picPath!=null){
                     DownPic = new DownLoadPicture("icon/" +leak.picPath.trim());
                    leak.image = DownPic.doInBackground();
                }else {
                    leak.image = bitPic;
                }
                leaks.add(leak);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaks;
    }

    public boolean MobApproveTip(TipModel tip) {
        Object[] paras = {tip.ID, tip.Approved};
        boolean isWorking = DBHelper.NonQuery(" uspMobApproveTip ", paras);
        DBHelper.Close();
        return isWorking;
    }

    public ArrayList<ReportLeakModel> GetAdminReportedLeaks() {
        ArrayList<ReportLeakModel> leaks = new ArrayList<>();
        try {
            DownLoadPicture DownPic;
            DownPic = new DownLoadPicture("icon/leak101");
            Bitmap bitPic = DownPic.doInBackground();
            outerResultSet = DBHelper.Select("{CALL uspGetAdminReportedLeaks}");
            while (outerResultSet.next()) {
                ReportLeakModel leak = new ReportLeakModel();
                leak.Location = outerResultSet.getString("Location");
                //leak.Latitude = outerResultSet.getString("Latitude");
                //leak.Longitude = outerResultSet.getString("Longitude");
                leak.date = outerResultSet.getDate("DateReported");
                leak.status = outerResultSet.getInt("StatusID");
                leak.LeakID = outerResultSet.getInt("LeakID");
                leak.picPath = outerResultSet.getString("PicPath");
                if(leak.picPath!=null){
                     DownPic = new DownLoadPicture("icon/" +leak.picPath.trim());
                    leak.image = DownPic.doInBackground();
                }else {

                leak.image = bitPic;
            }

            //Double longitude = location.getLongitude();
                //Double latitude = location.getLatitude();


                //This section gets the address from the longitude and latitude.
                //geocoder = new Geocoder(getContext(), Locale.getDefault());

                //ddresses = geocoder.getFromLocation(latitude, longitude, 1);

                //String address = addresses.get(0).getAddressLine(0);

                //String fullAddress = address;

                leaks.add(leak);
            }
            DBHelper.Close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaks;
    }

    public boolean MobApproveLeak(ReportLeakModel leak) {
        Object[] paras = {leak.LeakID, leak.status};
        boolean isWorking = DBHelper.NonQuery(" uspMobApproveLeak ", paras);
        DBHelper.Close();
        return isWorking;
    }

    public Business GetBusiness() {
        Business business = new Business();

        return business;
    }

    public ArrayList<Surburb> WEBSuburbs() {
        ArrayList<Surburb> surburb = new ArrayList<>();
        try {

            outerResultSet = DBHelper.Select("uspWEBSuburbs");
            while (outerResultSet.next()) {

                Surburb B = new Surburb();
                B.ID = outerResultSet.getInt("SuburbID");
                B.SurburbName = outerResultSet.getString("SuburbName");
                surburb.add(B);
            }
        } catch (Exception e) {

        }

        return surburb;
    }

    public ArrayList<Surburb> WEBCities() {
        ArrayList<Surburb> surburb = new ArrayList<>();
        try {

            outerResultSet = DBHelper.Select("uspWEBCities");
            while (outerResultSet.next()) {

                Surburb B = new Surburb();
                B.ID = outerResultSet.getInt("CityID");
                B.SurburbName = outerResultSet.getString("CityName");
                surburb.add(B);
            }
        } catch (Exception e) {

        }

        return surburb;
    }
    public ArrayList<uspMobGetSnitches> MobGetSnitches(uspMobGetSnitches sni) {

        ArrayList<uspMobGetSnitches> snitches = new ArrayList<>();
        try {
            Object[] para = {sni.from,sni.to,sni.top};
            outerResultSet = DBHelper.SelectPara("uspMobGetSnitches",para);
            while (outerResultSet.next()) {

                uspMobGetSnitches snitch = new uspMobGetSnitches();
                snitch.TotalSnitches = outerResultSet.getInt("TotalSnitches");
                snitch.FullName = outerResultSet.getString("FullName");
                snitches.add(snitch);
            }
        } catch (Exception e) {

        }

        return snitches;
    }
    public ArrayList<uspMobGetSnitches> MobGetSnitches(){

        ArrayList<uspMobGetSnitches> snitches = new ArrayList<>();
        try {
            outerResultSet = DBHelper.Select("uspMobGetAllSnitches");
            while (outerResultSet.next()) {

                uspMobGetSnitches snitch = new uspMobGetSnitches();
                snitch.TotalSnitches = outerResultSet.getInt("TotalSnitches");
                snitch.FullName = outerResultSet.getString("FullName");
                snitches.add(snitch);
            }
        } catch (Exception e) {

        }

        return snitches;
    }
    public boolean WEBAddResident(String fullName, String email, String password,
                                  String houseNumber, String streetName, int suburID, int numberOfRes) {
        Object[] paras = {fullName, email, password,
                houseNumber, streetName, suburID, numberOfRes};
        boolean isWorking = DBHelper.NonQuery(" uspWEBAddResident ", paras);
        DBHelper.Close();
        return isWorking;
    }
}
