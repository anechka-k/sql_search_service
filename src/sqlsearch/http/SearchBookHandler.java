package sqlsearch.http;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import sqlsearch.AppProperties;
import sqlsearch.util.Convert;

public final class SearchBookHandler extends APIServlet.APIRequestHandler
{
  static final SearchBookHandler instance = new SearchBookHandler();

  private SearchBookHandler()
  {
  }

  @Override
  JSONObject processRequest(HttpServletRequest req) throws JSONException
  {    
    String searchQuery = Convert.emptyToNull(req.getParameter("query"));
    JSONObject books = new JSONObject();
    
    try
    {
      books = searchInDB(searchQuery);
    }
    catch (SQLException e)
    {
      books.put("result", "error");
      books.put("error", e.getMessage());
      return books;
    }
    
    return books;
  }
  
  public JSONObject searchInDB(String query) throws SQLException, JSONException
  {
    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    JSONObject result = new JSONObject();
    
    try
    {
      con = DriverManager.getConnection(
        AppProperties.getStringProperty("mysqlUrlDB"),
        AppProperties.getStringProperty("mysqlLogin"),
        AppProperties.getStringProperty("mysqlPassword"));
      
      con.setAutoCommit(false);
      stmt = con.prepareStatement(
        "select * from flibusta.libbook where title LIKE ?");
      
      String titleSearch = "%" + query + "%";
      stmt.setString(1, titleSearch);
  
      rs = stmt.executeQuery();
      con.commit();
  
      boolean recordFound = rs.first();
      
      if(!recordFound)
      {
        result.put("result", "ok");
        result.put("library", "flibustaSQL");
        result.put("books", new JSONArray());
        return result;
      }
      
      JSONArray books = new JSONArray();
      
      while(true)
      {
        String bookId = rs.getString("bookId");
        String title = rs.getString("title");
        
        JSONObject book = new JSONObject();
        book.put("bookId", bookId);
        book.put("title", title);
        
        books.put(book);
        boolean nextRecord = rs.next();
        if(!nextRecord) break;
      }
      
      result.put("result", "ok");
      result.put("library", "flibustaSQL");
      result.put("books", books);
    }
    finally
    {
      try { if(con != null) con.close(); } catch(SQLException se) { }
      try { if(stmt != null)stmt.close(); } catch(SQLException se) { }
      try { if(rs != null) rs.close(); } catch(SQLException se) { }
    }
    
    return result;
  }
}
