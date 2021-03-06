package citrix.moonshot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import citrix.moonshot.enums.BootTarget;
import citrix.moonshot.enums.Health;
import citrix.moonshot.enums.HttpMethod;
import citrix.moonshot.enums.HttpScheme;
import citrix.moonshot.enums.ResetType;
import citrix.moonshot.models.MoonshotRestClientResponse;
import citrix.moonshot.models.Node;

public class MoonshotClient {

    private static final Logger s_logger = Logger
            .getLogger(MoonshotClient.class);

    public static final int DEFAULT_CONNECTION_TIMEOUT = 20000;

    public static final int DEFAULT_SCOCKET_TIMEOUT = 20000;

    private static final String MOONSHOT_URL_SUFFIX = "/rest/v1/";

    private static final String SYSTEMS_SUFFIX = "Systems/";

    private static final String SYSTEMS_SUMMARY_SUFFIX = "SystemsSummary";

    private String moonshotBaseUrl;

    private String moonshotSystemsUrl;

    private String moonshotSystemsSummaryUrl;

    private HttpScheme scheme;

    private int port;

    //private ThreadLocal<UsernamePasswordCredentials> credentials;

    private UsernamePasswordCredentials credentials;

    public MoonshotClient(String username, String password, String host,

    String scheme, Integer port) throws ConfigurationException {
        s_logger.debug("Initializing MoonshotClient. username:" + username  + " host:" + host);
        HttpScheme schemeEnum = getScheme(scheme);

        if (port == null || port == -1) {
            if (schemeEnum.equals(HttpScheme.HTTP)) {
                port = 8080;
            } else if (schemeEnum.equals(HttpScheme.HTTPS)) {
                port = 443;
            } else {
                throw new RuntimeException("Invalid scheme");
            }
        }

        boolean exceptionfound = false;
        Exception ex = null;
        try {
            initialize(username, password, host, schemeEnum, port);
        } catch (URISyntaxException e) {
            exceptionfound = true;
            ex = e;
            s_logger.error("Error in initializing MoonshotClient", e);
        }

        if (exceptionfound) {
            throw new ConfigurationException(
                    "MoonshotClient could be configured. Exception:" + ex);
        }
    }

    public void setCredentials(String username, String password) {
        //credentials.set(new UsernamePasswordCredentials(username, password));
        credentials = new UsernamePasswordCredentials(username, password);
    }

    public String getPowerStatus(String cartridgeNodeLocation) {
        Node n = getNode(cartridgeNodeLocation);
        return n.getPower().toString();
    }

    public boolean setNodePowerStatus(String cartridgeNodeLocation,
            ResetType resetType) {

        JSONObject jsonPayload = createResetRequest(resetType);

        MoonshotRestClientResponse resp = invoke(moonshotSystemsUrl
                + cartridgeNodeLocation, HttpMethod.POST, null, jsonPayload);

        return resp.isSuccessful();
    }

    public boolean bootOnce(String cartridgeNodeLocation, BootTarget bootTarget) {

        JSONObject jsonPayload = createBootOrderRequest(bootTarget, null, null);

        MoonshotRestClientResponse resp = invoke(moonshotSystemsUrl
                + cartridgeNodeLocation, HttpMethod.PATCH, null, jsonPayload);

        return resp.isSuccessful();
    }

    public boolean pingNode(String cartridgeNodeLocation) {
        Node n = getNode(cartridgeNodeLocation);
        if (Health.OK.equals(n.getHealth())) {
            return true;
        } else {
            return false;
        }
    }

