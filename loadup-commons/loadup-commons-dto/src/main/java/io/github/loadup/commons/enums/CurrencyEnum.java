/*
 * #%L
 * LoadUp Common Money
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
package io.github.loadup.commons.enums;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/** JDK 25 ISO 4217 catalog snapshot, including historical and special-purpose codes. */
public enum CurrencyEnum {
    ADP("ADP", "020", "ADP"),
    AED("AED", "784", "AED"),
    AFA("AFA", "004", "AFA"),
    AFN("AFN", "971", "AFN"),
    ALL("ALL", "008", "ALL"),
    AMD("AMD", "051", "AMD"),
    ANG("ANG", "532", "ANG"),
    AOA("AOA", "973", "AOA"),
    ARS("ARS", "032", "ARS"),
    ATS("ATS", "040", "ATS"),
    AUD("AUD", "036", "A$"),
    AWG("AWG", "533", "AWG"),
    AYM("AYM", "945", "AYM"),
    AZM("AZM", "031", "AZM"),
    AZN("AZN", "944", "AZN"),
    BAM("BAM", "977", "BAM"),
    BBD("BBD", "052", "BBD"),
    BDT("BDT", "050", "BDT"),
    BEF("BEF", "056", "BEF"),
    BGL("BGL", "100", "BGL"),
    BGN("BGN", "975", "BGN"),
    BHD("BHD", "048", "BHD"),
    BIF("BIF", "108", "BIF"),
    BMD("BMD", "060", "BMD"),
    BND("BND", "096", "BND"),
    BOB("BOB", "068", "BOB"),
    BOV("BOV", "984", "BOV"),
    BRL("BRL", "986", "R$"),
    BSD("BSD", "044", "BSD"),
    BTN("BTN", "064", "BTN"),
    BWP("BWP", "072", "BWP"),
    BYB("BYB", "112", "BYB"),
    BYN("BYN", "933", "BYN"),
    BYR("BYR", "974", "BYR"),
    BZD("BZD", "084", "BZD"),
    CAD("CAD", "124", "CA$"),
    CDF("CDF", "976", "CDF"),
    CHE("CHE", "947", "CHE"),
    CHF("CHF", "756", "CHF"),
    CHW("CHW", "948", "CHW"),
    CLF("CLF", "990", "CLF"),
    CLP("CLP", "152", "CLP"),
    CNY("CNY", "156", "￥"),
    COP("COP", "170", "COP"),
    COU("COU", "970", "COU"),
    CRC("CRC", "188", "CRC"),
    CSD("CSD", "891", "CSD"),
    CUC("CUC", "931", "CUC"),
    CUP("CUP", "192", "CUP"),
    CVE("CVE", "132", "CVE"),
    CYP("CYP", "196", "CYP"),
    CZK("CZK", "203", "CZK"),
    DEM("DEM", "276", "DEM"),
    DJF("DJF", "262", "DJF"),
    DKK("DKK", "208", "DKK"),
    DOP("DOP", "214", "DOP"),
    DZD("DZD", "012", "DZD"),
    EEK("EEK", "233", "EEK"),
    EGP("EGP", "818", "EGP"),
    ERN("ERN", "232", "ERN"),
    ESP("ESP", "724", "ESP"),
    ETB("ETB", "230", "ETB"),
    EUR("EUR", "978", "€"),
    FIM("FIM", "246", "FIM"),
    FJD("FJD", "242", "FJD"),
    FKP("FKP", "238", "FKP"),
    FRF("FRF", "250", "FRF"),
    GBP("GBP", "826", "￡"),
    GEL("GEL", "981", "GEL"),
    GHC("GHC", "288", "GHC"),
    GHS("GHS", "936", "GHS"),
    GIP("GIP", "292", "GIP"),
    GMD("GMD", "270", "GMD"),
    GNF("GNF", "324", "GNF"),
    GRD("GRD", "300", "GRD"),
    GTQ("GTQ", "320", "GTQ"),
    GWP("GWP", "624", "GWP"),
    GYD("GYD", "328", "GYD"),
    HKD("HKD", "344", "HK$"),
    HNL("HNL", "340", "HNL"),
    HRK("HRK", "191", "HRK"),
    HTG("HTG", "332", "HTG"),
    HUF("HUF", "348", "HUF"),
    IDR("IDR", "360", "IDR"),
    IEP("IEP", "372", "IEP"),
    ILS("ILS", "376", "₪"),
    INR("INR", "356", "₹"),
    IQD("IQD", "368", "IQD"),
    IRR("IRR", "364", "IRR"),
    ISK("ISK", "352", "ISK"),
    ITL("ITL", "380", "ITL"),
    JMD("JMD", "388", "JMD"),
    JOD("JOD", "400", "JOD"),
    JPY("JPY", "392", "¥"),
    KES("KES", "404", "KES"),
    KGS("KGS", "417", "KGS"),
    KHR("KHR", "116", "KHR"),
    KMF("KMF", "174", "KMF"),
    KPW("KPW", "408", "KPW"),
    KRW("KRW", "410", "₩"),
    KWD("KWD", "414", "KWD"),
    KYD("KYD", "136", "KYD"),
    KZT("KZT", "398", "KZT"),
    LAK("LAK", "418", "LAK"),
    LBP("LBP", "422", "LBP"),
    LKR("LKR", "144", "LKR"),
    LRD("LRD", "430", "LRD"),
    LSL("LSL", "426", "LSL"),
    LTL("LTL", "440", "LTL"),
    LUF("LUF", "442", "LUF"),
    LVL("LVL", "428", "LVL"),
    LYD("LYD", "434", "LYD"),
    MAD("MAD", "504", "MAD"),
    MDL("MDL", "498", "MDL"),
    MGA("MGA", "969", "MGA"),
    MGF("MGF", "450", "MGF"),
    MKD("MKD", "807", "MKD"),
    MMK("MMK", "104", "MMK"),
    MNT("MNT", "496", "MNT"),
    MOP("MOP", "446", "MOP"),
    MRO("MRO", "478", "MRO"),
    MRU("MRU", "929", "MRU"),
    MTL("MTL", "470", "MTL"),
    MUR("MUR", "480", "MUR"),
    MVR("MVR", "462", "MVR"),
    MWK("MWK", "454", "MWK"),
    MXN("MXN", "484", "MX$"),
    MXV("MXV", "979", "MXV"),
    MYR("MYR", "458", "MYR"),
    MZM("MZM", "508", "MZM"),
    MZN("MZN", "943", "MZN"),
    NAD("NAD", "516", "NAD"),
    NGN("NGN", "566", "NGN"),
    NIO("NIO", "558", "NIO"),
    NLG("NLG", "528", "NLG"),
    NOK("NOK", "578", "NOK"),
    NPR("NPR", "524", "NPR"),
    NZD("NZD", "554", "NZ$"),
    OMR("OMR", "512", "OMR"),
    PAB("PAB", "590", "PAB"),
    PEN("PEN", "604", "PEN"),
    PGK("PGK", "598", "PGK"),
    PHP("PHP", "608", "₱"),
    PKR("PKR", "586", "PKR"),
    PLN("PLN", "985", "PLN"),
    PTE("PTE", "620", "PTE"),
    PYG("PYG", "600", "PYG"),
    QAR("QAR", "634", "QAR"),
    ROL("ROL", "642", "ROL"),
    RON("RON", "946", "RON"),
    RSD("RSD", "941", "RSD"),
    RUB("RUB", "643", "RUB"),
    RUR("RUR", "810", "RUR"),
    RWF("RWF", "646", "RWF"),
    SAR("SAR", "682", "SAR"),
    SBD("SBD", "090", "SBD"),
    SCR("SCR", "690", "SCR"),
    SDD("SDD", "736", "SDD"),
    SDG("SDG", "938", "SDG"),
    SEK("SEK", "752", "SEK"),
    SGD("SGD", "702", "SGD"),
    SHP("SHP", "654", "SHP"),
    SIT("SIT", "705", "SIT"),
    SKK("SKK", "703", "SKK"),
    SLE("SLE", "925", "SLE"),
    SLL("SLL", "694", "SLL"),
    SOS("SOS", "706", "SOS"),
    SRD("SRD", "968", "SRD"),
    SRG("SRG", "740", "SRG"),
    SSP("SSP", "728", "SSP"),
    STD("STD", "678", "STD"),
    STN("STN", "930", "STN"),
    SVC("SVC", "222", "SVC"),
    SYP("SYP", "760", "SYP"),
    SZL("SZL", "748", "SZL"),
    THB("THB", "764", "THB"),
    TJS("TJS", "972", "TJS"),
    TMM("TMM", "795", "TMM"),
    TMT("TMT", "934", "TMT"),
    TND("TND", "788", "TND"),
    TOP("TOP", "776", "TOP"),
    TPE("TPE", "626", "TPE"),
    TRL("TRL", "792", "TRL"),
    TRY("TRY", "949", "TRY"),
    TTD("TTD", "780", "TTD"),
    TWD("TWD", "901", "NT$"),
    TZS("TZS", "834", "TZS"),
    UAH("UAH", "980", "UAH"),
    UGX("UGX", "800", "UGX"),
    USD("USD", "840", "US $"),
    USN("USN", "997", "USN"),
    USS("USS", "998", "USS"),
    UYI("UYI", "940", "UYI"),
    UYU("UYU", "858", "UYU"),
    UZS("UZS", "860", "UZS"),
    VEB("VEB", "862", "VEB"),
    VED("VED", "926", "VED"),
    VEF("VEF", "937", "VEF"),
    VES("VES", "928", "VES"),
    VND("VND", "704", "₫"),
    VUV("VUV", "548", "VUV"),
    WST("WST", "882", "WST"),
    XAD("XAD", "396", "XAD"),
    XAF("XAF", "950", "FCFA"),
    XAG("XAG", "961", "XAG"),
    XAU("XAU", "959", "XAU"),
    XBA("XBA", "955", "XBA"),
    XBB("XBB", "956", "XBB"),
    XBC("XBC", "957", "XBC"),
    XBD("XBD", "958", "XBD"),
    XCD("XCD", "951", "EC$"),
    XCG("XCG", "532", "Cg."),
    XDR("XDR", "960", "XDR"),
    XFO("XFO", "000", "XFO"),
    XFU("XFU", "000", "XFU"),
    XOF("XOF", "952", "FCFA"),
    XPD("XPD", "964", "XPD"),
    XPF("XPF", "953", "CFPF"),
    XPT("XPT", "962", "XPT"),
    XSU("XSU", "994", "XSU"),
    XTS("XTS", "963", "XTS"),
    XUA("XUA", "965", "XUA"),
    XXX("XXX", "999", "¤"),
    YER("YER", "886", "YER"),
    YUM("YUM", "891", "YUM"),
    ZAR("ZAR", "710", "ZAR"),
    ZMK("ZMK", "894", "ZMK"),
    ZMW("ZMW", "967", "ZMW"),
    ZWD("ZWD", "716", "ZWD"),
    ZWG("ZWG", "924", "ZWG"),
    ZWL("ZWL", "932", "ZWL"),
    ZWN("ZWN", "942", "ZWN"),
    ZWR("ZWR", "935", "ZWR");

    private final String code;
    private final String numericCode;
    private final String symbol;

    CurrencyEnum(String code, String numericCode, String symbol) {
        this.code = code;
        this.numericCode = numericCode;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSymbol(Locale locale) {
        return toCurrency().getSymbol(Objects.requireNonNull(locale, "locale"));
    }

    public Currency toCurrency() {
        return Currency.getInstance(code);
    }

    public int getFractionDigits() {
        return toCurrency().getDefaultFractionDigits();
    }

    public static CurrencyEnum fromCode(String code) {
        return valueOf(Objects.requireNonNull(code, "currency code"));
    }
}
