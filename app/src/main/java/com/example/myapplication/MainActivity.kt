package com.example.myapplication


//import androidx.compose.ui.graphics.Color

//import android.graphics.pdf.PdfDocument
//import android.graphics.Paint

//import retrofit2.Retrofit
//import androidx.compose.foundation.lazy.LazyColumn

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.pdf417.PDF417Writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

//import com.zebra.sdk.comm.BluetoothConnectionInsecure;
//import com.zebra.sdk.comm.Connection;

const val REQUEST_CODE_BLUETOOTH = 1001

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {


                Surface(
                    modifier = Modifier.size(800.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Organiza los elementos dentro de una columna
                    Column(
                        modifier = Modifier
                            .height(150 .dp) // Ajusta el largo (altura) de la columna
                            .fillMaxWidth(), // O puedes usar fillMaxWidth() si deseas que ocupe todo el ancho disponible
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo de la aplicación
                        Image(
                            painter = painterResource(id = R.drawable.makitafondoblanco),
                            contentDescription = "Logo",
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Fit
                        )


                        Spacer(modifier = Modifier.height(8.dp))

                        // FunSaludo se muestra debajo del logo
                        FunSaludo("Makita")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun FunSaludo(name: String, modifier: Modifier = Modifier)
{

    var text by remember { mutableStateOf("") }
    //var response by rememberSaveable { mutableStateOf<List<ItemResponse>>(emptyList()) }
    var extractedText by remember { mutableStateOf("") }
    var extractedText2 by remember { mutableStateOf("") }
    var extractedText3 by remember { mutableStateOf("") }
    var extractedText4 by remember { mutableStateOf("") }
    var textFieldValue2 by remember { mutableStateOf("") }
   // var response        by remember { mutableStateOf("") }
    var response by rememberSaveable { mutableStateOf<List<ItemResponse>>(emptyList()) }
   // var response by rememberSaveable { mutableStateOf<List<ItemResponse>>(emptyList()) }
   // var responseList by remember { mutableStateOf<List<ItemResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") } // Estado para manejar posibles errores
    var errorState by rememberSaveable { mutableStateOf<String?>(null) }
   // var successMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // Para manejar el foco
    // val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var bitmap by remember  { mutableStateOf<Bitmap?>(null) }
   // val scrollState = rememberScrollState()
    val context = LocalContext.current
    val timestamp = formatTimestamp(System.currentTimeMillis())
    val focusRequester = remember { FocusRequester() }

   // val focusManager = LocalFocusManager.current

    val apiService = RetrofitClient.apiService
   // var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
   // val coroutineScope = rememberCoroutineScope()

    val printerMacAddress = "8c:d5:4a:16:6d:92"

    val scope = rememberCoroutineScope()

   // val deviceList by remember { mutableStateOf(devices) }

   // var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var selectedDevice: BluetoothDevice? by remember { mutableStateOf(null) }
    var pdfPath by remember { mutableStateOf<String?>(null) }
    val REQUEST_CODE_BLUETOOTH = 1
     // Cambia esto por el UUID de tu impresora

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("*MAKITA*", "Permiso de ubicación concedido")
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    val (printers, setPrinters) = remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedPrinterName by remember { mutableStateOf("") }
    var bitmap2 by remember  { mutableStateOf<Bitmap?>(null) }

    val data = """
    *** Mi Empresa ***
    Fecha: ${System.currentTimeMillis()}
    ---------------------------
    Producto   Cantidad   Precio
    Café       2          $5.00
    Sandwich   1          $3.00
    ---------------------------
    Total               $8.00
    Gracias por su compra!
""".trimIndent()


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Habilita scroll vertical
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
               // .weight(1f), // Empuja la columna hacia abajo
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Impresion Etiqueta Cargador , $name! , $timestamp",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                modifier = modifier.padding(bottom = 16.dp)
            )


            Spacer(modifier = Modifier.height(14.dp))



            Text(
                text = "Codigo PDF417",
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LaunchedEffect(Unit) {
                // Solicita el foco en el primer TextField
                focusRequester.requestFocus()
            }


            TextField(
                value = text,
                label = { Text("Codigo Item Fabrica") },
                readOnly = false,
                onValueChange = { newText ->
                    text = newText

                    if (newText.length >= 20) {
                        extractedText = newText.substring(0, 20) // Primeros 20 caracteres //item
                        extractedText2 = newText.substring(20, newText.length.coerceAtMost(29))//serie_desde
                        extractedText3 = newText.substring(29, newText.length.coerceAtMost(38))//serie_hasta
                        extractedText4 = newText.substring(39, newText.length.coerceAtMost(52))//ean

                    } else {
                        extractedText =
                            newText // Si hay menos de 20 caracteres, se toma todo el texto en el primer campo
                        extractedText2 = "" // No hay texto para el segundo campo
                        extractedText3 = "" // No hay texto para el segundo campo
                        extractedText4 = ""
                    }

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 16.dp),

                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                )
            )


            Spacer(modifier = Modifier.height(10.dp))
            // Texto "Modelo"
            Text(
                text = "Item",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            // Segundo TextField: donde se muestran los primeros caracteres
            TextField(
                value = extractedText,
                onValueChange = { /* No se permite la edición */ },
                label = { Text("00 - 20") },
                readOnly = true, // Este campo es solo de lectura
                modifier = Modifier
                    .width(250.dp) // Definir ancho
                    .height(70.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp, // Tamaño del texto
                    color = Color.Red, // Color del texto
                    fontFamily = FontFamily.Serif, // Familia de fuentes
                    fontWeight = FontWeight.Bold // Peso de la fuente
                ),
                enabled = false

            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Serie Desde",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = extractedText2,
                onValueChange = { /* No se permite la edición */ },
                label = { Text("21 - 28") },
                readOnly = true, // Este campo es solo de lectura
                modifier = Modifier
                    .width(250.dp)
                    .height(70.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp, // Tamaño del texto
                    color = Color.Black, // Color del texto
                    fontFamily = FontFamily.Serif, // Familia de fuentes
                    fontWeight = FontWeight.Bold // Peso de la fuente
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Serie Hasta",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = extractedText3,
                onValueChange = { /* No se permite la edición */ },
                label = { Text("29 - 38") },
                readOnly = true, // Este campo es solo de lectura
                modifier = Modifier
                    .width(250.dp) // Definir ancho
                    .height(70.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp, // Tamaño del texto
                    color = Color.Black, // Color del texto
                    fontFamily = FontFamily.Serif, // Familia de fuentes
                    fontWeight = FontWeight.Bold // Peso de la fuente
                ),
                enabled = false
            )



            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Código EAN",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            TextField(
                value = extractedText4,
                onValueChange = { /* No se permite la edición */ },
                label = { Text("40 al 52") },
                readOnly = true, // Este campo es solo de lectura
                modifier = Modifier
                    .width(250.dp)
                    .height(70.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp, // Tamaño del texto
                    color = Color.Black, // Color del texto
                    fontFamily = FontFamily.Serif, // Familia de fuentes
                    fontWeight = FontWeight.Bold // Peso de la fuente
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = textFieldValue2,
                onValueChange = { textFieldValue2 = it },
                modifier = Modifier
                    .width(250.dp)
                    .height(70.dp),
                label = { Text("Codigo Cargador Comercial Chile") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))


            if (errorMessage.isNotEmpty()) {
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            LaunchedEffect(extractedText)
            {
                // Log.d("*MAKITA*", "INGRESANDO API $extractedText")
                // Cuando cambie extractedText, se hace la llamada a la API
                if (!extractedText.isNullOrEmpty()) {
                    Log.d("*MAKITA*", "NOESVACIOELTEXTO $extractedText")
                    try {
                        // Llama a la API con extractedText
                        val apiResponse = apiService.obtenerHerramienta(extractedText)

                        // Log.d("*MAKITA*", "RESPUESTA: ${apiResponse.size}")

                        // Guarda la respuesta de la API en el estado response
                        response = apiResponse

                        //Log.d("*MAKITA*", "Tamaño de la respuesta: ${response}")

                        errorState = null

                        if (apiResponse.isNullOrEmpty()) {
                            // No es error , no se encuentra definido en tabla HerramientasCargador
                            // Log.d("*MAKITA*", "ES XX EMPTY: ${apiResponse}")
                            errorState = " No se encontraron datos para el item proporcionado"
                            mostrarDialogo(
                                context,
                                "Advertencia",
                                "Item sin Cargador Definido (Consulte a Comex)"
                            )
                        }

                        val tieneValoresNulos = apiResponse.any { it.item == null }

                        if (tieneValoresNulos) {
                            // Log.d("*MAKITA*", "La respuesta contiene valores nulos")
                            mostrarDialogo(
                                context,
                                "Advertencia",
                                "Item sin Cargador Definido (Consulte a Comex)"
                            )
                        } else {
                            // Procesar la respuesta si no hay valores nulos
                            response = apiResponse


                            //Log.d("*MAKITA*", " No tiene valores nulos: $response")
                            // textFieldValue2 = response

                            Toast.makeText(context, "Item con Cargador Definido $response", Toast.LENGTH_SHORT).show()

                            //mostrarDialogo(
                            //   context,
                            //   "Exito!",
                            //   "Item con Cargador Definido $response"
                             // )
                        }
                    } catch (e: Exception) {

                        // errorState =
                        if (e.message?.contains("404") == true) {
                            mostrarDialogo(context, "Error", "No se encontraron datos para el item")
                        } else {
                            "Error al obtener cargador: ${e.message}"
                        }
                        e.printStackTrace()

                        Log.e("*MAKITA*", "Error al obtener datos: ${e.message}")
                        // Manejar error de la API+
                        mostrarDialogo(context, "Error", "Error al obtener datos: ${e.message}")

                    }
                }

                else {
                  //  Log.d("*MAKITA*", "vacio")
                    // response = "El texto está vacío, no se puede realizar la llamada a la API."
                     Toast.makeText(context, "Capture Herramienta", Toast.LENGTH_SHORT).show()
                    //mostrarDialogo(
                     //   context,
                      //  "Inicio",
                       // "Capture Codigo"
                   //)
                }
            }


        }


        // ACA RECORRE EL RESPONSE

       // Log.d("*MAKITA*", "  PASAPOR ACA: $response")
        if (response.isNotEmpty()) {
            Text(
                text = "Resultados de la API:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Itera sobre la lista de respuesta
            response.forEach { item ->
                Text(

                    text = "Item: ${item.item}, Descripción: ${item.descripcion}, Código Chile 1: ${item.CodigoChile1}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp),

                    )
                textFieldValue2 = item.CodigoChile1.padStart(10, '0')




            }


            // Si hay un error, mostrar el mensaje de error
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }



        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth() // Llenar el ancho disponible
                .padding(horizontal = 16.dp), // Añadir padding horizontal
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {


            Button(
                onClick = {
                    // Limpiar el contenido de los campos de texto
                    text = ""
                    extractedText = ""
                    extractedText2 = ""
                    extractedText3 = ""
                    extractedText4 = ""
                    textFieldValue2 = ""
                    response = emptyList()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00909E)),
                modifier = Modifier
                    .width(110.dp) // Ancho del botón ajustado
                    .height(40.dp) // Alto del botón ajustado
            )
            {
                Text(
                    text = "Limpiar ",

                    style = TextStyle
                        (
                        fontSize = 13.sp,  // Cambiar tamaño del texto
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                )
            }

            Button(
                onClick = { /*printpdf*/ },

                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00909E)),
                modifier = Modifier
                    .width(110.dp) // Ancho del botón ajustado
                    .height(40.dp) // Alto del botón ajustado
            )
            {
                Text(
                    text = "Imprimir",
                    style = TextStyle
                        (
                        fontSize = 13.sp,  // Cambiar tamaño del texto
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                )
            }






        }

        /*ACA*/


        Column(
            modifier = Modifier.fillMaxSize(),  // Ajusta la columna a todo el espacio disponible
            horizontalAlignment = Alignment.CenterHorizontally,  // Centra los elementos horizontalmente
            verticalArrangement = Arrangement.Center  // Centra los elementos verticalmente
        ) {
      //      // Botón para generar el código PDF417


            Spacer(modifier = Modifier.height(16.dp))  // Añade espacio entre el botón y la imagen



            Button(onClick =
            {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        (context as Activity),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        100
                    )
                }
                else
                {
                    showDialog = true
                    startBluetoothDiscovery(context, bluetoothAdapter, setPrinters)
                }
            })
            {
                Text("Seleccionar Impresora Bluetooth")
            }


            // Diálogo de selección de dispositivos
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Seleccione una impresora") },
                    text = {
                            BluetoothDeviceList(
                            deviceList = printers,

                            onDeviceSelected = { device ->
                                selectedDevice = device
                                selectedPrinterName = device.name ?: "Desconocida"
                                showDialog = false
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false })
                        {
                            Log.d("*MAKITA*IMpresora", selectedPrinterName )
                            Text("Cerrar" )
                        }
                    }
                )
            }

            // Mostrar el nombre de la impresora seleccionada
            if (selectedPrinterName.isNotEmpty()) {
                Text("Impresora seleccionada: $selectedPrinterName", fontSize = 16.sp, fontWeight = FontWeight.Bold)
              //  printBitmap(socket: BluetoothSocket, bitmap:
              //  printBitmap(selectedDevice, bitmap)
              //  printBitmapToBluetoothDevice(selectedDevice, bitmap)
              //  printDataToBluetoothDevice(device, data)
            }


            Button(
                onClick = {
                    selectedDevice?.let { device ->
                        //extractedText = ""
                        //extractedText2 = ""
                        //extractedText3 = ""
                        //extractedText4 = ""
                        //textFieldValue2 = ""

                        val data = extractedText
                        val printerLanguage = "ZPL"  // Cambiar según el lenguaje soportado por la impresora

                        val codigoCargador = (extractedText?.trim() ?: "").padEnd(20, '0') +
                                (extractedText2?.trim() ?: "").padEnd(10, '0') +
                                (extractedText3?.trim() ?: "").padEnd(10, '0') +
                                (extractedText4?.trim() ?: "").padEnd(13, '0') +
                                "0" +
                                (textFieldValue2?.trim() ?: "").padEnd(10, '0') // CodigoChile
                                "0000000000" +    // Codigo Comercial
                                "000000000000000000000000000000"  // Nro Proforma



                        //  (extractedText2 ?: "").padEnd(10, '0')

                        Log.d("*MAKITA*", "CodigoCargador $codigoCargador")

                        //val codigoCargador = extractedText.padStart(20, '0')   // item
                        //    + extractedText2.padStart(10, '0')                // serie
                        //   + extractedText3.padStart(10, '0')                // serie hasta
                        //   + extractedText3.padStart(13, '0')                // ean
                        //   + "0"                                             // agrega '0' como String
                        //    + textFieldValue2.padStart(10, '0')               // CodigoChile
                        //    + "0000000000"                                    // CodigoComercial
                        //    + "000000000000000000000000000000"                // Otros códigos

                        printDataToBluetoothDevice(device, data, context, printerLanguage, textFieldValue2 , codigoCargador)
                    }
                },
                enabled = selectedDevice != null
            ) {
                Text(
                    text = "Imprimir",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }

    }

}

@RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
fun printDataToBluetoothDevice(
    device: BluetoothDevice,
    data: String,
    context: Context,
    printerLanguage: String , // Lenguaje de Programacion Zebra  ZPL (ZPL, CPCL o ESC/POS)
    comercial: String,
    CodigoConcatenado: String
) {
    val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Conectar al dispositivo Bluetooth
            val bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket.connect()

            if (bluetoothSocket.isConnected) {
                val outputStream = bluetoothSocket.outputStream

                // Enviar los datos de impresión
                Log.d("*MAKITA*", " PASA POR 1  $data")
               // val configLabel = getConfigLabel(printerLanguage, data)
               // outputStream.write(configLabel)

               // outputStream.flush()  // Asegurarse de que todos los datos se envíen

                // Enviar comando de finalización si es ZPL o CPCL
                if (printerLanguage == "ZPL")
                {

                    Log.d("*MAKITA*", "Imprimir  $CodigoConcatenado")

                    val linea2 = "^XA\n " +
                             "^PW354 \n" +   // Ancho de la etiqueta (3 cm = 354 dots)
                             "^LL354 \n" +
                             "^FO50,25\n " +
                             "^ADN,15,13\n " +
                             "^FD$data^FS\n " +
                             "^FO50,70\n " +
                             "^ADN,15,12\n " +
                           //"^B7N,5,10,2,5,N\n " +
                            //"^B7N,1,30,2,30,N\n  " +
                             //"^B7N,2,10,2,30,NY\n  " +
                             //"^FD$comercial^FS\n " +
                             "^B7N,5,10,2,20,N" +
                              "^FD$CodigoConcatenado^FS " +
                             "^FO50,190\n " +
                             "^ADN,15,13\n " +
                             "^FD$comercial^FS\n " +
                             "^XZ\n"

                    outputStream.write(linea2.toByteArray(Charsets.US_ASCII))
                    outputStream.flush()
                   // outputStream.write("^XZ".toByteArray(Charsets.US_ASCII)) // Finalizar trabajo en ZPL
                }

                // Cerrar la conexión solo después de que los datos se hayan enviado

                //bluetoothSocket.close()

                // Mostrar un mensaje de éxito
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Impresión Correcta", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si no se pudo conectar, mostrar un mensaje de error
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No se pudo conectar al dispositivo Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejo de errores
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}





fun getConfigLabel(printerLanguage: String, data: String): ByteArray {
    println("printerLanguage , $printerLanguage")
    return when (printerLanguage) {
        "ZPL" -> {
            // Calcular la posición para centrar el texto en una etiqueta de 30mm x 30mm (aprox. 236 puntos)
            val labelWidth = 236  // Ancho de la etiqueta en puntos
            val labelHeight = 236 // Alto de la etiqueta en puntos
            val textWidth = 30  // Ancho de la fuente (ajustar según el tamaño de la fuente)
            val textHeight = 30 // Alto de la fuente (ajustar según el tamaño de la fuente)

            // Calcular las coordenadas para centrar el texto
           // val xPosition = (labelWidth - textWidth) / 2
           // val yPosition = (labelHeight - textHeight) / 2
             val xPosition =1
            val yPosition  =1

            // Comando ZPL con el texto centrado
            //"^XA^FO${xPosition},${yPosition}^GB200,200,8^FS^FT65,255^A0N,20,20^FD$data^FS^XZ".toByteArray(Charsets.US_ASCII)
            //"^XA^FO1,1^GB379,371,8^FS^FT65,255^A0N,135,134^FD$data^FS^XZ".toByteArray(Charsets.US_ASCII);

            "^XA ^FO50,50 ^B8N,100,Y,N ^FD$data^FS ^XZ".toByteArray(Charsets.US_ASCII);



        }
        "CPCL" -> {
            // Comando CPCL con el texto proporcionado
            ("! 0 200 200 406 1\r\n" +
                    "ON-FEED IGNORE\r\n" +
                    "BOX 20 20 380 380 8\r\n" +
                    "T 0 6 137 177 $data\r\n" +
                    "PRINT\r\n").toByteArray(Charsets.US_ASCII)
        }
        else -> {
            // Texto ASCII si el lenguaje no está definido
            data.toByteArray(Charsets.US_ASCII)
        }
    }
}









// Función ficticia para convertir Bitmap a ZPL, debes implementarla según el formato de tu impresora.
// Función para convertir Bitmap a ZPL (esto es un ejemplo básico para ZPL)
fun convertBitmapToZPL(bitmap: Bitmap): ByteArray {
    // Aquí estamos generando un formato ZPL básico para la impresora.
    // Debes ajustar según las especificaciones de tu impresora
    val zplHeader = "^XA"  // Comienza la etiqueta ZPL
    val zplFooter = "^XZ"  // Termina la etiqueta ZPL
    val zplBody = "^FO50,50^GB200,200,200^FS"  // Un cuadro de 200x200 pixeles
    val zplBarcode = "^FO50,50^B3N,N,100,Y^FD>:${bitmap.hashCode()}^FS"  // Ejemplo de código de barras con el hash del bitmap

    // Concatenar todo el contenido ZPL
    val zpl = "$zplHeader$zplBody$zplBarcode$zplFooter"
    return zpl.toByteArray(Charsets.US_ASCII)
}

fun checkBluetoothPermissions(context: Context): Boolean {
    val bluetoothConnectPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.BLUETOOTH_CONNECT
    )
    val bluetoothScanPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.BLUETOOTH_SCAN
    )
    val fineLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Si los permisos no están concedidos, solicita los permisos
    if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED ||
        bluetoothScanPermission != PackageManager.PERMISSION_GRANTED ||
        fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1 // Código de solicitud de permisos
        )
        return false
    }

    return true
}


