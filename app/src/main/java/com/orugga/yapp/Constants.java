package com.orugga.yapp;

/**
 * Created by Alexis on 18/10/2017.
 */

@SuppressWarnings("ALL")
public class Constants {
    public static class RequestPermission{
        public static final int REQUEST_PERMISSION_LOCATION = 0x00001;
        public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x00002;
    }

    public static class IntentFilters {
        public static final String FRAGMENT_MANAGEMENT = "fragment-management";
        public static final String OPERATION_OPEN_FRAGMENT = "fragment-target";
        public static final String OPERATION_OPEN_FRAGMENT_HOME = "fragment-target-home";
        public static final String OPERATION_OPEN_FRAGMENT_PROMOS = "fragment-target-promos";
        public static final String OPERATION_OPEN_FRAGMENT_BUSQUEDA_FARMACIA = "fragment-target-busqueda-farmacia";
        public static final String OPERATION_OPEN_FRAGMENT_BUSQUEDA_PRODUCTO = "fragment-target-busqueda-producto";
        public static final String OPERATION_OPEN_FRAGMENT_BUSQUEDA_PROGRAMA_PACIENTE = "fragment-target-busqueda-programa-paciente";
        public static final String OPERATION_OPEN_FRAGMENT_RESULTADOS_BUSQUEDA_PRODUCTO = "fragment-target-resultados-busqueda-producto";
        public static final int REQUEST_IMAGE_CAPTURE = 1;
        public static final int REQUEST_SELECT_IMAGE = 2;
        public static final int ACTION_SEE_RESULTS = 0x00002;
        public static final int ACTION_SEARCH = 0x00003;
        public static final int ACTION_UPDATE = 0x00004;
        public static final int ACTION_COMPLETE_INFO = 0x00009;
        public static final int ACTION_CREATE = 0x00005;
        public static final int ACTION_SELECT_PHARMACY = 0x00006;
        public static final int ACTION_SELECT_LOCATION = 0x00007;
        public static final int ACTION_SEE_PHARMACY = 0x00008;
    }

    public static class FragmentsNames {
        public static final String FRAGMENT_HOME = "fragment-home";
        public static final String FRAGMENT_PROMOS = "fragment-promos";
        public static final String FRAGMENT_BUSCAR_FARMACIA = "fragment-buscar-farmacia";
        public static final String FRAGMENT_BUSCAR_PRODUCTO = "fragment-buscar-producto";
        public static final String FRAGMENT_BUSCAR_PROGRAMA_PACIENTE = "fragment-buscar-programa-paciente";
        public static final String FRAGMENT_RESULTADOS_BUSQUEDA_PRODUCTO = "fragment-resultados-busqueda-producto";
        public static final String FRAGMENT_REGISTER_ACCOUNT = "fragment-register-account";
        public static final String FRAGMENT_LOGIN_OPTIONS = "fragment-login-options";
        public static final String FRAGMENT_WELCOME = "fragment-welcome";
        public static final String FRAGMENT_PROFILE = "fragment-profile";
        public static final String FRAGMENT_PROFILE_DEALS = "fragment-profile-deals";
        public static final String FRAGMENT_LOGIN = "fragment-login";
        public static final String FRAGMENT_DETALLE_RESULTADOS_BUSQUEDA_PRODUCTO = "fragment-detalle-resultado-busqueda-producto";
        public static final String FRAGMENT_DETALLE_PROGRAMA_PACIENTE = "fragment-detalle-programa-paciente";
        public static final String FRAGMENT_MIS_RECORDATORIOS_TOMA = "fragment-mis-recordatorios-toma";
        public static final String FRAGMENT_MIS_RECORDATORIOS_VISITA = "fragment-mis-recordatorios-visita";
        public static final String FRAGMENT_AGREGAR_RECORDATORIO_TOMA = "fragment-agregar-recordatorio-toma";
        public static final String FRAGMENT_AGREGAR_RECORDATORIO_VISITA = "fragment-agregar-recordatorio-visita";
        public static final String FRAGMENT_FAVORITOS = "fragment-favoritos";
        public static final String FRAGMENT_MIS_FARMACIAS_FAVORITAS = "fragment-mis-farmacias-favoritas";
        public static final String FRAGMENT_MIS_MEDICAMENTOS_FAVORITOS = "fragment-mis-medicamentos-favoritos";
        public static final String FRAGMENT_RECUPERAR_CONTRASEÑA = "fragment-recuperar-contraseña";
        public static final String FRAGMENT_ABOUT_US = "fragment-about-us";
        public static final String FRAGMENT_CAMBIAR_CONTRASEÑA = "fragment-cambiar-contraseña";
        public static final String FRAGMENT_TYC = "fragment-terms-and-conditions";
        public static final String FRAGMENT_COLABORA = "fragment-colabora";
        public static final String FRAGMENT_COLABORA_PRECIO = "fragment-colabora-precio";
        public static final String FRAGMENT_COLABORA_FARMACIA = "fragment-colabora-farmacia";
        public static final String FRAGMENT_COLABORA_CONVENIO = "fragment-colabora-convenio";
        public static final String FRAGMENT_COLABORA_PROMOCION = "fragment-colabora-promocio";
        public static final String FRAGMENT_COLABORA_OTRO = "fragment-colabora-otro";
        public static final String FRAGMENT_COLABORA_THANKS = "fragment-colabora-thanks";

