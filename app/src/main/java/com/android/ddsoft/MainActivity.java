package com.android.ddsoft;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 1;

    private ImageView image;
    private Button button,addButton;
    private EditText quantity,weight,color,rate,price,notes;
    private TextView outputTextView;
    private List<DataModel> dataList;
    private String imageFilePath;
//    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
        }

        image = findViewById(R.id.image);
        button = findViewById(R.id.OpenCamera);
        addButton = findViewById(R.id.addButton);
        quantity = findViewById(R.id.quantity);
        weight = findViewById(R.id.weight);
        color = findViewById(R.id.color);
        rate = findViewById(R.id.rate);
        price = findViewById(R.id.price);
        notes = findViewById(R.id.notes);
        dataList = new ArrayList<>();
        outputTextView = findViewById(R.id.outputTextView);

        Button addButton = findViewById(R.id.addButton);
        Button saveButton = findViewById(R.id.saveButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap img= ((BitmapDrawable) image.getDrawable()).getBitmap();
                String qty = quantity.getText().toString();
                String wgt =weight.getText().toString();
                String clr =color.getText().toString();
                String nts =notes.getText().toString();
                String rt =rate.getText().toString();
                String prc =price.getText().toString();
                if (img != null && !qty.isEmpty() && !wgt.isEmpty() && !clr.isEmpty() && !nts.isEmpty() && !rt.isEmpty() && !prc.isEmpty()) {
                    dataList.add(new DataModel(img ,qty, wgt, clr, nts, rt, prc));
                    outputTextView.setText("Data added: Image - "+ img+ ", Quantity - " + qty + ", Weight - " + wgt+" ,Color - " + clr + ",Notes - " + nts + ",Rate - " + rt + ",Price - " + prc);
                    quantity.getText().clear();
                    weight.getText().clear();
                    color.getText().clear();
                    notes.getText().clear();
                    rate.getText().clear();
                    price.getText().clear();
                    image.setImageBitmap(null);

                }
                else {
                    outputTextView.setText("Please enter Valid Details.");
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkWriteStoragePermission()) {
                    createExcelFile();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File imageFile = null; // Create a file to save the image
                    try {
                        imageFile = createImageFile();
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (imageFile != null) {
                        imageFilePath = imageFile.getAbsolutePath(); // Save the file path
                        Uri imageUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // Set the image file output path
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }

            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                imageFilePath = imageFile.getAbsolutePath();
                return imageFile;
            }
        });





    }

    private boolean checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createExcelFile();
            }
            else {
                outputTextView.setText("Permission denied. Cannot create Excel file.");
            }
        }
    }


    private void createExcelFile() {
        try {
//            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
//            if (bitmap == null) {
//                outputTextView.setText("Error: Captured image is null.");
//                return;
//            }
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            Sheet sheet = hssfWorkbook.createSheet("Data");
            Row headerRow = sheet.createRow(0);
            Cell imgCell = headerRow.createCell(0);
            imgCell.setCellValue("Image");
            Cell qtyCell = headerRow.createCell(1);
            qtyCell.setCellValue("Quantity");
            Cell wgtCell = headerRow.createCell(2);
            wgtCell.setCellValue("Weight");
            Cell clrCell = headerRow.createCell(3);
            clrCell.setCellValue("Color");
            Cell ntsCell = headerRow.createCell(4);
            ntsCell.setCellValue("Notes");
            Cell rtCell = headerRow.createCell(5);
            rtCell.setCellValue("Rate");
            Cell prcCell = headerRow.createCell(6);
            prcCell.setCellValue("Price");


            for (int k = 0; k < dataList.size(); k++) {
                Row dataRow = sheet.createRow(k + 1);
                DataModel data = dataList.get(k);
                Bitmap bitmap = data.getImg();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bytes = stream.toByteArray();
                int intPictureIndex = hssfWorkbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);

                CreationHelper creationHelper = hssfWorkbook.getCreationHelper();
                Drawing drawing = sheet.createDrawingPatriarch();
                ClientAnchor clientAnchor = creationHelper.createClientAnchor();
                clientAnchor.setCol1(0);
                clientAnchor.setRow1(k+2);
                clientAnchor.setCol2(1);
                clientAnchor.setRow2(k+3);
                drawing.createPicture(clientAnchor, intPictureIndex);


                Cell qtyDataCell = dataRow.createCell(1);
                qtyDataCell.setCellValue(data.getQty());
                Cell wgtDataCell = dataRow.createCell(2);
                wgtDataCell.setCellValue(data.getWgt());
                Cell clrDataCell = dataRow.createCell(3);
                clrDataCell.setCellValue(data.getClr());
                Cell ntsDataCell = dataRow.createCell(4);
                ntsDataCell.setCellValue(data.getNts());
                Cell rtDataCell = dataRow.createCell(5);
                rtDataCell.setCellValue(data.getRt());
                Cell prcDataCell = dataRow.createCell(6);
                prcDataCell.setCellValue(data.getPrc());

            }
//            filterExcelTable(sheet);
            // Save the workbook to a file

            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/data.xls";
            FileOutputStream fileOut = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOut);
            fileOut.close();
            hssfWorkbook.close();
            outputTextView.setText("Excel file created successfully at " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            outputTextView.setText("Error occurred while creating Excel file: " + e.getMessage());
        }
    }





//    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        float scaleWidth = ((float) maxWidth) / width;
//        float scaleHeight = ((float) maxHeight) / height;
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
//    }

//    private byte[] bitmapToByteArray(Bitmap bitmap) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        return stream.toByteArray();
//    }


//    private void filterExcelTable(Sheet sheet) {
//        int rowCount = sheet.getPhysicalNumberOfRows();
//
//        for (int i = 1; i < rowCount; i += 2) {
//            Row row = sheet.getRow(i);
//            if (row != null) {
//                sheet.removeRow(row);
//            }
//        }
//
//        // Adjust row numbers after removal
//        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//            Row row = sheet.getRow(i);
//            if (row != null) {
//                sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
//                i--; // Adjust the loop counter after shifting rows
//            }
//        }
//    }









    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap photo = BitmapFactory.decodeFile(imageFilePath);
//        Bitmap photo = (Bitmap)data.getExtras().get("data");
            image.setImageBitmap(photo);
            DataModel newData = new DataModel(photo, quantity.getText().toString(), weight.getText().toString(), color.getText().toString(),
                    notes.getText().toString(), rate.getText().toString(), price.getText().toString());

            // Add the new DataModel object to your dataList
            dataList.add(newData);

            // Clear input fields
            quantity.getText().clear();
            weight.getText().clear();
            color.getText().clear();
            notes.getText().clear();
            rate.getText().clear();
            price.getText().clear();
        }
    }

}
