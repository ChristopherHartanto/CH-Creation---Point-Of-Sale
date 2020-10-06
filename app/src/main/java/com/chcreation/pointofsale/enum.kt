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
    ABOUT,
    SINCERE,
    ACTIVITY_LOGS,
    VERSION
}

enum class ESincere{
    SINCERE
}

enum class EActivityLogs{
    LOG,
    CREATED_DATE,
    CREATED_BY
}

enum class EAbout{
    TEXT1,
    TEXT2,
    TEXT3,
    IMAGE
}

enum class ECategory{
    NAME,
    STATUS_CODE
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
    UPDATED_BY,
    WHOLE_SALE
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
    ADDRESS,
    MERCHANT_CODE,
    MEMBER_STATUS,
    ACTIVE,
    LANGUAGE,
    COUNTRY,
    MEMBER_DEADLINE,
    OPT_FIELD
}

enum class EAvailableMerchant{
    CREATED_DATE,
    UPDATED_DATE,
    STATUS,
    CREDENTIAL,
    USER_GROUP,
    NAME,
    MERCHANT_CODE
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
    STATUS_CODE,
    TABLE_NO,
    PEOPLE_NO,
    ORDER_NO,
    OPT_FIELD
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
    CODE,
    MEMBER_STATUS,
    ACTIVE,
    DEVICE_ID
}

enum class EUserMemberStatus{
    FREE_TRIAL,
    PREMIUM
}

enum class EMerchantMemberStatus{
    FREE_TRIAL,
    PREMIUM
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
    MERCHANT_CODE,
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
    FETCH_INVITATION_SUCCESS,
    FETCH_ACTIVITY_LOG_SUCCESS
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

enum class EStatusUser{ // use for user and merchant both
    ACTIVE,
    DE_ACTIVE,
    SUSPEND
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
    PRODUCT_VIEW,
    DEVICE_ID,
    MERCHANT_CODE,
    MERCHANT_NAME,
    MEMBER_DEADLINE,
    MERCHANT_CREDENTIAL,
    MERCHANT_IMAGE,
    USER_GROUP,
    NO_TELP,
    ADDRESS,
    NAME,
    COUNTRY,
    LANGUAGE,
    EMAIL,
    SINCERE,
    CUSTOMER_NAME,
    CUSTOMER_NO_TEL,
    CUSTOMER_ADDRESS,
    RECEIPT_MERCHANT_ICON,
    CUSTOM_RECEIPT,
    MERCHANT_MEMBER_STATUS,
    RECEIPT_NOTE,
    RECEIPT_DATE,
    RECEIPT_NO,
    PRINTER_DPI,
    PRINTER_WIDTH,
    PRINTER_CHAR_LINE,
    RECEIPT_TABLE_NO,
    RECEIPT_PEOPLE_NO,
    RECEIPT_ORDER_NO,
    RECEIPT_LAST_ORDER_NO_CREATED_DATE,
    RECEIPT_LAST_ORDER_NO
}

enum class ECustomReceipt{
    RECEIPT1,
    RECEIPT2
}

enum class ESort{
    PROD_NAME,
    PROD_CODE,
    PROD_PRICE,
    NEWEST
}

enum class EMonth(var value:Int){
    All(99),
    January(1),
    February(2),
    March(3),
    April(4),
    May(5),
    June(6),
    July(7),
    August(8),
    September(9),
    October(10),
    November(11),
    December(12)
}

enum class EProductView{
    LIST,
    GRID
}