fun generarCodigoPDF417(texto: String): Bitmap? {
    val pdf417Writer = PDF417Writer()
    Log.d("*MAKITA*", "dibuja un bitmap 417")
    return try {
        val bitMatrix: BitMatrix = pdf417Writer.encode(texto, BarcodeFormat.PDF_417, 600, 300)
        val ancho = bitMatrix.width
        val alto = bitMatrix.height
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)
        for (x in 0 until ancho) {
            for (y in 0 until alto) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        Log.d("*MAKITA*", "mostrar 417 $bitmap")
        bitmap
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun BluetoothDeviceList(
    deviceList: List<BluetoothDevice>,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {

    val context = LocalContext.current
    // Verifica el permiso BLUETOOTH_CONNECT en dispositivos con Android 12 (API 31) o superior

    Log.d("*MAKITA*", " version blue 1")


    val hasBluetoothConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    } else
    {
        Log.d("*MAKITA*", " si tiene permiso")
        true // No se requiere permiso en versiones anteriores
    }



    // Solo muestra la lista si el permiso es otorgado o si el sistema no lo requiere
    if (hasBluetoothConnectPermission) {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(deviceList) { device ->
                Text(
                    text = device.name ?: "Dispositivo desconocido",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeviceSelected(device) }
                        .padding(8.dp)
                )
            }
        }
    } else {
        // Mostrar mensaje o manejar el caso en el que no se tiene el permiso
        Log.d("*MAKITA*", " permisos blue")
        Text("Permiso Bluetooth no otorgado. No se pueden mostrar los dispositivos.")
    }
}

