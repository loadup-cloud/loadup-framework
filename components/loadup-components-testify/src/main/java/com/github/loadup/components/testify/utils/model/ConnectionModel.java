
package com.github.loadup.components.testify.utils.model;

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


/**
 * 
 * 
 */
public class ConnectionModel {

    /**
     * mysql prefix
     */
    private static final String MYSQL_URL_PREFIX = "jdbc:mysql://";
    /**
     * jdbc userName
     */
    private String username;
    /**
     * jdbc password
     */
    private String password;
    /**
     * jdbc url
     */
    private String url;
    //= new OracleDriver();
    /**
     * oracle driver
     */
    private Driver driver;
    /**
     * schema info
     */
    private String schema;
    /**
     * connect info
     */
    private Connection connection;

    /**
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
     * 
     *
     * @return property value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     *
     * @param username value to be assigned to property username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     *
     * @return property value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     *
     * @param password value to be assigned to property password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     *
     * @return property value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     *
     * @param url value to be assigned to property url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     *
     * @return property value of driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     *
     *
     * @param driver value to be assigned to property driver
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * 
     *
     * @return property value of schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     *
     *
     * @param schema value to be assigned to property schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 
     *
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     *
     *
     * @param connection value to be assigned to property connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }


}
