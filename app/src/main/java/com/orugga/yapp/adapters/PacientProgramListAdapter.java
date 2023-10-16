package com.orugga.yapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orugga.yapp.R;

import java.text.NumberFormat;
import java.util.Locale;

import static com.orugga.yapp.requests.DownloadImageRequest.downloadImage;

/**
 * Created by Alexis on 05/12/2017.
 */

public class PacientProgramListAdapter extends ArrayAdapter<JsonObject> {

    private JsonArray mPacientPrograms;

    public PacientProgramListAdapter(@NonNull Context context, JsonArray pacientPrograms) {
        super(context, R.layout.pacient_program_item);
        this.mPacientPrograms = pacientPrograms;
    }

    public void setNewContent(JsonArray pacientPrograms) {
        this.mPacientPrograms = pacientPrograms;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPacientPrograms.size();
    }

    @Override
    public JsonObject getItem(int position) {
        return mPacientPrograms.get(position).getAsJsonObject();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).get("id").getAsLong();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pacient_program_item, parent, false);

        JsonObject pacientProgram = mPacientPrograms.get(position).getAsJsonObject();

        if (pacientProgram.get("labs") != null && !pacientProgram.get("labs").isJsonNull())
            downloadImage(getContext(), pacientProgram.getAsJsonObject("labs").get("full_path_image_400").getAsString(),
                    (ImageView) convertView.findViewById(R.id.labImage), R.drawable.placeholder_lab);

        ((TextView) convertView.findViewById(R.id.pacientProgramName)).setText(pacientProgram.get("name").getAsString());
        if (pacientProgram.get("diagnostics") != null && !pacientProgram.get("diagnostics").isJsonNull() &&
                pacientProgram.get("treatments") != null && !pacientProgram.get("treatments").isJsonNull()) {
            convertView.findViewById(R.id.pacientProgramDiagnostics).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.pacientProgramDiagnostics)).setText(pacientProgram.get("diagnostics").getAsString());
            ((TextView) convertView.findViewById(R.id.pacientProgramSummary)).setText(pacientProgram.get("treatments").getAsString());
        } else {
            ((TextView) convertView.findViewById(R.id.pacientProgramSummary)).setText(pacientProgram.get("summary").getAsString());
        }
        if (pacientProgram.get("price") != null && !pacientProgram.get("price").isJsonNull()) {
            convertView.findViewById(R.id.lblPrecioConDescuento).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.txtPrecioConDescuento).setVisibility(View.VISIBLE);
            NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMAN);
            format.setMaximumFractionDigits(0);
            String precio = "$" + format.format(pacientProgram.get("price").getAsDouble());
            ((TextView) convertView.findViewById(R.id.txtPrecioConDescuento)).setText(precio);
        } else {
            convertView.findViewById(R.id.lblPrecioConDescuento).setVisibility(View.GONE);
            convertView.findViewById(R.id.txtPrecioConDescuento).setVisibility(View.GONE);
        }

        return convertView;
    }
}
