package com.chcreation.pointofsale

enum class ETable{
    USER,
    PRODUCT,
    MERCHANT,
    CUSTOMER,
    CAT
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
    CAT
}

enum class EMerchant{
    CREATED_DATE,
    NAME,
    CAT
}

enum class EMessageResult{
    SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
    FETCH_MERCHANT_SUCCESS,
    FETCH_CATEGORY_SUCCESS
}