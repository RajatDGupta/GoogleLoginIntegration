package google.socialmedia.neerajgupta.com.googleloginintegration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, FacebookCallback<LoginResult> {

    private LinearLayout li_profile, li_button;
    private CircleImageView img_profile;
    private TextView tv_user_name, tv_user_email;
    Button btn_logout;
    private SignInButton btn_signInButton;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 7949;
    SessionForGoogleLogin sessionForLogin;
    private ProgressDialog mProgressDialog;
    LoginButton fb_login_button;
    TextView fb_status;
    CallbackManager callbackManager;
    AccessToken token;


    String firstName, lastName, email, birthday, gender;
    private URL profilePicture;
    private String userId;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        init();
        session();

        li_profile.setVisibility(View.GONE);

        printKeyHash(this);

        callbackManager = CallbackManager.Factory.create();
        fb_login_button.registerCallback(callbackManager, this);


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


    }


    private void session() {
        sessionForLogin = new SessionForGoogleLogin(getApplicationContext());

    }

    private void init() {
        li_profile = findViewById(R.id.li_profile);
        li_button = findViewById(R.id.li_button);

        img_profile = findViewById(R.id.imageView);

        tv_user_name = findViewById(R.id.tv_name);
        tv_user_email = findViewById(R.id.tv_email);
        fb_status = findViewById(R.id.login_status);

        btn_logout = findViewById(R.id.btn_logout);

        btn_signInButton = findViewById(R.id.btn_signin);
        btn_signInButton.setSize(SignInButton.SIZE_STANDARD);

        fb_login_button = findViewById(R.id.facebookLogin);

        btn_signInButton.setOnClickListener(this);
        fb_login_button.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signin:
                signIn();
                break;
            case R.id.btn_logout:

                token = AccessToken.getCurrentAccessToken();

                if (token == null) {
                    signOut();
                } else {
                    disconnectFromFacebook();
                }
                break;
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updatUI(false);
            }
        });
    }


    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();

        updatUI(false);
    }


    private void handleResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {

            GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();

            String user_name = googleSignInAccount.getDisplayName();
            String user_email = googleSignInAccount.getEmail();
            String user_img_url = googleSignInAccount.getPhotoUrl().toString();

            tv_user_name.setText(user_name);
            tv_user_email.setText(user_email);
            Glide.with(this).load(user_img_url).into(img_profile);

            updatUI(true);

        } else {
            updatUI(false);
        }
    }

    private void updatUI(boolean isLogin) {
        if (isLogin) {
            li_profile.setVisibility(View.VISIBLE);
            li_button.setVisibility(View.GONE);
        } else {
            li_profile.setVisibility(View.GONE);
            li_button.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(googleSignInResult);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d("TAG", "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


    public String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }


    @Override
    public void onSuccess(LoginResult loginResult) {
        //loginResult.getAccessToken();
        String status = "login successs";
        fb_status.setText(status);


        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.e(TAG, object.toString());
                Log.e(TAG, response.toString());

                try {
                    userId = object.getString("id");
                    profilePicture = new URL("https://graph.facebook.com/" + userId + "/picture?width=500&height=500");
                    if (object.has("first_name"))
                        firstName = object.getString("first_name");
                    if (object.has("last_name"))
                        lastName = object.getString("last_name");
                    if (object.has("email"))
                        email = object.getString("email");
                    if (object.has("birthday"))
                        birthday = object.getString("birthday");
                    if (object.has("gender"))
                        gender = object.getString("gender");

                    String url = profilePicture.toString();
                    Log.v(TAG, url);

                    tv_user_name.setText(firstName);
                    tv_user_email.setText(gender);
                    Glide.with(MainActivity.this).load(url).into(img_profile);

                    if (url != null) {
                        updatUI(true);
                    }


                   /* Intent main = new Intent(LoginActivity.this,MainActivity.class);
                    main.putExtra("name",firstName);
                    main.putExtra("surname",lastName);
                    main.putExtra("imageUrl",profilePicture.toString());
                    startActivity(main);
                    finish();*/


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        //Here we put the requested fields to be returned from the JSONObject
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email, birthday, gender");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public void onCancel() {
        String status = "login cancelled";
        fb_status.setText(status);
    }

    @Override
    public void onError(FacebookException error) {
        Toast.makeText(MainActivity.this, "error to Login Facebook", Toast.LENGTH_SHORT).show();

    }


}
