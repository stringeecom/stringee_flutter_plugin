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

-dontwarn com.stringee.R$styleable
-dontwarn javax.naming.InvalidNameException
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.ldap.LdapName
-dontwarn javax.naming.ldap.Rdn
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid