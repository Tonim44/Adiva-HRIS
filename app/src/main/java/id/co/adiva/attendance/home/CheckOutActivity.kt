package id.co.adiva.attendance.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import id.co.adiva.attendance.DashboardActivity
import id.co.adiva.attendance.databinding.ActivityAbsenBinding
import id.co.adiva.attendance.helper.DataManager
import id.co.adiva.attendance.helper.HPI
import id.co.adiva.attendance.helper.PrefHelper
import id.co.adiva.attendance.uitel.LoadingDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.concurrent.thread

class CheckOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbsenBinding
    private val FILE_NAME = "photo.jpg"
    private val REQUEST_CODE = 42
    private lateinit var photoFile: File
    private lateinit var data: DataManager
    lateinit var prefHelper: PrefHelper
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        data = DataManager(baseContext)

        val view = binding.root
        prefHelper = PrefHelper(this)
        setContentView(view)

        binding.tittle.text = "Absen Keluar"

        binding.back.setOnClickListener(View.OnClickListener { onBackPressed() })

        binding.sharelok.setOnClickListener {
            Toast.makeText(this@CheckOutActivity, "Masukan foto absen terlebih dahulu", Toast.LENGTH_LONG).show()
        }
        binding.simpanKehadiran.setOnClickListener {
            Toast.makeText(this@CheckOutActivity, "Masukan foto absen terlebih dahulu", Toast.LENGTH_LONG).show()
        }

        //TakePhoto
        binding.ambilfoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider =
                FileProvider.getUriForFile(this, "id.co.adiva.attendance.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val data = DataManager(baseContext)
            val imageView = binding.fotoAbsen
            val byteArrayOutputStream = ByteArrayOutputStream()
            var bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            bitmap = resizeBitmap(bitmap, 800)
            imageView.setImageBitmap(bitmap)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            sendAbsen()
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun resizeBitmap(bitmap : Bitmap, maxSize: Int) : Bitmap {

        var width : Int = bitmap.getWidth();
        var height : Int = bitmap.getHeight();
        var x : Int = 0;

        if (width >= height && width > maxSize) {
            x = width / height;
            width = maxSize;
            height = maxSize / x
        } else if (height >= width && height > maxSize) {
            x = height / width;
            height = maxSize;
            width = maxSize / x
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    fun sendAbsen() {

        //TakeLocation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        task.addOnSuccessListener {

            val data = DataManager(baseContext)

            val latitude: String = it.latitude.toString()
            val longitude: String = it.longitude.toString()

            Log.i("DATA_API", latitude.toString())
            Log.i("DATA_API", longitude.toString())

            data.putLatitudeNow(latitude)
            data.putLongitudeNow(longitude)

            val shareLokasi = binding.sharelok
            shareLokasi.setOnClickListener {
                val intent = Intent(this, MapsIsiActivity::class.java)
                startActivity(intent)
            }

            //SimpanKehadiran
            binding.simpanKehadiran.setOnClickListener {

                binding.simpanKehadiran.isEnabled = false
                val loading = LoadingDialog(this)
                loading.startLoading()
                val token = data.getString("token")

                thread {
                    try {
                        val client = OkHttpClient()
                        val file = File(photoFile.absolutePath)
                        val requestBody = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart(
                                "photo",
                                file.name,
                                RequestBody.create(MediaType.parse("image/jpg"), file)
                            )
                            .addFormDataPart("token", token)
                            .addFormDataPart("latitude", latitude)
                            .addFormDataPart("longitude", longitude)
                            .build()

                        Log.i("DATA_API", token.toString())
                        Log.i("DATA_API", latitude.toString())
                        Log.i("DATA_API", longitude.toString())

                        val request = Request.Builder()
                            .url("${HPI.API_URL}attendance/clock-out")
                            .post(requestBody)
                            .build()

                        val response = client.newCall(request).execute()

                        val responseBody = response.body()?.string()
                        val jsonResponse = JSONObject(responseBody)

                        Log.i("DATA_API", jsonResponse.toString())
                        runOnUiThread {
                            if (response.isSuccessful) {
                                val results = jsonResponse.getString("message")
                                Toast.makeText(
                                    this@CheckOutActivity,
                                    results,
                                    Toast.LENGTH_LONG
                                ).show()
                                moveIntent()
                            } else {
                                val errorMessage = jsonResponse.optString("message", "Terjadi kesalahan saat mengirim data kehadiran")
                                Toast.makeText(
                                    this@CheckOutActivity,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            binding.simpanKehadiran.isEnabled = true
                            loading.isDismiss()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@CheckOutActivity,
                                "Terjadi kesalahan saat mengirim data kehadiran: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.simpanKehadiran.isEnabled = true
                            loading.isDismiss()
                        }
                    }
                }

            }
        }
    }

    private fun moveIntent(){
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
