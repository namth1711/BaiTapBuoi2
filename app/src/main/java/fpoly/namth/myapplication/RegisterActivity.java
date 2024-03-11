package fpoly.namth.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

import fpoly.namth.myapplication.model.UserModel;
import fpoly.namth.myapplication.utils.FirebaseUtil;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    CountryCodePicker countryCodePicker;
    EditText edUsername, edPassword, edRePassword,edPhone,edEmail;
    TextView back;
    FirebaseAuth mAuth;
    UserModel userModel;
    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edEmail = findViewById(R.id.register_email);
        edUsername = findViewById(R.id.register_uname);
        countryCodePicker = findViewById(R.id.register_countrycode);
        edPhone = findViewById(R.id.register_phone);
        edPassword = findViewById(R.id.register_password);
        edRePassword = findViewById(R.id.register_repassword);
        btnRegister = findViewById(R.id.btnregister);
        back = findViewById(R.id.tvBack);

        countryCodePicker.registerCarrierNumberEditText(edPhone);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edEmail.getText().toString().trim();
                String username = edUsername.getText().toString().trim();
                String phone = countryCodePicker.getFullNumberWithPlus();
                String password = edPassword.getText().toString();
                String rePassword = edRePassword.getText().toString();
                if(!countryCodePicker.isValidFullNumber()){
                    edPhone.setError(edPhone.getText().toString().trim());
                    return;
                }
                validate(email,username,password,rePassword);

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userModel = new UserModel(phone,username, Timestamp.now(),FirebaseUtil.currentUserId());
                            FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                        startActivity(intent);
                                    }}
                            });
                        } else {
                            Log.w("register", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void validate(String email,String username,String password,String rePassword){
        if (TextUtils.isEmpty(email)) {
            edEmail.setError("Vui lòng nhập email");
            edEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email không hợp lệ");
            edEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(username)) {
            edUsername.setError("Vui lòng nhập tên");
            edUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edPassword.setError("Vui lòng nhập mật khẩu");
            edPassword.requestFocus();
        } else if (password.length() < 6) {
            edPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(rePassword)) {
            edRePassword.setError("Vui lòng nhập lại mật khẩu");
            edRePassword.requestFocus();
        } else if (!rePassword.equals(password)) {
            edRePassword.setError("Mật khẩu không khớp");
            edRePassword.requestFocus();
            return;
        }
    }
}
