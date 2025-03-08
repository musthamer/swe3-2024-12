package hbv.jdbcexample;

import java.util.*;
import java.sql.*;

public class JDBCMain {
  public static void main(String... args){
    System.out.println("running:"+JDBCMain.class);
    Properties conf = new Properties();
    conf.setProperty("user", System.getenv("mariadbuser"));
    conf.setProperty("password", System.getenv("mariadbpassword"));
    conf.setProperty("database", System.getenv("mariadbdatabase"));
    try (Connection con = DriverManager.getConnection("jdbc:mariadb://mysql-server:3306/",conf)){
      for(int i=0;i<10;++i){
        doQueriesWith(con);
      }
    } catch(SQLException e){ e.printStackTrace(); }
  }
  static void doQueriesWith(Connection con) throws SQLException {
    try (Statement stmt = con.createStatement()){
      stmt.execute("insert into demo (name) values ('jdbc')");
    } catch(SQLException e){ e.printStackTrace(); }

    try (PreparedStatement ps = con.prepareStatement("select * from demo where name=?")){
      ps.setString(1,"jdbc");
      ResultSet rs = ps.executeQuery();
      while (rs.next()){
        System.out.println(rs.getInt("id")+" "+rs.getString("name"));
      }
    } catch(SQLException e){ e.printStackTrace(); }
  }

}
