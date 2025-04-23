/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.utils.model;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;


/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ConnectionModel.java, v 0.1 2016年5月7日 下午1:57:18 tianzhu.wtzh Exp $
 */
public class ConnectionModel {

    /**jdbc userName*/
    private String              username;

    /**jdbc password*/
    private String              password;

    /**jdbc url*/
    private String              url;

    /**oracle driver*/
    private Driver              driver           ;
    //= new OracleDriver();

    /**schema info*/
    private String              schema;

    /**connect info*/
    private Connection          connection;

    /**mysql prefix*/
    private static final String MYSQL_URL_PREFIX  = "jdbc:mysql://";

    /**
     * 
     * @param url
     * @param username
     * @param password
     */
    public ConnectionModel(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schema = username;
        if (url.startsWith(MYSQL_URL_PREFIX)) {

            try {
                this.driver = new com.mysql.jdbc.Driver();
            } catch (Exception e) {
                // logger.error("",e);
                this.driver = null;
            }

        }
    }

    /**
     * Getter method for property <tt>username</tt>.
     * 
     * @return property value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter method for property <tt>username</tt>.
     * 
     * @param username value to be assigned to property username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter method for property <tt>password</tt>.
     * 
     * @return property value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter method for property <tt>password</tt>.
     * 
     * @param password value to be assigned to property password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter method for property <tt>url</tt>.
     * 
     * @return property value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter method for property <tt>url</tt>.
     * 
     * @param url value to be assigned to property url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter method for property <tt>driver</tt>.
     * 
     * @return property value of driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Setter method for property <tt>driver</tt>.
     * 
     * @param driver value to be assigned to property driver
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * Getter method for property <tt>schema</tt>.
     * 
     * @return property value of schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Setter method for property <tt>schema</tt>.
     * 
     * @param schema value to be assigned to property schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Getter method for property <tt>connection</tt>.
     * 
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Setter method for property <tt>connection</tt>.
     * 
     * @param connection value to be assigned to property connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }


}
