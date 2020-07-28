package com.chcreation.pointofsale

enum class ETable{
    USER,
    PRODUCT,
    MERCHANT,
    AVAILABLE_MERCHANT,
    CUSTOMER,
    CAT,
    TRANSACTION,
    PAYMENT,
    ENQUIRY
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
    CAT
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
    CODE
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

enum class E_Enqury{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TRANS_CODE,
    CUST_CODE,
    PRODUCT_KEY,
    MANAGE_STOCK,
    STOCK,
    STATUS_CODE
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

enum class EMessageResult{
    SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
    FETCH_AVAIL_MERCHANT_SUCCESS,
    FETCH_MERCHANT_SUCCESS,
    FETCH_CATEGORY_SUCCESS,
    FETCH_CUSTOMER_SUCCESS,
    FETCH_TRANS_SUCCESS,
    FETCH_TRANS_LIST_PAYMENT_SUCCESS,
    FETCH_PEND_PAYMENT_SUCCESS
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
}

enum class EStatusUser{
    ACTIVE,
    DE_ACTIVE
}

enum class EUserGroup{
    MANAGER,
    WAITER
}

enum class ESharedPreference{
    MERCHANT,
    MERCHANT_CREDENTIAL,
    USER_GROUP,
    NO_TELP,
    ADDRESS
}