    public List<Node> getAllNodes() {
        List<Node> nodeList = new ArrayList<Node>();
        JSONArray nodes = getAllnodes();
        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = null;

            try {
                node = nodes.getJSONObject(i);
            } catch (JSONException e) {
                s_logger.error("Error in parsing json", e);
                throw new RuntimeException("Exception in Json parsig", e);
            }

            if (node != null) {
                Node n = getNodeByJson(node);
                nodeList.add(n);
            }
        }
        return nodeList;
    }

    public Node getNode(String shortName) {
        Node node = getNodeByJson(invoke(moonshotSystemsUrl + shortName,
                HttpMethod.GET, null, null).getJsonResponse());

        return node;
    }

    private void initialize(String username, String password, String host,
            HttpScheme scheme, int port) throws URISyntaxException {

        this.scheme = scheme;
        this.port = port;

        //credentials = new ThreadLocal<UsernamePasswordCredentials>();
        //credentials.set(new UsernamePasswordCredentials(username, password));

        credentials = new UsernamePasswordCredentials(username, password);
        moonshotBaseUrl = new URIBuilder().setScheme(scheme.toString())
                .setHost(host).setPort(port).setPath(MOONSHOT_URL_SUFFIX)
                .build().toString();

        moonshotSystemsUrl = moonshotBaseUrl + SYSTEMS_SUFFIX;
        moonshotSystemsSummaryUrl = moonshotBaseUrl + SYSTEMS_SUMMARY_SUFFIX;
    }

    private HttpClient prepareHttpClient() throws KeyManagementException,
    NoSuchAlgorithmException, KeyStoreException {
        Scheme newScheme;

        if (scheme.equals(HttpScheme.HTTPS)) {

            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs,
                        String authType) {
                }
            } }, new SecureRandom());

            SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                    new AllowAllHostnameVerifier());

            newScheme = new Scheme(HttpScheme.HTTPS.toString(), 443, sf);

        } else {
            newScheme = new Scheme(HttpScheme.HTTP.toString(), port,
                    PlainSocketFactory.getSocketFactory());
        }

        SchemeRegistry schemeRegistry = new SchemeRegistry();

        schemeRegistry.register(newScheme);

        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(
                schemeRegistry);

        connectionManager.setMaxTotal(500); // TODO make configurable
        connectionManager.setDefaultMaxPerRoute(50); // TODO make configurable

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, DEFAULT_CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, DEFAULT_SCOCKET_TIMEOUT);
        return new DefaultHttpClient(connectionManager, params);
    }

    private MoonshotRestClientResponse invoke(String url, HttpMethod method,
            Map<String, String> data, JSONObject jsonPayload) {

        s_logger.debug("Invoking URL:" + url + " Method:" + method.name() + " data:" +  String.valueOf(data) + " Payload:" + jsonPayload);
        HttpClient client = null;
        String errorMessage = "Error in creating Http client";
        try {
            client = prepareHttpClient();
        } catch (KeyManagementException e1) {
            s_logger.error(errorMessage, e1);
            return new MoonshotRestClientResponse(null, null, e1);
        } catch (NoSuchAlgorithmException e1) {
            s_logger.error(errorMessage, e1);
            return new MoonshotRestClientResponse(null, null, e1);
        } catch (KeyStoreException e1) {
            s_logger.error(errorMessage, e1);
            return new MoonshotRestClientResponse(null, null, e1);
        }

        JSONObject jsonResponse = null;

        HttpRequestBase request = null;

        HttpResponse httpResponse = null;
        Exception ex = null;

        switch (method) {
        case GET: {
            request = new HttpGet(url);
            break;
        }
        case POST: {
            request = new HttpPost(url);
            ((HttpPost) request).setEntity(new StringEntity(jsonPayload
                    .toString(), ContentType.APPLICATION_JSON));
            break;
        }
        case PUT: {

            break;
        }
        case PATCH: {
            request = new HttpPatch(url);
            ((HttpPatch) request).setEntity(new StringEntity(jsonPayload
                    .toString(), ContentType.APPLICATION_JSON));
            break;
        }
        case DELETE: {

            break;
        }
        default: {
            break;
        }
        }

        if (request != null) {
            try {
               // request.addHeader(new BasicScheme().authenticate(
               //         credentials.get(), request, null));

               request.addHeader(new BasicScheme().authenticate(
                         credentials, request, null));

                s_logger.debug("Request: " + request);

                httpResponse = client.execute(request);

                s_logger.debug("Response: " + httpResponse);

                jsonResponse = new JSONObject(EntityUtils.toString(httpResponse
                        .getEntity()));
            } catch (AuthenticationException e) {
                ex = e;
                s_logger.error("Error in executing request", e);
            } catch (ClientProtocolException e) {
                ex = e;
                s_logger.error("Error in executing request", e);
            } catch (ParseException e) {
                ex = e;
                s_logger.error("Error in executing request", e);
            } catch (IOException e) {
                ex = e;
                s_logger.error("Error in executing request", e);
            } catch (JSONException e) {
                ex = e;
                s_logger.error("Error in executing request", e);
            }
        }

        return new MoonshotRestClientResponse(httpResponse, jsonResponse, ex);
    }

    private JSONObject createBootOrderRequest(BootTarget bootOnce,
            Boolean wakeOnLan, BootTarget[] bootOrder) {

        JSONObject oem = new JSONObject();
        JSONObject hp = new JSONObject();
        JSONObject optionsOuter = new JSONObject();
        JSONObject options = new JSONObject();

        try {
            if (bootOnce != null) {
                options.put("BootOnce", bootOnce.toString());
            }

            if (wakeOnLan != null) {
                if (wakeOnLan) {
                    options.put("WOL", "ENABLE");
                } else {
                    options.put("WOL", "DISABLE");
                }
            }

            if (bootOrder != null) {

                if (bootOrder.length == 0 || bootOrder.length > 2) {
                    throw new IllegalArgumentException(
                            "bootOrder cannot be empty or can have max of 2 elements");
                } else {
                    options.put("BootOrder", new JSONArray(bootOrder));
                }

            }
            optionsOuter.put("Options", options);
            hp.put("Hp", optionsOuter);
            oem.put("Oem", hp);
        } catch (JSONException e) {
            throw new RuntimeException("Error in parsing Json", e);
        }

        return oem;
    }

    private JSONObject createResetRequest(ResetType resetType) {
        JSONObject resetRequest = new JSONObject();
        try {
            resetRequest.put("Action", "Reset");
            resetRequest.put("ResetType", resetType.toString());
        } catch (JSONException e) {
            throw new RuntimeException("Error in parsing Json", e);
        }
        return resetRequest;
    }

    private JSONArray getAllnodes() {

        JSONArray response = null;

        try {
            response = invoke(moonshotSystemsSummaryUrl, HttpMethod.GET, null,
                    null).getJsonResponse().getJSONArray("Systems");
        } catch (JSONException e) {
            throw new RuntimeException("Error in parsing Json", e);
        }

        return response;
    }

    private Node getNodeByJson(JSONObject obj) {

        Node node = null;

        try {
            node = new Node(obj.getString(Node.NODE_NAME));

            if (obj.has(Node.NODE_POWER)) {
                node.setPower(obj.getString(Node.NODE_POWER));
            }

            if (obj.has(Node.NODE_HEALTH)) {
                node.setHealth(Health.valueOf(obj.getString(Node.NODE_HEALTH)));
            } else {
                node.setHealth(Health.valueOf(obj.getJSONObject(
                        Node.NODE_STATUS).getString(Node.NODE_HEALTH)));
            }

            JSONArray mac = null;
            if (obj.has(Node.NODE_HOST_MAC_ADDRESS)) {
                mac = obj.getJSONArray(Node.NODE_HOST_MAC_ADDRESS);
            } else {
                mac = obj.getJSONObject(Node.NODE_HOST_CORRELATION)
                        .getJSONArray(Node.NODE_HOST_MAC_ADDRESS);
            }

            String[] macArray = new String[mac.length()];
            for (int z = 0; z < mac.length(); z++) {
                macArray[z] = mac.getString(z);
            }

            node.setMac(macArray);

            if (obj.has(Node.NODE_MEMORY)) {
                Object memory = obj.get(Node.NODE_MEMORY);
                if (memory instanceof JSONObject) {
                    if (((JSONObject) memory)
                            .has(Node.NODE_TOTAL_SYSTEM_MEMORY_GB)) {
                        node.setMemory(((JSONObject) memory)
                                .getInt(Node.NODE_TOTAL_SYSTEM_MEMORY_GB));
                    }
                } else if (memory instanceof String) {
                    node.setMemory(Integer.parseInt(((String) memory)
                            .split("\\s+")[0]));
                }
            }

            if (obj.has(Node.NODE_PROCESSORS)) {
                JSONObject processors = obj.getJSONObject(Node.NODE_PROCESSORS);

                if (processors.has(Node.NODE_CURRENT_CLOCK_SPEED_MHZ)) {
                    node.setCurrentClockSpeed(processors
                            .getInt(Node.NODE_CURRENT_CLOCK_SPEED_MHZ));
                }

                if (processors.has(Node.NODE_MAX_CLOCK_SPEED_MHZ)) {
                    node.setMaxClockSpeed(processors
                            .getInt(Node.NODE_MAX_CLOCK_SPEED_MHZ));
                }

                if (processors.has(Node.NODE_NUMBER_OF_CORES)) {
                    node.setNoOfCores(processors
                            .getInt(Node.NODE_NUMBER_OF_CORES));
                }

            }

        } catch (NumberFormatException e) {
            throw new RuntimeException("Error in parsing number", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error in parsing number", e);
        }

        return node;
    }

    private citrix.moonshot.enums.HttpScheme getScheme(String scheme) {
        HttpScheme schemeEnum = null;
        if (scheme == null) {
            scheme = "https"; // TODO - callers responsiblity to
                                           // pass correct in arg.. later remove
                                           // this.
            // throw new
            // RuntimeException("Scheme can not be null. Pass either http or https.");
        }


        if (scheme.equalsIgnoreCase("http")) {
            schemeEnum = citrix.moonshot.enums.HttpScheme.HTTP;
        } else if (scheme.equalsIgnoreCase("https")) {
            schemeEnum = citrix.moonshot.enums.HttpScheme.HTTPS;
        }
        return schemeEnum;
    }

}
