package com.orugga.yapp.fragments.login;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;
import com.orugga.yapp.AlarmBroadcastReceiver;
import com.orugga.yapp.LoginActivity;
import com.orugga.yapp.MainHomeActivity;
import com.orugga.yapp.R;
import com.orugga.yapp.database.RecordatorioToma;
import com.orugga.yapp.database.RecordatorioVisita;
import com.orugga.yapp.helpers.FileChooser;
import com.orugga.yapp.helpers.SessionHelper;
import com.orugga.yapp.interfaces.ActivityUpdateBackToolbar;
import com.orugga.yapp.interfaces.JsonObjectResponse;
import com.orugga.yapp.requests.UploadImageRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_CAMBIAR_CONTRASEÑA;
import static com.orugga.yapp.Constants.FragmentsNames.FRAGMENT_WELCOME;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_COMPLETE_INFO;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_CREATE;
import static com.orugga.yapp.Constants.IntentFilters.ACTION_UPDATE;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_IMAGE_CAPTURE;
import static com.orugga.yapp.Constants.IntentFilters.REQUEST_SELECT_IMAGE;
import static com.orugga.yapp.Constants.RequestPermission.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static com.orugga.yapp.helpers.ImageHelper.drawableToBitmap;
import static com.orugga.yapp.helpers.SessionHelper.getAccessToken;
import static com.orugga.yapp.helpers.SessionHelper.getUser;
import static com.orugga.yapp.helpers.SessionHelper.setUser;
import static com.orugga.yapp.requests.CreateUserRequest.createUser;
import static com.orugga.yapp.requests.DownloadImageRequest.downloadProfilePhoto;
import static com.orugga.yapp.requests.GetRegisterDataRequest.getRegisterData;
import static com.orugga.yapp.requests.UpdateUserRequest.updateUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterAccountFragment extends Fragment implements com.orugga.yapp.interfaces.OnFragmentUpdateToolbarIntefrace {


    @SuppressWarnings("RegExpRedundantEscape")
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\!\\@\\#\\$\\-\\+\\_\\*]{3,24}"
    );
    private TextView mNombreView;
    private TextView mEmailView;
    private TextView mPasswordView;
    private TextView mConfirmPasswordView;
    private RoundedImageView mProfileImage;
    private boolean imageUpdated = false;
    private int mAction;


    public RegisterAccountFragment() {
        // Required empty public constructor
    }

    public static RegisterAccountFragment newInstance(int mAction) {
        RegisterAccountFragment fragment = new RegisterAccountFragment();
        Bundle args = new Bundle();
        args.putInt("action", mAction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mAction = getArguments().getInt("action");
        if (mAction == ACTION_UPDATE)
            setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.menu_item_add_reminder);
        if (item != null) {
            item.setVisible(false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_account, container, false);

        TextView btnCreateAccount = view.findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attempt()) {
                    if (mAction == ACTION_UPDATE || mAction == ACTION_COMPLETE_INFO) {
                        Answers.getInstance().logCustom(new CustomEvent("Mi perfil - Actualizar usuario"));
                        updateAccount();
                    } else {
                        Answers.getInstance().logCustom(new CustomEvent("Crear cuenta - Crear cuenta"));
                        registerAccount();
                    }
                }
            }
        });

        mNombreView = view.findViewById(R.id.txtNombre);
        mEmailView = view.findViewById(R.id.txtEmail);
        mPasswordView = view.findViewById(R.id.txtContraseña);
        mConfirmPasswordView = view.findViewById(R.id.txtRepetirContraseña);
        mProfileImage = view.findViewById(R.id.imgAvatar);


        if (mAction == ACTION_UPDATE || mAction == ACTION_COMPLETE_INFO) {
            btnCreateAccount.setText(getString(R.string.btn_siguiente));
            JsonObject user = getUser(getContext());
            if (user == null || getActivity() == null || getActivity().isFinishing()) return view;
            if (user.get("picture") != JsonNull.INSTANCE)
                downloadProfilePhoto(getContext(), user.get("picture").getAsString(), mProfileImage, R.drawable.avatar);
            mNombreView.setText(user.get("name").getAsString());
            mEmailView.setText(user.get("email").getAsString());
            mPasswordView.setVisibility(View.GONE);
            mConfirmPasswordView.setVisibility(View.GONE);
            TextView btnChangePassword = view.findViewById(R.id.btnResetPassword);
            if (user.get("if_facebook") == JsonNull.INSTANCE &&
                    user.get("if_google") == JsonNull.INSTANCE &&
                    user.get("if_twitter") == JsonNull.INSTANCE) {
                btnChangePassword.setVisibility(View.VISIBLE);
                btnChangePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Answers.getInstance().logCustom(new CustomEvent("Mi perfil - Cambiar Contraseña"));
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.coordinatorLayout, ChangePasswordFragment.newInstance())
                                .addToBackStack(FRAGMENT_CAMBIAR_CONTRASEÑA).commitAllowingStateLoss();
                    }
                });
            }
        }


        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Mi perfil - Cambiar Imagen"));
                if (!runtime_permissions())
                    selectPhoto();
            }
        });

        if (mAction == ACTION_COMPLETE_INFO) {
            Toast.makeText(getContext(), R.string.please_complete_user_information, Toast.LENGTH_SHORT).show();
        }

        return view;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap;
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    imageBitmap = (Bitmap) data.getExtras().get("data");
                    try {
                        File outputDir = getContext().getCacheDir(); // context being the Activity pointer
                        File outputFile = File.createTempFile("tempImage", ".jpeg", outputDir);
                        OutputStream fos = new FileOutputStream(outputFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                        if (mAction == ACTION_UPDATE || mAction == ACTION_COMPLETE_INFO) {
                            uploadProfileImage(outputFile, imageBitmap);
                        } else {
                            mProfileImage.setImageBitmap(imageBitmap);
                            imageUpdated = true;
                        }
                    } catch (IOException e) {
                        Log.e("RegisterAccountFragment", e.getMessage());
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), R.string.toast_error_text, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_SELECT_IMAGE:
                    Uri selectedImageUri = data.getData();
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                        if (mAction == ACTION_UPDATE || mAction == ACTION_COMPLETE_INFO) {
                            File imageFile = new File(FileChooser.getPath(getContext(), selectedImageUri));
                            uploadProfileImage(imageFile, imageBitmap);
                        } else {
                            mProfileImage.setImageBitmap(imageBitmap);
                            imageUpdated = true;
                        }
                    } catch (IOException e) {
                        Log.e("RegisterAccountFragment", e.getMessage());
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), R.string.toast_error_text, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    private void uploadProfileImage(File outputFile, final Bitmap imageBitmap) {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        UploadImageRequest.uploadImage(getContext(), getAccessToken(getContext()), outputFile, new JsonObjectResponse() {
            @Override
            public void onSuccess(JsonObject userData) {
                progressDialog.dismiss();
                mProfileImage.setImageBitmap(imageBitmap);
                if (getActivity() instanceof MainHomeActivity) {
                    ((MainHomeActivity) getActivity()).setProfileImage(userData.get("picture").getAsString());
                }
                setUser(getContext(), userData);
                imageUpdated = true;
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                progressDialog.dismiss();
                if (e != null) {
                    Crashlytics.logException(e);
                } else if (response.get("rta") != null && !response.get("rta").isJsonNull() &&
                        response.get("rta").getAsString().equals("Unauthorised")) {
                    Toast.makeText(getContext(), R.string.expired_session, Toast.LENGTH_SHORT).show();
                    SessionHelper.clearSession(getActivity().getApplicationContext());
                    AlarmBroadcastReceiver.cancelAllAlarms(getActivity().getApplicationContext());
                    RecordatorioVisita.deleteAll(RecordatorioVisita.class);
                    RecordatorioToma.deleteAll(RecordatorioToma.class);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void selectPhoto() {
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_photo);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.take_photo))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } else if (items[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                } else if (items[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private boolean attempt() {

        // Reset errors.
        mNombreView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the register attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();
        String nombre = mNombreView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (mAction == ACTION_CREATE) {
            if (password.length() < 4) {
                mPasswordView.setError(getString(R.string.error_weak_password));
                focusView = mPasswordView;
                cancel = true;
            } else if (!isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_password_invalid));
                focusView = mPasswordView;
                cancel = true;
            } else if (!arePasswordEqual(password, confirmPassword)) {
                mConfirmPasswordView.setError(getString(R.string.error_passwords_not_match));
                focusView = mConfirmPasswordView;
                cancel = true;
            }
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(nombre)) {
            mNombreView.setError(getString(R.string.error_field_required));
            focusView = mNombreView;
            cancel = true;
        } else if (!isNameValid(nombre)) {
            mNombreView.setError(getString(R.string.error_invalid_name));
            focusView = mNombreView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void registerAccount() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        getRegisterData(getActivity(), new JsonObjectResponse() {
            @Override
            public void onSuccess(final JsonObject registerData) {
                createUser(getActivity(), mNombreView.getText().toString(), mEmailView.getText().toString(),
                        mPasswordView.getText().toString(), new JsonObjectResponse() {
                            @Override
                            @SuppressWarnings("ConstantConditions")
                            public void onSuccess(JsonObject userData) {
                                if (imageUpdated) {
                                    try {
                                        Bitmap imageBitmap = drawableToBitmap(mProfileImage.getDrawable());
                                        File outputDir = getContext().getCacheDir(); // context being the Activity pointer
                                        File outputFile = File.createTempFile("tempImage", ".jpeg", outputDir);
                                        OutputStream fos = new FileOutputStream(outputFile);
                                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                        fos.flush();
                                        fos.close();
                                        UploadImageRequest.uploadImage(getContext(), userData.get("api_token").getAsString(), outputFile, new JsonObjectResponse() {
                                            @Override
                                            public void onSuccess(JsonObject userData) {
                                                progressDialog.dismiss();
                                                getActivity().getSupportFragmentManager().popBackStack();
                                                getActivity().getSupportFragmentManager().beginTransaction()
                                                        .add(R.id.coordinatorLayout, WelcomeFragment.newInstance(userData, registerData)).addToBackStack(FRAGMENT_WELCOME)
                                                        .commitAllowingStateLoss();
                                            }

                                            @Override
                                            public void onError(JsonObject response, Exception e) {
                                                progressDialog.dismiss();
                                                if (e != null) {
                                                    Crashlytics.logException(e);
                                                } else if (response != null) {
                                                    if (response.get("error") != null && response.get("error") != JsonNull.INSTANCE) {
                                                        Toast.makeText(getContext(), response.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        if (response.get("rta").getAsString().equals("Unauthorised")) {
                                                            Toast.makeText(getContext(), R.string.expired_session, Toast.LENGTH_SHORT).show();
                                                            SessionHelper.clearSession(getActivity().getApplicationContext());
                                                            AlarmBroadcastReceiver.cancelAllAlarms(getActivity().getApplicationContext());
                                                            RecordatorioVisita.deleteAll(RecordatorioVisita.class);
                                                            RecordatorioToma.deleteAll(RecordatorioToma.class);
                                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                                            getActivity().finish();
                                                        } else {
                                                            Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } catch (IOException e) {
                                        Log.e("RegisterAccountFragment", e.getMessage());
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .add(R.id.coordinatorLayout, WelcomeFragment.newInstance(userData, registerData)).addToBackStack(FRAGMENT_WELCOME)
                                            .commitAllowingStateLoss();
                                }

                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                progressDialog.dismiss();
                                if (e != null) {
                                    Crashlytics.logException(e);
                                } else if (response != null) {
                                    Toast.makeText(getContext(), response.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAccount() {
        JsonObject user = getUser(getContext());
        if (user == null) return;
        final long region = user.get("region_id") == JsonNull.INSTANCE ? -1 : user.get("region_id").getAsLong();
        final long comunne = user.get("comunne_id") == JsonNull.INSTANCE ? -1 : user.get("comunne_id").getAsLong();
        final long healtInsurances = user.get("healthinsurances").getAsJsonArray().size() == 0 ? -1 : user.get("healthinsurances").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
        final String gender = user.get("gender") == JsonNull.INSTANCE ? null : user.get("gender").getAsString();
        JsonArray jsonDeals = user.get("deals").getAsJsonArray();
        final ArrayList<Long> deals = new ArrayList<>();
        for (int i = 0; i < jsonDeals.size(); i++) {
            deals.add(jsonDeals.get(i).getAsJsonObject().get("id").getAsLong());
        }
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), "", getString(R.string.loading));
        getRegisterData(getActivity(), new JsonObjectResponse() {
            @Override
            public void onSuccess(final JsonObject registerData) {
                progressDialog.dismiss();
                updateUser(getActivity(), getAccessToken(getContext()), mEmailView.getText().toString(),
                        mNombreView.getText().toString(), "" /*mApellidoView.getText().toString()*/, null /*mFechaNacimiento*/, gender,
                        region, comunne, healtInsurances, deals, new JsonObjectResponse() {
                            @Override
                            public void onSuccess(JsonObject userData) {
                                if (getActivity() == null || getActivity().isFinishing()) return;
                                setUser(getContext(), userData);
                                progressDialog.dismiss();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .add(R.id.coordinatorLayout, ProfileFragment.newInstance(userData, registerData, ACTION_UPDATE)).addToBackStack(FRAGMENT_WELCOME)
                                        .commitAllowingStateLoss();
                            }

                            @Override
                            public void onError(JsonObject response, Exception e) {
                                if (getActivity() == null || getActivity().isFinishing()) return;
                                progressDialog.dismiss();
                                if (e != null) {
                                    Crashlytics.logException(e);
                                } else if (response != null) {
                                    if (response.get("error") != null && response.get("error") != JsonNull.INSTANCE) {
                                        Toast.makeText(getContext(), response.get("error").toString(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (response.get("rta").getAsString().equals("Unauthorised")) {
                                            Toast.makeText(getContext(), R.string.expired_session, Toast.LENGTH_SHORT).show();
                                            SessionHelper.clearSession(getActivity().getApplicationContext());
                                            AlarmBroadcastReceiver.cancelAllAlarms(getActivity().getApplicationContext());
                                            RecordatorioVisita.deleteAll(RecordatorioVisita.class);
                                            RecordatorioToma.deleteAll(RecordatorioToma.class);
                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                            getActivity().finish();
                                        } else {
                                            Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onError(JsonObject response, Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), R.string.error_message_internet_conection, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean arePasswordEqual(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isNameValid(String name) {
        return Pattern.compile("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]{1,256}").matcher(name).matches();
    }

    @Override
    public void onFragmentUpdateToolbar() {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (getActivity() instanceof ActivityUpdateBackToolbar) {
            ((ActivityUpdateBackToolbar) getActivity()).showBackNavigationIcon();
        }
        if (mAction == ACTION_CREATE) {
            getActivity().setTitle(getString(R.string.action_bar_title_nueva_cuenta));
        } else {
            getActivity().setTitle(getString(R.string.action_bar_title_my_account));
        }
    }

    private boolean runtime_permissions() {
        if (getActivity() == null || getActivity().isFinishing()) return false;
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            return true;
        }
        return false;
    }

    public int getAction() {
        return mAction;
    }
}
