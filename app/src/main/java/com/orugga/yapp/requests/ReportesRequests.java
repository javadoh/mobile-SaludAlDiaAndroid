package com.orugga.yapp.requests;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.orugga.yapp.interfaces.JsonObjectResponse;

import java.io.File;

import static com.orugga.yapp.Constants.Urls.SEND_REPORTE_URL;

public class ReportesRequests {

    public static void sendReporteProducto(Context context, String apiToken, String categoria, String subCategoria, String productName,
                                           String laboratory, String grammage, String cant, String pharmacyChain, String price, String correction,
                                           String pharmacy, boolean withoutStock, File image, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("product_name", productName.isEmpty() ? null : productName);
        bodyObject.addProperty("laboratory", laboratory.isEmpty() ? null : laboratory);
        bodyObject.addProperty("grammage", grammage.isEmpty() ? null : grammage);
        bodyObject.addProperty("cant", cant.isEmpty() ? null : cant);
        bodyObject.addProperty("pharmacy_chain", pharmacyChain.isEmpty() ? null : pharmacyChain);
        bodyObject.addProperty("price", price.isEmpty() ? null : price);
        bodyObject.addProperty("correction", correction.isEmpty() ? null : correction);
        bodyObject.addProperty("pharmacy", pharmacy.isEmpty() ? null : pharmacy);
        bodyObject.addProperty("without_stock", withoutStock);
        body.add(bodyObject);
        Builders.Any.M loaderBuilder = Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString());
        if (image != null)
            loaderBuilder.setMultipartFile("image", image);
        loaderBuilder
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }

    public static void sendReportePrecio(Context context, String apiToken, String categoria, String subCategoria, String productName,
                                         String pharmacyChain, String deal, String isapre, String cant, String pricePaid, String price,
                                         String discount, String grammage, String laboratory, File imageBallot, File image, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("product_name", productName.isEmpty() ? null : productName);
        bodyObject.addProperty("pharmacy_chain", pharmacyChain.isEmpty() ? null : pharmacyChain);
        bodyObject.addProperty("price_paid", pricePaid.isEmpty() ? null : pricePaid);
        bodyObject.addProperty("deal", deal.isEmpty() ? null : deal);
        bodyObject.addProperty("isapre", isapre.isEmpty() ? null : isapre);
        bodyObject.addProperty("laboratory", laboratory.isEmpty() ? null : laboratory);
        bodyObject.addProperty("cant", cant.isEmpty() ? null : cant);
        bodyObject.addProperty("grammage", grammage.isEmpty() ? null : grammage);
        bodyObject.addProperty("price", price.isEmpty() ? null : price);
        bodyObject.addProperty("discount", discount.isEmpty() ? null : discount);
        body.add(bodyObject);
        Builders.Any.M loaderBuilder = Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString());
        if (image != null)
            loaderBuilder.setMultipartFile("image", image);
        if (imageBallot != null)
            loaderBuilder.setMultipartFile("image_ballot", imageBallot);
        loaderBuilder
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }

    public static void sendReporteFarmacia(Context context, String apiToken, String categoria, String subCategoria, String pharmacyId,
                                           String description, String pharmacyChain, String closed, String star,
                                           String latitude, String longitude, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("pharmacy_id", pharmacyId.isEmpty() ? null : pharmacyId);
        bodyObject.addProperty("description", description.isEmpty() ? null : description);
        bodyObject.addProperty("chain_pharmacy", pharmacyChain.isEmpty() ? null : pharmacyChain);
        bodyObject.addProperty("closed", closed.isEmpty() ? null : closed);
        bodyObject.addProperty("star", star.isEmpty() ? null : star);
        bodyObject.addProperty("latitude", latitude.isEmpty() ? null : latitude);
        bodyObject.addProperty("longitude", longitude.isEmpty() ? null : longitude);
        body.add(bodyObject);
        Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }

    public static void sendReporteConvenio(Context context, String apiToken, String categoria, String subCategoria, String convenio,
                                           String descripcion, String sitioWeb, File image, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("deal", convenio.isEmpty() ? null : convenio);
        bodyObject.addProperty("description", descripcion.isEmpty() ? null : descripcion);
        bodyObject.addProperty("site_web", sitioWeb.isEmpty() ? null : sitioWeb);
        body.add(bodyObject);
        Builders.Any.M loaderBuilder = Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString());
        if (image != null)
            loaderBuilder.setMultipartFile("image", image);
        loaderBuilder
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }

    public static void sendReportePromocion(Context context, String apiToken, String categoria, String subCategoria, String pharmacyChain,
                                            String descripcion, String link, File image, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("pharmacy_chain", pharmacyChain.isEmpty() ? null : pharmacyChain);
        bodyObject.addProperty("description", descripcion.isEmpty() ? null : descripcion);
        bodyObject.addProperty("link", link.isEmpty() ? null : link);
        body.add(bodyObject);
        Builders.Any.M loaderBuilder = Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString());
        if (image != null)
            loaderBuilder.setMultipartFile("image", image);
        loaderBuilder
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }

    public static void sendReporteOtros(Context context, String apiToken, String categoria, String subCategoria, String asunto,
                                        String email, String description, final JsonObjectResponse callback) {
        StringBuilder url = new StringBuilder(SEND_REPORTE_URL);
        if (apiToken != null)
            url.append("api_token=").append(apiToken);
        url.append("&category=").append(categoria)
                .append("&subcategory=").append(subCategoria);
        JsonArray body = new JsonArray();
        JsonObject bodyObject = new JsonObject();
        bodyObject.addProperty("subject_email", asunto.isEmpty() ? null : asunto);
        bodyObject.addProperty("email", email.isEmpty() ? null : email);
        bodyObject.addProperty("description", description.isEmpty() ? null : description);
        body.add(bodyObject);
        Ion.with(context)
                .load(url.toString())
                .setMultipartParameter("data", body.toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onError(null, e);
                        } else if (!result.get("rta").getAsString().equals("OK")) {
                            callback.onError(result, null);
                        } else {
                            callback.onSuccess(result.getAsJsonObject("data"));
                        }
                    }
                });
    }
}
