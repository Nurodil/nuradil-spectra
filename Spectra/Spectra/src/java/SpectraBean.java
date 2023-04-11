/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author PELIN
 */
@ManagedBean(name = "Spectra")
@SessionScoped
public class SpectraBean {

    private String username;
    private String password;
    private String blogPost;
    private String blogPostWillBeEdited;
    private String blogPostId;
    private String userMessage;
    private String name;
    private String surname;
    private String eMail;
    private Boolean isLoggedIn = false;

    public void setIsLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public Boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public void setEmail(String eMail) {
        this.eMail = eMail;
    }

    public String getEmail() {
        return eMail;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setBlogPost(String blogPost) {
        this.blogPost = blogPost;
    }

    public String getBlogPost() {
        return blogPost;
    }

    public void setBlogPostWillBeEdited(String blogPostWillBeEdited) {
        this.blogPostWillBeEdited = blogPostWillBeEdited;
    }

    public String getBlogPostWillBeEdited() {
        return blogPostWillBeEdited;
    }

    public void setBlogPostId(String blogPostId) {
        this.blogPostId = blogPostId;
    }

    public String getBlogPostId() {
        return blogPostId;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    CachedRowSet rowSet = null;

    DataSource dataSource;

    public SpectraBean() {
        try {
            Context ctx = new InitialContext();
            // sample databaseine bağlanıyoruz
            dataSource = (DataSource) ctx.lookup("jdbc/sample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void register() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Unable to obtain DataSource");
        }

        Connection connection = dataSource.getConnection();

        if (connection == null) {
            throw new SQLException("Unable to connect to DataSource");
        }
        try {
            PreparedStatement ps;

            ps = connection.prepareStatement("insert into MembershipUser (MembershipUser.Username,MembershipUser.Password) values (?,?)");
            ps.setString(1, getUsername());
            ps.setString(2, getPassword());

            ps.executeUpdate();
        } finally {
            connection.close();
        }
    }

    public void redirect(String url) {
        try {

            FacesContext.getCurrentInstance().getExternalContext().redirect(url);

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void redirectToLoginPage() {
        try {

            FacesContext.getCurrentInstance().getExternalContext().redirect("Login.xhtml");

            setIsLoggedIn(false);

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void saveBlogPost() throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            PreparedStatement ps;

            ps = connection.prepareStatement("insert into Blogs (Blogs.BlogPost, Blogs.CreatedBy, Blogs.CreateDate, Blogs.IsDeleted) values (?,?,?,0)");
            ps.setString(1, getBlogPost());
            ps.setString(2, getUsername());
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

    public void saveUserMessages() throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            PreparedStatement ps;

            ps = connection.prepareStatement("insert into UserMessages "
                    + "(UserMessages.Message, UserMessages.Name, "
                    + "UserMessages.Surname, UserMessages.Email,"
                    + "UserMessages.CreateDate, UserMessages.IsDeleted) "
                    + "values (?,?,?,?,?,0)");
            ps.setString(1, getUserMessage());
            ps.setString(2, getName());
            ps.setString(3, getSurname());
            ps.setString(4, getEmail());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

    public ResultSet fetchUserMessages() throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement ps;
        try {
            if (this.isLoggedIn) {
                ps = connection.prepareStatement("select UserMessages.Id, UserMessages.Message, UserMessages.Name, UserMessages.Surname, UserMessages.Email,UserMessages.CreateDate from UserMessages where UserMessages.IsDeleted = 0 ");

                rowSet = new com.sun.rowset.CachedRowSetImpl();
                rowSet.populate(ps.executeQuery());
            } else {
                redirect("Login.xhtml");
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
            return rowSet;
        }

    }

    public ResultSet fetchBlogPosts() throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement ps;
        try {
            if (this.isLoggedIn) {

                ps = connection.prepareStatement("select Blogs.Id, Blogs.BlogPost, Blogs.CreateDate, Blogs.UpdateDate, Blogs.CreatedBy from Blogs where Blogs.CreatedBy = ? "
                        + "and Blogs.IsDeleted = 0");
                ps.setString(1, getUsername());

                rowSet = new com.sun.rowset.CachedRowSetImpl();
                rowSet.populate(ps.executeQuery());
            } else {
                redirect("Login.xhtml");
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
            return rowSet;
        }

    }

    public ResultSet fetchBlogPostsForUsers() throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement ps;
        try {

            ps = connection.prepareStatement("select Blogs.BlogPost,Blogs.CreateDate,Blogs.CreatedBy from Blogs where Blogs.IsDeleted = 0 ");

            rowSet = new com.sun.rowset.CachedRowSetImpl();
            rowSet.populate(ps.executeQuery());
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
            return rowSet;
        }

    }

    public void deleteBlogPost(String blogId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps;

        int BlogId = Integer.parseInt(blogId);

        try {

            ps = connection.prepareStatement("Update Blogs set IsDeleted = 1 where Blogs.CreatedBy = ? "
                    + "and Blogs.Id = ? "
                    + "and Blogs.IsDeleted = 0");
            ps.setString(1, getUsername());
            ps.setInt(2, BlogId);

            ps.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

    public void deleteUserMessage(String blogId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps;

        int BlogId = Integer.parseInt(blogId);

        try {

            ps = connection.prepareStatement("Update UserMessages set IsDeleted = 1 where UserMessages.Id = ? "
                    + "and UserMessages.IsDeleted = 0");
            ps.setInt(1, BlogId);

            ps.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

    public void editBlogPosts(String blogId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps;

        int BlogId = Integer.parseInt(blogId);

        try {

            ps = connection.prepareStatement("Update Blogs set Blogs.BlogPost = ?, Blogs.UpdatedBy = ?, Blogs.UpdateDate = ? where Blogs.Id = ? ");
            ps.setString(1, getBlogPostWillBeEdited());
            ps.setString(2, getUsername());
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, BlogId);
            ps.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

    public void navigateToEditBlogPost(String blogId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps;
        String url = "EditBlog.xhtml";
        int BlogId = Integer.parseInt(blogId);
        setBlogPostId(blogId);
        ps = connection.prepareStatement("select Blogs.BlogPost from Blogs where Blogs.Id = ? ");
        ps.setInt(1, BlogId);

        rowSet = new com.sun.rowset.CachedRowSetImpl();
        rowSet.populate(ps.executeQuery());
        while (rowSet.next()) {
            //Retrieve by column name
            String BlogPost = rowSet.getString("BlogPost");
            setBlogPostWillBeEdited(BlogPost);
        }
        redirect(url);
    }
    
        public void navigateToShowBlogPost(String blogId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps;
        String url = "ShowBlog.xhtml";
        int BlogId = Integer.parseInt(blogId);
        setBlogPostId(blogId);
        ps = connection.prepareStatement("select Blogs.BlogPost from Blogs where Blogs.Id = ? ");
        ps.setInt(1, BlogId);

        rowSet = new com.sun.rowset.CachedRowSetImpl();
        rowSet.populate(ps.executeQuery());
        while (rowSet.next()) {
            //Retrieve by column name
            String BlogPost = rowSet.getString("BlogPost");
            setBlogPostWillBeEdited(BlogPost);
        }
        redirect(url);
    }

    public void login() throws SQLException {

        if (dataSource == null) {
            throw new SQLException("Unable to obtain DataSource");
        }

        Connection connection = dataSource.getConnection();

        if (connection == null) {
            throw new SQLException("Unable to connect to DataSource");
        }
        PreparedStatement ps;
        try {

            if (getUsername() != null && !"".equals(getUsername()) && getPassword() != null && !"".equals(getPassword())) {
                ps = connection.prepareStatement("select * from MembershipUser where MembershipUser.Username = ? "
                        + "and MembershipUser.Password = ? ");
                ps.setString(1, getUsername());
                ps.setString(2, getPassword());

                rowSet = new com.sun.rowset.CachedRowSetImpl();
                rowSet.populate(ps.executeQuery());
                if (rowSet.first()) {
                    String url = "ListBlogs.xhtml";
                    setIsLoggedIn(true);
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                } else {
                    String url = "Login.xhtml";
                    setIsLoggedIn(false);
                    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
                }

            } else {
                String url = "Login.xhtml";
                setIsLoggedIn(false);
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            }

        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            connection.close();
        }
    }

}
