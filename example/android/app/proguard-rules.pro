#Flutter Wrapper
-dontwarn org.webrtc.**
-keep class org.webrtc.** { *; }
-keep class com.stringee.** { *; }

-dontwarn com.android.volley.NetworkResponse
-dontwarn com.android.volley.Request
-dontwarn com.android.volley.RequestQueue
-dontwarn com.android.volley.Response$ErrorListener
-dontwarn com.android.volley.Response$Listener
-dontwarn com.android.volley.VolleyError
-dontwarn com.android.volley.toolbox.JsonObjectRequest
-dontwarn com.android.volley.toolbox.Volley
-dontwarn org.apache.http.entity.mime.MultipartEntity
-dontwarn org.apache.http.entity.mime.content.ContentBody
-dontwarn org.apache.http.entity.mime.content.FileBody