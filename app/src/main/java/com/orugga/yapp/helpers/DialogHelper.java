package com.orugga.yapp.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;
import com.orugga.yapp.interfaces.ButtonReporteCallback;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.requests.DownloadImageRequest;

import static com.orugga.yapp.Constants.ApiFields.PHARMACY_ADDRESS;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_DD;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_DH;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_DS;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_INTERMEDIATE_DD;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_INTERMEDIATE_DH;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_CLOSE_INTERMEDIATE_DS;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LATITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_LONGITUDE;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_NAME;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_OPEN_DD;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_OPEN_DH;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_OPEN_DS;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHARMACYCHAIN;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_PHONE_NUMBER;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_REOPEN_DD;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_REOPEN_DH;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_REOPEN_DS;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_SHIFT;
import static com.orugga.yapp.Constants.ApiFields.PHARMACY_STATUS;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_ADD;
import static com.orugga.yapp.Constants.REQUESTS_ACTIONS.ACTION_DELETE;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_DESCUENTO_O_CONVENIO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_FARMACIA;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_OTRO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PRECIO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PRODUCTO;
import static com.orugga.yapp.Constants.ReportesTypes.REPORTE_PROMOCION;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.isUserLogedIn;
import static com.orugga.yapp.requests.FavouritesRequests.AddDeleteFavouritePharmacy;

/**
 * Created by Alexis on 31/10/2017.
 */

