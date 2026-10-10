/*
 * #%L
 * LoadUp Merchant
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.merchant.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

@Table("merchant_profile")
public class MerchantDO extends BaseDO {
    private String merchantCode;

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String value) {
        merchantCode = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    private String shortName;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String value) {
        shortName = value;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
    }

    private String industry;

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String value) {
        industry = value;
    }

    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        country = value;
    }

    private String province;

    public String getProvince() {
        return province;
    }

    public void setProvince(String value) {
        province = value;
    }

    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String value) {
        city = value;
    }

    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        address = value;
    }

    private String registrationNo;

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String value) {
        registrationNo = value;
    }

    private String contactName;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String value) {
        contactName = value;
    }

    private String contactPhone;

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String value) {
        contactPhone = value;
    }

    private String contactEmail;

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String value) {
        contactEmail = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private Long rowVersion;

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long value) {
        rowVersion = value;
    }

    private String createdBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String value) {
        createdBy = value;
    }

    private String updatedBy;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String value) {
        updatedBy = value;
    }

    @Override
    public String toString() {
        return "MerchantDO[id=" + getId() + "]";
    }
}
