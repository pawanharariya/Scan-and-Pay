package com.anvay.noqueuepaydashboard.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    public static final String SHARED_PREF = "app",
            STORE_ZIPCODE = "zipcode",
            STORE_ID = "storeId",
            STORE_IMAGE_URL = "imageUrl",
            STORE_EXTRA_INFO = "storeExtraInfo",
            STORE_NAME = "storeName",
            STORE_ADDRESS = "address",
            STORE_UPI_ID = "storeUpiId",
            MOBILE_NUMBER = "mobileNumber",
            STORE_EMAIL = "email",
            LATITUDE = "latitude",
            LONGITUDE = "longitude",
            IS_LOGGED_IN = "isLoggedIn",
            IS_SCAN_ONLY_MODE = "isScanOnlyMode",
            IS_PROFILE_DONE = "isProfileDone",
            KEY_ORDER_DOC_ID = "orderDocId";

    public static final String
            BASE_STORES_URL = "stores/",
            BASE_ORDER_URL = "orders/",
            BASE_URL_COMPLAINTS = "complaints/stores/storeComplaints",
            KEY_STORE_ID = "storeId",
            KEY_IS_VERIFIED = "verified";

    public static boolean validateUPI(String upi) {
        final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^(.+)@(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(upi);
        return matcher.find();
    }
}
