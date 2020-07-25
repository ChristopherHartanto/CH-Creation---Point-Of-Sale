package com.chcreation.pointofsale

enum class ETable{
    USER,
    PRODUCT,
    MERCHANT,
    CUSTOMER,
    CAT,
    TRANSACTION
}

enum class EProduct{
    NAME,
    DESC,
    PRICE,
    COST,
    STOCK,
    PROD_CODE,
    UOM_CODE,
    IMAGE,
    CAT,
    CODE
}

enum class EMerchant{
    CREATED_DATE,
    NAME,
    CAT
}

enum class ECustomer{
    CREATED_DATE,
    UPDATED_DATE,
    NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    NOTE,
    CODE
}

enum class ETransaction{
    CREATED_DATE,
    TOTAL_PRICE,
    TOTAL_OUTSTANDING,
    TOTAL_RECEIVED,
    DISCOUNT,
    PAYMENT_METHOD,
    DETAIL,
    CUST_CODE,
    NOTE,
    TRANS_CODE,
    USER_CODE
}

enum class EMessageResult{
    SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
    FETCH_MERCHANT_SUCCESS,
    FETCH_CATEGORY_SUCCESS,
    FETCH_CUSTOMER_SUCCESS,
    FETCH_TRANS_SUCCESS
}

enum class EPaymentMethod{
    CASH,
    CARD
}