        /**LUIS**/
        public static final String FRAGMENT_FARMACIA_ONLINE_WEB = "fragment-farmacia-web-online";
    }

    public static class Urls {
        public static final String SEARCH_PRODUCTS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/products?";
        public static final String SEARCH_PHARMACIES_BY_RADIUS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/pharmacies-by-radio?";
        public static final String SEARCH_PHARMACIES_BY_RADIUS_PRODUCTS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/pharmacies-by-radio-products-customer?";
        public static final String CREATE_USER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/register?";
        public static final String LOGIN_USER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/login?";
        public static final String GET_REGISTER_DATA_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/get-general-data";
        public static final String UPDATE_USER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/update-user?";
        public static final String SEARCH_PACIENT_PROGRAMS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/get-pacient-programs?";
        public static final String SEARCH_DETAIL_PACIENT_PROGRAMS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/get-detail-pacient-programs/";
        public static final String SEARCH_LABS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/labs?";
        public static final String SUBSCRIBE_TO_PACIENT_PROGRAM_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/program-pacient-subscription?";
        public static final String GET_PROMOTIONS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/get-promotions";
        public static final String ADD_TAKE_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/add-reminder-take?";
        public static final String ADD_VISIT_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/add-reminder-visit?";
        public static final String MY_TAKE_REMINDERS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/my-reminder-take?";
        public static final String MY_VISIT_REMINDERS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/my-reminder-visit?";
        public static final String DELETE_TAKE_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/delete-reminder-take?";
        public static final String DELETE_VISIT_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/delete-reminder-visit?";
        public static final String UPDATE_TAKE_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/update-reminder-take?";
        public static final String UPDATE_VISIT_REMINDER_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/update-reminder-visit?";
        public static final String MY_FAVOURITES_PHARMACIES_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/my-favorite-pharmacies?";
        public static final String ADD_FAVOURITE_PHARMACIE_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/add-favorite-pharmacies?";
        public static final String MY_FAVOURITES_PRODUCTS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/my-favorite-products?";
        public static final String ADD_FAVOURITE_PRODUCT_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/add-favorite-product?";
        public static final String RESTORE_PASSWORD_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/restore-password?";
        public static final String UPLOAD_IMAGE_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/upload-picture?";
        public static final String LOGIN_USER_WITH_FACEBOOK_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/login-with-facebook?";
        public static final String GET_BANNERS_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/get-banners";
        public static final String SEND_REPORTE_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/mail-quiz?";
        public static final String LOG_PACIENT_PROGRAM_DETAIL_URL = BuildConfig.BACKEND_URL_PREFIX + BuildConfig.BACKEND_URL_VERSION + "/pacient-programs-log";
        public static final String YAPP_COLABORA = "http://www.yapp.cl/colabora";
        public static final String YAPP_BIOEQUIVALENCIAS = "http://www.yapp.cl/m/bioequivalentes";
    }

    public static class ApiFields {
        //Product_Api
        public static final String PRODUCTS_ARRAY = "products";
        public static final String PRODUCT_ID = "id";
        public static final String PRODUCT_CATEGORY_ID = "product_categories_id";
        public static final String PRODUCT_ACTIVE_PRINCIPLES_ID = "active_principles_id";
        public static final String PRODUCT_LABS_ID = "labs_id";
        public static final String PRODUCT_SAP = "sap";
        public static final String PRODUCT_NAME = "name";
        public static final String PRODUCT_FULL_NAME = "full_name";
        public static final String PRODUCT_COMPOSITION = "composition";
        public static final String PRODUCT_PRESENTATION = "presentation";
        public static final String PRODUCT_PICTURE = "picture";
        public static final String PRODUCT_HAB = "hab";
        public static final String PRODUCT_CREATED_AT = "created_at";
        public static final String PRODUCT_UPDATED_AT = "updated_at";
        public static final String PRODUCT_CATEGORIES = "product_categories";
        public static final String PRODUCT_CATEGORIES_NAME = "name";
        public static final String PRODUCT_LABS = "labs";
        public static final String PRODUCT_LABS_NAME = "name";
        public static final String PRODUCT_ACTIVE_PRINCIPLES = "active_principles";
        public static final String PRODUCT_ACTIVE_PRINCIPLES_NAME = "name";

