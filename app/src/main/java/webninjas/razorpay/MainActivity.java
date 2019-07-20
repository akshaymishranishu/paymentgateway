package webninjas.razorpay;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultListener;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements PaymentResultWithDataListener
{
    EditText et_amount;
    Button payment;
    int amount;
    static RequestQueue requestQueue;
    static String url = null;
    static String url2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Checkout.preload(getApplicationContext());

        et_amount = findViewById(R.id.editText2);
        payment = findViewById(R.id.button);

        requestQueue = Volley.newRequestQueue(this);
        url = "http://192.168.43.94:5000/order";

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                amount = Integer.parseInt(et_amount.getText().toString().trim());
                amount = amount * 100;
                String amt = String.valueOf(amount);
                sendCreateOrderData(amt);
            }
        });
    }

    private void sendCreateOrderData(String amt)
    {
        Map<String, String> params = new HashMap();
        params.put("amount", amt);
        params.put("currency", "INR");
        params.put("receipt", Calendar.getInstance().getTime().toString());
        params.put("payment_capture", "1");

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                startPayment(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                0));

        requestQueue.add(jsonRequest);
    }


    private void startPayment(JSONObject jsonResponse) {
        String razorpayOrdeId = null;

        try{
                razorpayOrdeId = jsonResponse.getString("id");
        }
        catch(JSONException e){

        }

        final Checkout checkout = new Checkout();
        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Modabba");
            options.put("description", "Order #123456");
            options.put("order_id", razorpayOrdeId);
            options.put("currency", "INR");
            options.put("amount", "300");

            JSONObject preFill = new JSONObject();
            preFill.put("email", "akshaymishranishu@gmail.com");
            preFill.put("contact", "7064007719");

            options.put("prefill", preFill);

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e("Error", e.toString());
        }
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData) {
        Toast.makeText(MainActivity.this, "Payment Successful"+" "+razorpayPaymentID+" "+paymentData.getPaymentId()+" "+
                paymentData.getSignature()+" "+paymentData.getOrderId(), Toast.LENGTH_LONG).show();
        String paymentId = paymentData.getPaymentId();
        String signature = paymentData.getSignature();
        String orderId = paymentData.getOrderId();

        sendDataForSignatureVerification(razorpayPaymentID, paymentId, signature, orderId);
    }

    @Override
    public void onPaymentError(int code, String response, PaymentData paymentData) {
        Toast.makeText(MainActivity.this, String.valueOf(code)+": "+response, Toast.LENGTH_LONG).show();
    }

    private void sendDataForSignatureVerification(String razorpayPaymentID, String paymentId, String signature, String orderId)
    {
        url2 = "http://192.168.43.94:5000/verifysign";

        Map<String, String> params = new HashMap();
        params.put("razorpayPaymentID", razorpayPaymentID);
        params.put("paymentId", paymentId);
        params.put("signature", signature);
        params.put("orderId", orderId);

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url2, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                0));

        requestQueue.add(jsonRequest);
    }
}