fun startBluetoothDiscovery(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    setDevices: (List<BluetoothDevice>) -> Unit
) {
    val foundDevices = mutableListOf<BluetoothDevice>()

    // Receptor para dispositivos encontrados
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!foundDevices.contains(it))
                    {
                        foundDevices.add(it)
                        setDevices(foundDevices)
                    }
                }
            }
        }
    }

    // Registrar el receptor para detectar dispositivos Bluetooth
    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    context.registerReceiver(receiver, filter)

    // Verifica y solicita permisos si es necesario
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        // Maneja la solicitud de permisos aquí
        return
    }

    // Inicia el descubrimiento de dispositivos
    bluetoothAdapter?.startDiscovery()
}

fun mostrarDialogo(context: Context, titulo: String, mensaje: String) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(titulo)
    builder.setMessage(mensaje)
    builder.setPositiveButton("OK", null)
    builder.show()
}
/*


fun printDataToBluetoothDevice(device: BluetoothDevice, data: String, context: Context, bitmap: Bitmap?) {
    if (bitmap == null) {
        Log.e("*MAKITA*", "Error: El bitmap proporcionado es null.")
        return
    }

    Log.e("*MAKITA*", "printDataToBluetoothDevice. $device")
    val uuid = device.uuids?.firstOrNull()?.uuid ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    var bluetoothSocket: BluetoothSocket? = null
    val zplCommand = "^XA^FO100,100^A0N,50,50^FDHello, World!^FS^XZ"  // Comando ZPL de prueba

    // Convierte el bitmap a formato gráfico ZPL
    val zplGraphic = bitmapToZPLGraphics(bitmap)
    val zplGraphicCommand = "^XA^FO50,50^GFA,${zplGraphic.length / 2},${zplGraphic.length / 2},${bitmap.width / 8},$zplGraphic^FS^XZ"

    Log.e("*MAKITA*", "ZPL Graphic Command: $zplGraphicCommand")

    try {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Establecer conexión Bluetooth
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        bluetoothSocket.connect()

        val outputStream: OutputStream = bluetoothSocket.outputStream

        // Enviar comando de gráfico ZPL
        outputStream.write(zplGraphicCommand.toByteArray())
        outputStream.flush()

    } catch (e: IOException) {
        Log.e("*MAKITA*", "catch. $device")
        e.printStackTrace()
    } finally {
        try {
            Log.e("*MAKITA*", "Cierra Bluetooth. $device")
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("*MAKITA*", "catch. $device")
            e.printStackTrace()
        }
    }
}

fun bitmapToZPLGraphics(bitmap: Bitmap): String {
    val width = bitmap.width
    val height = bitmap.height
    val hexData = StringBuilder()

    // Convertir el bitmap en formato gráfico ZPL
    for (y in 0 until height step 8) {
        for (x in 0 until width) {
            var byteData = 0
            for (bit in 0 until 8) {
                val pixel = if (y + bit < height) bitmap.getPixel(x, y + bit) else -1
                if (pixel != -1 && (pixel and 0xFF000000.toInt()) != 0) {  // Si el pixel es negro
                    byteData = byteData or (1 shl (7 - bit))
                }
            }
            hexData.append(String.format("%02X", byteData))
        }
    }

    return hexData.toString()
}



fun printBitmap(socket: BluetoothSocket, bitmap: Bitmap) {
    try {
        val outputStream: OutputStream = socket.outputStream
        val bytes = bitmapToByteArray(bitmap) // Convertir el bitmap a bytes
        outputStream.write(bytes)
        outputStream.flush()
        Log.d("Print", "Imagen enviada a la impresora")
    } catch (e: Exception) {
        Log.e("Print", "Error al enviar la imagen: ${e.message}")
        e.printStackTrace()
    }
}


fun printBitmapToBluetooth(bitmap: Bitmap, socket: BluetoothSocket) {
    val outputStream = socket.outputStream
    val escPosData = bitmapToESCPosData(bitmap)

    outputStream.write(escPosData)
    outputStream.flush()
}


fun bitmapToESCPosData(bitmap: Bitmap): ByteArray {
    val width = bitmap.width
    val height = bitmap.height
    val command = ByteArrayOutputStream()

    // Comando ESC/POS para modo gráfico
    command.write(byteArrayOf(0x1B, 0x33, 0x00)) // Establece el modo gráfico

    for (y in 0 until height step 24) {
        command.write(byteArrayOf(0x1B, 0x2A, 33, (width and 0xFF).toByte(), (width shr 8).toByte()))

        for (x in 0 until width) {
            var slice = 0
            for (b in 0..2) {
                for (bit in 0..7) {
                    val yy = y + b * 8 + bit
                    if (yy < height) {
                        val pixel = bitmap.getPixel(x, yy)
                        if (pixel != -1) {
                            slice = slice or (1 shl (7 - bit))
                        }
                    }
                }
                command.write(slice)
            }
        }
        command.write(byteArrayOf(0x0A)) // Salto de línea
    }

    return command.toByteArray()
}



fun startBluetoothDiscovery(
    context: Context,
    bluetoothAdapter: BluetoothAdapter?,
    setDevices: (List<BluetoothDevice>) -> Unit
) {
    val foundDevices = mutableListOf<BluetoothDevice>()

    // Receptor para dispositivos encontrados
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!foundDevices.contains(it)) {
                        foundDevices.add(it)
                        setDevices(foundDevices) // Actualiza la lista en la UI
                    }
                }
            }
        }
    }

    // Registrar el receptor para detectar dispositivos Bluetooth
    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    context.registerReceiver(receiver, filter)

    // Verifica y solicita permisos si es necesario
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        // Maneja la solicitud de permisos aquí
        return
    }

    // Inicia el descubrimiento de dispositivos
    bluetoothAdapter?.startDiscovery()
}



fun isPrinter(context: Context, device: BluetoothDevice): Boolean {
    // Verifica el permiso BLUETOOTH_CONNECT en Android 12 (API 31) o superior
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            // Si el permiso no está otorgado, maneja el caso según sea necesario
            return false
        }
    }

    // Filtra dispositivos que podrían ser impresoras
    return device.name?.contains("printer", ignoreCase = true) ?: false
}





fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) // Puedes cambiar a JPG si es necesario
    return stream.toByteArray()
}

fun String.padLeft(totalLength: Int, padChar: Char = ' '): String
{
    return this.padStart(totalLength, padChar)
}



fun saveTextFile(context: Context) {
    val text = "Este es un archivo de texto generado en Android"
    val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "mi_archivo.txt")

    try {
        val fileOutputStream = FileOutputStream(filePath)
        fileOutputStream.write(text.toByteArray())
        fileOutputStream.close()
        Toast.makeText(context, "Archivo de texto guardado en: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar archivo: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun createPdfFromBarcode(context: Context, bitmap: Bitmap) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = document.startPage(pageInfo)

    val canvas: Canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    document.finishPage(page)

    val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyPDFs")
    if (!directory.exists()) {
        directory.mkdirs()
    }

    val filePath = File(directory, "codigo_pdf417.pdf")

    try {
        document.writeTo(FileOutputStream(filePath))
        Toast.makeText(context, "PDF generado correctamente en ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_LONG).show()
    } finally {
        document.close()
    }
}
*/

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("GMT-4") // Establece la zona horaria de Santiago de Chile
    return formatter.format(date)
}


@Preview(showBackground = false)

@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        FunSaludo("Android")
    }
}
