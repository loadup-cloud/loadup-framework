package com.github.loadup.modules.upms.domain;

/*-
 * #%L
 * loadup-modules-upms-domain
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.github.loadup.commons.domain.BaseDomain;
import com.github.loadup.commons.util.ToStringUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author lise
 * @since 1.0.0
 */
@Getter
@Setter
public class ContactAddress extends BaseDomain implements Serializable {
    private String id;
    private String country;
    private String province;
    private String city;
    private String area;
    private String address1;
    private String address2;
    private String address3;
    private String zipcode;
    private String addressType;
    private boolean defaultAddress;
    private boolean verified;
    private String suburb;
    private String extendInfo;

    public boolean equals(Object o) {
        if (!(o instanceof ContactAddress)) {
            return false;
        }
        ContactAddress contactAddress = (ContactAddress) o;
        if (!StringUtils.equals(contactAddress.getAddressType(), this.addressType)) {
            return false;
        } else if (!StringUtils.equals(contactAddress.getAddress1(), this.address1)) {
            return false;
        } else if (!StringUtils.equals(contactAddress.getAddress2(), this.address2)) {
            return false;
        } else if (!StringUtils.equals(contactAddress.getCountry(), this.country)) {
            return false;
        } else if (!StringUtils.equals(contactAddress.getArea(), this.area)) {
            return false;
        } else if (!StringUtils.equals(contactAddress.getCity(), this.city)) {
            return false;
        } else {
            return !StringUtils.equals(contactAddress.getProvince(), this.province) ? false : StringUtils.equals(
                    contactAddress.getZipcode(), this.zipcode);
        }
    }

    @Override
    public String toString() {
        return ToStringUtils.reflectionToString(this);
    }
}
