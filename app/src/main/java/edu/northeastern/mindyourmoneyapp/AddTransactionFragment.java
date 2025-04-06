package edu.northeastern.mindyourmoneyapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.northeastern.mindyourmoneyapp.databinding.ActivityTransactionBinding;
import edu.northeastern.mindyourmoneyapp.databinding.FragmentAddTransactionBinding;
import edu.northeastern.mindyourmoneyapp.databinding.ListDialogBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    FragmentAddTransactionBinding binding;


    private DatabaseReference mindYourMoneyRef;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 101;
    private static final int REQUEST_CODE_CAMERA = 102;

    private Uri capturedImageUri = null;



    String monthYear,year;

    public AddTransactionFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);
        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.YEAR, datePicker.getYear());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                SimpleDateFormat dateFormatMonthYear = new SimpleDateFormat("MMMM, YYYY");
                monthYear = dateFormatMonthYear.format(calendar.getTime());
                SimpleDateFormat Year = new SimpleDateFormat("YYYY");
                year = Year.format(calendar.getTime());
                binding.date.setText(dateFormat.format(calendar.getTime()));

            });
            datePickerDialog.show();
        });

        binding.uploadPhotoBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
            } else {
                launchCamera();
            }
        });





        binding.category.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());

            ArrayList<Category> categories = new ArrayList<>();
            categories.add(new Category("Groceries",R.drawable.ic_accounts,R.color.category1));
            categories.add(new Category("Fuel",R.drawable.ic_accounts,R.color.category2));
            categories.add(new Category("Business",R.drawable.ic_accounts,R.color.category3));
            categories.add(new Category("Dining",R.drawable.ic_accounts,R.color.category4));
            categories.add(new Category("Utilities",R.drawable.ic_accounts,R.color.category5));
            categories.add(new Category("General",R.drawable.ic_accounts,R.color.category6));

            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), categories, new CategoryAdapter.CategoryClickListener() {
                @Override
                public void onCategoryClicked(Category category) {

                    binding.category.setText(category.getCategoryName());
                    categoryDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);

            categoryDialog.show();
        });

        binding.account.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Credit Card"));
            accounts.add(new Account(0, "Debit Card"));
            accounts.add(new Account(0, "PayPal"));
            accounts.add(new Account(0, "Other"));

            AccountsAdapter adapter = new AccountsAdapter(getContext(), accounts, new AccountsAdapter.AccountsClickListener() {
                @Override
                public void onAccountSelected(Account account) {
                    binding.account.setText(account.getAccountName());
                    accountsDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBinding.recyclerView.setAdapter(adapter);

            accountsDialog.show();

        });

        binding.saveTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String imageUrl = capturedImageUri != null ? capturedImageUri.toString() : null;
                if (capturedImageUri != null) {
                    uploadToCloudinary(capturedImageUri);
                } else {
                    saveTransaction(null);
                }
            }
        });

        return binding.getRoot();
    }

    private void saveTransaction(String imageUrl) {
        String category = binding.category.getText().toString().trim();
        String account = binding.account.getText().toString().trim();
        String note = binding.note.getText().toString().trim();
        String date = binding.date.getText().toString().trim();
        String amountStr = binding.amount.getText().toString().trim();
        if (category.isEmpty() || account.isEmpty() || date.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        mindYourMoneyRef = FirebaseDatabase.getInstance().getReference("transactionHistory");

        String transactionId = mindYourMoneyRef.push().getKey();
        Transaction transaction = new Transaction(transactionId, category, account, note, date, monthYear, year, amount, imageUrl);

        Log.println(Log.INFO,"Trasaction",transaction.toString());

        mindYourMoneyRef.child(transactionId).setValue(transaction);

        Toast.makeText(getContext(), "Transaction added successfully!", Toast.LENGTH_SHORT).show();
        Bundle result = new Bundle();
        result.putBoolean("transaction_added", true);
        getParentFragmentManager().setFragmentResult("transaction_request_key", result);
        dismiss();
    }

    private void launchCamera() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                File imageFile = createImageFile();
                if (imageFile != null) {
                    capturedImageUri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".provider",
                            imageFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                }
            }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && capturedImageUri != null) {
            binding.billPhoto.setImageURI(capturedImageUri);
        }
    }


    private File createImageFile() {
        try {
            String fileName = "IMG_" + System.currentTimeMillis();
            File storageDir = new File(requireContext().getExternalFilesDir(null), "bill_images");
            if (!storageDir.exists()) storageDir.mkdirs();
            return new File(storageDir, fileName + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadToCloudinary(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            File tempFile = File.createTempFile("upload_", ".jpg", requireContext().getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.getName(),
                            RequestBody.create(tempFile, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", "mindyourmoney_preset") // your preset
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/detsebj18/image/upload") // your cloud name
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String imageUrl = json.getString("secure_url");
                            requireActivity().runOnUiThread(() -> saveTransaction(imageUrl));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("Cloudinary", "Upload failed: " + response.body().string());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing upload", Toast.LENGTH_SHORT).show();
        }
    }





}