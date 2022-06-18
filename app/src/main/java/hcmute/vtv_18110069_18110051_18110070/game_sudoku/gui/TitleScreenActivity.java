package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Random;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;

/**
 *
 * Class này là activity dùng để xử lí sự kiện cho screen trang bắt đầu
 *
 * Người dùng có thể tiếp tục game họ đã chơi ở đây
 * và luôn là màn chơi gần nhất --> Resume
 * hoặc chọn chơi màn khác --> New Game
 * hoặc chỉnh sửa thiết lập --> Settings
 *
 */
public class TitleScreenActivity extends ThemedActivity {

    private static final int RC_SIGN_IN = 9001;
    private Button mResumeBtn, mLogoutBtn;
    private SignInButton mGoogleSignInBtn;
    private TextView mNameTv;
    //declare google firebase
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        mNameTv = findViewById(R.id.name_textview);
        mResumeBtn = findViewById(R.id.resume_button);
        mGoogleSignInBtn = findViewById(R.id.sign_in_button);
        mLogoutBtn = findViewById(R.id.logout_button);
        Button mNewGameBtn = findViewById(R.id.new_game_button);
        Button mSudokuListBtn = findViewById(R.id.sudoku_list_button);
        Button mSettingsBtn = findViewById(R.id.settings_button);

        setupResumeButton();
        setupGoogleSignInButton();

        Random random = new Random();
        int sudokuId = random.nextInt(90 - 1) + 1;
        Intent intentNewGame = new Intent(TitleScreenActivity.this, PlayActivity.class);
        intentNewGame.putExtra(PlayActivity.EXTRA_SUDOKU_ID, (long) sudokuId);
        mNewGameBtn.setOnClickListener((view) ->
                startActivity(intentNewGame));

        mSudokuListBtn.setOnClickListener((view) ->
                startActivity(new Intent(this, DifficultiesListActivity.class)));

        mSettingsBtn.setOnClickListener((view) ->
                startActivity(new Intent(this, GameSettingsActivity.class)));

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showSudokuFolderListOnStartup = gameSettings.getBoolean("show_sudoku_lists_on_startup", false);
        if (showSudokuFolderListOnStartup) {
            startActivity(new Intent(this, DifficultiesListActivity.class));
        }

        // Initialize sign in options
        // the client-id is copied form
        // google-services.json file
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken("923872413327-qcekb497smokmhnthcii7ot37mhpdf1k.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Initialize sign in client
        googleSignInClient= GoogleSignIn.getClient(TitleScreenActivity.this
                ,googleSignInOptions);
        // Initialize firebase auth
        firebaseAuth= FirebaseAuth.getInstance();
        // Initialize firebase user
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser!=null)
        {
            // When user already sign in
            // redirect to profile activity
            startActivity(new Intent(this,TitleScreenActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private boolean canResume(long mSudokuGameID) {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        SudokuGame mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        if (mSudokuGame != null) {
            return mSudokuGame.getState() != SudokuGame.GAME_STATE_COMPLETED;
        }
        return false;
    }

    private void setupResumeButton() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long mSudokuGameID = gameSettings.getLong("most_recently_played_sudoku_id", 0);
        if (canResume(mSudokuGameID)) {
            mResumeBtn.setVisibility(View.VISIBLE);
            mResumeBtn.setOnClickListener((view) -> {
                Intent intentToPlay = new Intent(TitleScreenActivity.this, PlayActivity.class);
                intentToPlay.putExtra(PlayActivity.EXTRA_SUDOKU_ID, mSudokuGameID);
                startActivity(intentToPlay);
            });
        } else {
            mResumeBtn.setVisibility(View.GONE);
        }
    }

    private void setupGoogleSignInButton() {
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            mGoogleSignInBtn.setVisibility(View.VISIBLE);
            mLogoutBtn.setVisibility(View.GONE);
            mNameTv.setVisibility(View.GONE);

            mGoogleSignInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Initialize sign in intent
                    Intent intent=googleSignInClient.getSignInIntent();
                    // Start activity for result
                    startActivityForResult(intent,RC_SIGN_IN);
                }
            });
        } else {
            mGoogleSignInBtn.setVisibility(View.GONE);
            mLogoutBtn.setVisibility(View.VISIBLE);
            mNameTv.setVisibility(View.VISIBLE);
            mNameTv.setText("Hello" + firebaseUser.getDisplayName());
            mLogoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Sign out from google
                    googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Check condition
                            if(task.isSuccessful())
                            {
                                // When task is successful
                                // Sign out from firebase
                                firebaseAuth.signOut();

                                // Display Toast
                                Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();

                                // Finish activity
                                finish();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupResumeButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if(requestCode==RC_SIGN_IN)
        {
            // When request code is equal to RC_SIGN_IN
            // Initialize task
            Task<GoogleSignInAccount> signInAccountTask=GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            // check condition
            if(signInAccountTask.isSuccessful())
            {
                // When google sign in successful
                // Initialize string
                String s="Google sign in successful";
                // Display Toast
                displayToast(s);
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount=signInAccountTask
                            .getResult(ApiException.class);
                    // Check condition
                    if(googleSignInAccount!=null)
                    {
                        // When sign in account is not equal to null
                        // Initialize auth credential
                        AuthCredential authCredential= GoogleAuthProvider
                                .getCredential(googleSignInAccount.getIdToken()
                                        ,null);
                        // Check credential
                        firebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // Check condition
                                        if(task.isSuccessful())
                                        {
                                            startActivity(new Intent(TitleScreenActivity.this
                                                    ,TitleScreenActivity.class)
                                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                            // Display Toast
                                            displayToast("Firebase authentication successful");

                                        }
                                        else
                                        {
                                            // When task is unsuccessful
                                            // Display Toast
                                            displayToast("Authentication Failed :"+task.getException()
                                                    .getMessage());
                                        }
                                    }
                                });

                    }
                }
                catch (ApiException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

}
