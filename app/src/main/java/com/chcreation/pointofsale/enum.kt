package com.chcreation.pointofsale

enum class ETable{
    USER,
    PRODUCT,
    MERCHANT,
    CUSTOMER,
    CAT,
    TRANSACTION,
    PAYMENT
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
    DISCOUNT,
    TAX,
    PAYMENT_METHOD,
    DETAIL,
    CUST_CODE,
    NOTE,
    TRANS_CODE,
    USER_CODE
}

enum class EPayment{
    CREATED_DATE,
    TOTAL_RECEIVED,
    PAYMENT_METHOD,
    NOTE,
    USER_CODE
}

enum class EMessageResult{
    SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
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