public class DialogHelper {
    public static void showPharmacyDetails(final Activity activity, final JsonObject pharmacyDetails) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.pharmacy_detail_alert_dialog, null);

        //inicializo la vista con los datos del Json
        ImageView pharmacyPhoto = view.findViewById(R.id.imgPharmacyPhoto);
        TextView pharmacyName = view.findViewById(R.id.txtPharmacyName);
        TextView pharmacyFlagStatus = view.findViewById(R.id.txtFlagPharmacyStatus);
        final CheckBox addPharmacyToFavourites = view.findViewById(R.id.btnAddPharmacyToFavourites);
        TextView pharmacyAddress = view.findViewById(R.id.txtPharmacyAddress);
        TextView pharmacyHorariosDeAtencion = view.findViewById(R.id.txtPharmacyHorariosDeAtencion);
        TextView pharmacyPhone = view.findViewById(R.id.txtPharmacyPhone);
        TextView btnIr = view.findViewById(R.id.btnPharmacyDescriptionIr);
        ImageView btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        JsonObject pharmacyChain = pharmacyDetails.get(PHARMACY_PHARMACYCHAIN).getAsJsonObject();
        DownloadImageRequest.downloadImage(activity, pharmacyChain.get("full_path_picture_800").getAsString(), pharmacyPhoto, R.drawable.placeholder_farmacia_detalle);

        pharmacyName.setText(pharmacyDetails.get(PHARMACY_NAME).getAsString());
        pharmacyAddress.setText(pharmacyDetails.get(PHARMACY_ADDRESS).getAsString());
        StringBuilder horariosAtencion = new StringBuilder();
        if (pharmacyDetails.get(PHARMACY_SHIFT).getAsInt() == 1) {
            pharmacyHorariosDeAtencion.setText(activity.getString(R.string.farmacia_abierta_24_hs));
        } else {
            view.findViewById(R.id.pharmacyEstaDeTurno).setVisibility(View.GONE);
            JsonElement openDH = pharmacyDetails.get(PHARMACY_OPEN_DH),
                    closeIntDH = pharmacyDetails.get(PHARMACY_CLOSE_INTERMEDIATE_DH),
                    reopenDH = pharmacyDetails.get(PHARMACY_REOPEN_DH),
                    closeDH = pharmacyDetails.get(PHARMACY_CLOSE_DH),
                    openDS = pharmacyDetails.get(PHARMACY_OPEN_DS),
                    closeIntDS = pharmacyDetails.get(PHARMACY_CLOSE_INTERMEDIATE_DS),
                    reopenDS = pharmacyDetails.get(PHARMACY_REOPEN_DS),
                    closeDS = pharmacyDetails.get(PHARMACY_CLOSE_DS),
                    openDD = pharmacyDetails.get(PHARMACY_OPEN_DD),
                    closeIntDD = pharmacyDetails.get(PHARMACY_CLOSE_INTERMEDIATE_DD),
                    reopenDD = pharmacyDetails.get(PHARMACY_REOPEN_DD),
                    closeDD = pharmacyDetails.get(PHARMACY_CLOSE_DD);
            horariosAtencion
                    .append(activity.getString(R.string.pharmacy_details_week_days)).append(" ")
                    .append(openDH.isJsonPrimitive() ? openDH.getAsString().substring(0, 5) : "N/A")
                    .append((closeIntDH.isJsonPrimitive() && reopenDH.isJsonPrimitive()) ?
                            " - " + closeIntDH.getAsString().substring(0, 5) + " / " + reopenDH.getAsString().substring(0, 5) : "").append(" - ")
                    .append(closeDH.isJsonPrimitive() ? closeDH.getAsString().substring(0, 5) : "N/A").append("\n")
                    .append(activity.getString(R.string.pharmacy_details_saturday)).append(" ");
            if (openDS.isJsonPrimitive() && closeDS.isJsonPrimitive()) {
                horariosAtencion.append(openDS.getAsString().substring(0, 5))
                        .append((closeIntDS.isJsonPrimitive() && reopenDS.isJsonPrimitive()) ?
                                " - " + closeIntDS.getAsString().substring(0, 5) + " / " + reopenDS.getAsString().substring(0, 5) : "").append(" - ")
                        .append(closeDS.getAsString().substring(0, 5));
            } else {
                horariosAtencion.append(activity.getString(R.string.pharmacy_details_status_closed));
            }
            horariosAtencion.append("\n")
                    .append(activity.getString(R.string.pharmacy_details_sunday)).append(" ");
            if (openDD.isJsonPrimitive() && closeDD.isJsonPrimitive()) {
                horariosAtencion.append(openDD.getAsString().substring(0, 5))
                        .append((closeIntDD.isJsonPrimitive() && reopenDD.isJsonPrimitive()) ?
                                " - " + closeIntDD.getAsString().substring(0, 5) + " / " + reopenDD.getAsString().substring(0, 5) : "").append(" - ")
                        .append(closeDD.getAsString().substring(0, 5));
            } else {
                horariosAtencion.append(activity.getString(R.string.pharmacy_details_status_closed));
            }
            pharmacyHorariosDeAtencion.setText(horariosAtencion);
        }
        JsonElement jsonPhoneNumer = pharmacyDetails.get(PHARMACY_PHONE_NUMBER);
        if (jsonPhoneNumer != JsonNull.INSTANCE && !jsonPhoneNumer.getAsString().equals("")) {
            pharmacyPhone.setText(jsonPhoneNumer.getAsString());
        } else {
            view.findViewById(R.id.layoutPharmacyPhone).setVisibility(View.GONE);
        }

        String status = pharmacyDetails.get(PHARMACY_STATUS).getAsString();
        switch (status) {
            case "open":
                pharmacyFlagStatus.setBackground(activity.getResources().getDrawable(R.drawable.flag_abierto));
                pharmacyFlagStatus.setText(R.string.pharmacy_flag_status_open);
                break;
            case "closed":
                pharmacyFlagStatus.setBackground(activity.getResources().getDrawable(R.drawable.flag_cerrado));
                pharmacyFlagStatus.setText(R.string.pharmacy_flag_status_closed);
                break;
            case "closing":
                pharmacyFlagStatus.setBackground(activity.getResources().getDrawable(R.drawable.flag_cierre_proximo));
                pharmacyFlagStatus.setText(R.string.pharmacy_flag_status_closing);
                break;
        }
        addPharmacyToFavourites.setChecked(pharmacyDetails.get("is_favorite").getAsBoolean());
        addPharmacyToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = addPharmacyToFavourites.isChecked();
                if (isUserLogedIn(activity)) {
                    if (isChecked) {
                        AddDeleteFavouritePharmacy(activity, getAccessToken(activity), pharmacyDetails.get("id").getAsLong(), ACTION_ADD, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(activity, R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                addPharmacyToFavourites.setChecked(false);
                            }
                        });
                    } else {
                        AddDeleteFavouritePharmacy(activity, getAccessToken(activity), pharmacyDetails.get("id").getAsLong(), ACTION_DELETE, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject response) {
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                Toast.makeText(activity, R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                addPharmacyToFavourites.setChecked(true);
                            }
                        });
                    }
                } else {
                    addPharmacyToFavourites.setChecked(false);
                    Toast.makeText(activity, R.string.must_be_loged_to_have_favs, Toast.LENGTH_SHORT).show();
                }
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        final AlertDialog dialog = builder.create();


        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Farmacias Abiertas Detalle - Ir"));
                dialog.dismiss();
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pharmacyDetails.get(PHARMACY_LATITUDE).getAsString() + "," + pharmacyDetails.get(PHARMACY_LONGITUDE).getAsString());

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    // Attempt to start an activity that can handle the Intent
                    activity.startActivity(mapIntent);
                } else {
                    Toast.makeText(activity, "Ups.. Necesitas tener Google Maps", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @SuppressWarnings("unused")
    public static Dialog showSubscriptionConfirmation(Activity activity) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.pacient_program_subscription_confirmation, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        //bindeo boton ir Dialog.dismiss
        view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        return dialog;
    }


    public static void showReportSelector(final Activity activity, String[] textoBotones, final int reporteType, final ButtonReporteCallback callback) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.colabora_dialog_selector, null, false);
        LinearLayout btnsLayout = view.findViewById(R.id.reporteListSelector);
        ImageView imgEsquina = view.findViewById(R.id.imgReporte);

        switch (reporteType) {
            case REPORTE_PRECIO:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_precio);
                break;
            case REPORTE_PRODUCTO:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_producto);
                break;
            case REPORTE_DESCUENTO_O_CONVENIO:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_convenio);
                break;
            case REPORTE_FARMACIA:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_farmacia);
                break;
            case REPORTE_PROMOCION:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_promocion);
                break;
            case REPORTE_OTRO:
                imgEsquina.setImageResource(R.drawable.esquina_reporta_otro);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(null);

        for (int i = 0; i < textoBotones.length; i++) {
            String btnText = textoBotones[i];
            TextView btnTextView = (TextView) activity.getLayoutInflater().inflate(R.layout.reporte_selector_item, btnsLayout, false);
            btnTextView.setText(btnText);
            btnTextView.setTag(i);
            btnTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClick((int) v.getTag());
                    dialog.dismiss();
                }
            });
            btnsLayout.addView(btnTextView);
        }

        dialog.setView(view);
        dialog.show();

    }
}
