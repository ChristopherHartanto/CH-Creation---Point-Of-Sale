package com.chcreation.pointofsale

enum class ETable{
    USER,
    USER_LIST,
    PRODUCT,
    MERCHANT,
    AVAILABLE_MERCHANT,
    CUSTOMER,
    CAT,
    TRANSACTION,
    PAYMENT,
    ENQUIRY,
    STOCK_MOVEMENT,
    USER_ACCEPTANCE,
    ABOUT
}

enum class EAbout{
    TEXT1,
    TEXT2,
    TEXT3,
    IMAGE
}
enum class EProduct{
    NAME,
    DESC,
    PRICE,
    COST,
    MANAGE_STOCK,
    STOCK,
    PROD_KEY,
    PROD_CODE,
    UOM_CODE,
    IMAGE,
    CAT,
    CODE,
    STATUS_CODE,
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY
}

enum class EMerchant{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAME,
    BUSINESS_INFO,
    NO_TELP,
    CAT,
    IMAGE,
    USER_LIST,
    ADDRESS
}

enum class EAvailableMerchant{
    CREATED_DATE,
    UPDATED_DATE,
    STATUS,
    CREDENTIAL,
    USER_GROUP,
    NAME
}

enum class ECustomer{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    NOTE,
    CODE,
    IMAGE,
    STATUS_CODE
}

enum class ETransaction{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TOTAL_PRICE,
    TOTAL_OUTSTANDING,
    DISCOUNT,
    TAX,
    PAYMENT_METHOD,
    DETAIL,
    CUST_CODE,
    NOTE,
    TRANS_CODE,
    USER_CODE,
    STATUS_CODE
}

enum class E_Enquiry{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TRANS_KEY,
    CUST_CODE,
    PROD_KEY,
    PROD_CODE,
    MANAGE_STOCK,
    STOCK,
    STATUS_CODE
}

enum class EStock_Movement{
    CREATED_DATE,
    UPDATED_DATE,
    UPDATED_BY,
    TRANS_KEY,
    QTY,
    PROD_CODE,
    PROD_KEY,
    STATUS,
    STATUS_CODE,
    NOTE
}

enum class EPayment{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TOTAL_RECEIVED,
    PAYMENT_METHOD,
    NOTE,
    USER_CODE,
    STATUS_CODE
}

enum class EUser{
    CREATED_DATE,
    UPDATED_DATE,
    NAME,
    EMAIL,
    CODE
}

enum class EUserList{
    CREATED_DATE,
    UPDATED_DATE,
    NAME,
    USER_GROUP,
    USER_CODE,
    STATUS_CODE
}

enum class EUserAcceptance{
    CREATED_DATE,
    EMAIL,
    CREDENTIAL,
    NAME,
    USER_GROUP
}

enum class EMerchantUserList{
    CREATED_DATE,
    UPDATED_DATE,
    USER_CODE,
    STATUS_CODE
}

enum class EMessageResult{
    SUCCESS,
    UPDATE,
    DELETE_SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
    FETCH_AVAIL_MERCHANT_SUCCESS,
    FETCH_MERCHANT_SUCCESS,
    FETCH_CATEGORY_SUCCESS,
    FETCH_CUSTOMER_SUCCESS,
    FETCH_CUSTOMER_TRANSACTION_SUCCESS,
    FETCH_TRANS_SUCCESS,
    FETCH_TRANS_LIST_PAYMENT_SUCCESS,
    FETCH_STOCK_MOVEMENT_SUCCESS,
    FETCH_PEND_PAYMENT_SUCCESS,
    FETCH_USER_SUCCESS,
    FETCH_USER_LIST_SUCCESS,
    CREATE_INVITATION_SUCCESS,
    FETCH_INVITATION_SUCCESS
}

enum class EPaymentMethod{
    CASH,
    CARD
}

enum class EStatusCode{
    NEW,
    PENDING,
    DONE,
    CANCEL,
    DELETE,
    ACTIVE
}

enum class EStatusUser{
    ACTIVE,
    DE_ACTIVE
}

enum class EStatusStock{
    INBOUND,
    OUTBOUND,
    MISSING,
    CANCEL
}

enum class EUserGroup{
    MANAGER,
    WAITER
}

enum class ESharedPreference{
    MERCHANT,
    MERCHANT_CREDENTIAL,
    MERCHANT_IMAGE,
    USER_GROUP,
    NO_TELP,
    ADDRESS,
    NAME,
    EMAIL
}