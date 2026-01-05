package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

/**
 * Department Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Table("upms_department")
public class DepartmentDO extends BaseDO {

  @Id(keyType = KeyType.None)
  private String id;

  private String parentId;

  private String deptName;

  private String deptCode;

  private Integer deptLevel;

  private Integer sortOrder;

  private String leaderUserId;

  private String phone;

  private String email;

  private Short status;

  private String remark;

  /**
   * Getter method for property <tt>id</tt>.
   *
   * @return property value of id
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Setter method for property <tt>id</tt>.
   *
   * @param id value to be assigned to property id
   */
  @Override
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Getter method for property <tt>parentId</tt>.
   *
   * @return property value of parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Setter method for property <tt>parentId</tt>.
   *
   * @param parentId value to be assigned to property parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /**
   * Getter method for property <tt>deptName</tt>.
   *
   * @return property value of deptName
   */
  public String getDeptName() {
    return deptName;
  }

  /**
   * Setter method for property <tt>deptName</tt>.
   *
   * @param deptName value to be assigned to property deptName
   */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  /**
   * Getter method for property <tt>deptCode</tt>.
   *
   * @return property value of deptCode
   */
  public String getDeptCode() {
    return deptCode;
  }

  /**
   * Setter method for property <tt>deptCode</tt>.
   *
   * @param deptCode value to be assigned to property deptCode
   */
  public void setDeptCode(String deptCode) {
    this.deptCode = deptCode;
  }

  /**
   * Getter method for property <tt>deptLevel</tt>.
   *
   * @return property value of deptLevel
   */
  public Integer getDeptLevel() {
    return deptLevel;
  }

  /**
   * Setter method for property <tt>deptLevel</tt>.
   *
   * @param deptLevel value to be assigned to property deptLevel
   */
  public void setDeptLevel(Integer deptLevel) {
    this.deptLevel = deptLevel;
  }

  /**
   * Getter method for property <tt>sortOrder</tt>.
   *
   * @return property value of sortOrder
   */
  public Integer getSortOrder() {
    return sortOrder;
  }

  /**
   * Setter method for property <tt>sortOrder</tt>.
   *
   * @param sortOrder value to be assigned to property sortOrder
   */
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * Getter method for property <tt>leaderUserId</tt>.
   *
   * @return property value of leaderUserId
   */
  public String getLeaderUserId() {
    return leaderUserId;
  }

  /**
   * Setter method for property <tt>leaderUserId</tt>.
   *
   * @param leaderUserId value to be assigned to property leaderUserId
   */
  public void setLeaderUserId(String leaderUserId) {
    this.leaderUserId = leaderUserId;
  }

  /**
   * Getter method for property <tt>phone</tt>.
   *
   * @return property value of phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Setter method for property <tt>phone</tt>.
   *
   * @param phone value to be assigned to property phone
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * Getter method for property <tt>email</tt>.
   *
   * @return property value of email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Setter method for property <tt>email</tt>.
   *
   * @param email value to be assigned to property email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Getter method for property <tt>status</tt>.
   *
   * @return property value of status
   */
  public Short getStatus() {
    return status;
  }

  /**
   * Setter method for property <tt>status</tt>.
   *
   * @param status value to be assigned to property status
   */
  public void setStatus(Short status) {
    this.status = status;
  }

  /**
   * Getter method for property <tt>remark</tt>.
   *
   * @return property value of remark
   */
  public String getRemark() {
    return remark;
  }

  /**
   * Setter method for property <tt>remark</tt>.
   *
   * @param remark value to be assigned to property remark
   */
  public void setRemark(String remark) {
    this.remark = remark;
  }
}