        //Pharmacy_api
        public static final String PHARMACY_ID = "id";
        public static final String PHARMACY_NAME = "name";
        public static final String PHARMACY_ADDRESS = "address";
        public static final String PHARMACY_PHONE_NUMBER = "number_phone";
        public static final String PHARMACY_OPEN_DH = "open_DH";
        public static final String PHARMACY_CLOSE_INTERMEDIATE_DH = "close_intermediate_DH";
        public static final String PHARMACY_REOPEN_DH = "reopen_DH";
        public static final String PHARMACY_CLOSE_DH = "close_DH";
        public static final String PHARMACY_OPEN_DS = "open_DS";
        public static final String PHARMACY_CLOSE_INTERMEDIATE_DS = "close_intermediate_DS";
        public static final String PHARMACY_REOPEN_DS = "reopen_DS";
        public static final String PHARMACY_CLOSE_DS = "close_DS";
        public static final String PHARMACY_OPEN_DD = "open_DD";
        public static final String PHARMACY_CLOSE_INTERMEDIATE_DD = "close_intermediate_DD";
        public static final String PHARMACY_REOPEN_DD = "reopen_DD";
        public static final String PHARMACY_CLOSE_DD = "close_DD";
        public static final String PHARMACY_REGIONS_ID = "regions_id";
        public static final String PHARMACY_COMUNNES_ID = "comunnes_id";
        public static final String PHARMACY_PHARMACYCHAIN_ID = "pharmacychains_id";
        public static final String PHARMACY_LATITUDE = "latitude";
        public static final String PHARMACY_LONGITUDE = "longitude";
        public static final String PHARMACY_SHIFT = "shift";
        public static final String PHARMACY_CLOSE_SUNDAY = "close_sunday";
        public static final String PHARMACY_ENABLE = "enable";
        public static final String PHARMACY_PICTURE = "picture";
        public static final String PHARMACY_CREATED_AT = "created_at";
        public static final String PHARMACY_UPDATED_AT = "updated_at";
        public static final String PHARMACY_STATUS = "status";
        public static final String PHARMACY_REGION = "region";
        public static final String PHARMACY_COMUNNE = "comunne";
        public static final String PHARMACY_COMUNNE_NAME = "name";
        public static final String PHARMACY_PHARMACYCHAIN = "pharmacychain";
        public static final String PHARMACY_PHARMACYCHAIN_NAME = "name";
        public static final String PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE = "total_price_products";
        public static final String PHARMACY_RESULT_TOTAL_PRODUCTS_PRICE_DISCOUNT = "total_price_products_discount";
        /** LUIS **/
        public static final String PHARMACY_RESULT_URL_WEB = "url";

    }

    public static class SharedPrefsKeys{
        public static final String SESSION = "__key_session";
        public static final String SESSION_ACCESS_TOKEN = "__session_access_token";
        public static final String SESSION_USER_INFO = "__session_user_info";
        public static final String SESSION_TERMS_ACCEPTED = "__session_terms_accepted";

        public static final String PREF_INITIAL_DATA = "__initial_data";
        public static final String PREF_SESSION = "__session";

        public static final String INFO = "__key_info";
        public static final String INFO_FIRST_TIME_IN_APP = "__key_info";

        public static final String BANNERS_LAST_UPDATE = "__banners_last_update";
    }

    public static class Alarms {
        public static final String ACTION_RECORDATORIO= "action-recordatorio";
        public static final String ACTION_RECORDATORIO_TOMA= "action-recordatorio-toma";
        public static final String ACTION_RECORDATORIO_RECOMPRA= "action-recordatorio-recompra";
        public static final String ACTION_RECORDATORIO_VISITA= "action-recordatorio-visita";
        public static final String NOTIFICATION_CHANELL_ID = "yapp-channel-id";

        public static final int REMINDER_ALARM = 0x000015;
        public static final int RECOMPRA_REQUEST_CODE_SALT = 0x0015164;
        public static final int VISITA_REQUEST_CODE_SALT = 0x0515164;
    }

    public static class REQUESTS_ACTIONS {
        public static String ACTION_ADD = "add";
        public static String ACTION_DELETE = "delete";
    }

    public static class ANSWERS_CONSTANTS {
        public static String CONTENT_TYPE_VIEW = "VIEW";

        public static String VIEW_FORGOT_PASSWORD_ID = "00001";
        public static String VIEW_BUSCAR_FARMACIAS_ID = "00002";
        public static String VIEW_BUSCAR_PRODUCTOS_ID = "00003";
        public static String VIEW_BUSCAR_PROGRAMA_PACIENTE_ID = "00004";
        public static String VIEW_BUSCAR_DESCUENTOS_Y_PROMOS_ID = "00005";
        public static String VIEW_DETALLE_RESULTADO_BUSQUEDA_PRODUCTO_ID = "00006";
        public static String VIEW_MENU_ID = "00007";
        public static String VIEW_MY_TAKES_REMINDERS_ID = "00008";
        public static String VIEW_MY_VISIT_REMINDERS_ID = "00009";
        public static String VIEW_MY_FAVOURITES_ID = "00010";
        public static String VIEW_MY_PROFILE_ID = "00011";
        public static String VIEW_ABOUT_US_ID = "00012";
    }

    public static class ReportesTypes {
        public static final int REPORTE_PRECIO = 0x0;
        public static final int REPORTE_PRODUCTO = 0x1;
        public static final int REPORTE_DESCUENTO_O_CONVENIO = 0x2;
        public static final int REPORTE_FARMACIA = 0x3;
        public static final int REPORTE_PROMOCION = 0x4;
        public static final int REPORTE_OTRO = 0x5;


    }

}
